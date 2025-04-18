package program.chatus;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ChatServer: listens on port 1234, accepts client connections,
 * and handles incoming commands/messages.
 */
public class ChatServer {
    private static final int PORT = 1234;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10); // Adjust if needed

    // Map of (username -> Socket) for all connected (online) clients
    private static final HashMap<String, Socket> clients = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle each client connection in a separate thread
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientId; // The username from the client
        private Connection connection; // Database connection for this thread

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                connection = DatabaseConnection.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // First line from client is the username
                clientId = in.readLine();
                System.out.println("Client " + clientId + " connected.");

                // Add the client to the "online" map
                synchronized (clients) {
                    clients.put(clientId, clientSocket);
                }

                // Continuously read messages from the client
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from " + clientId + ": " + message);

                    if (message.equals("/listFriends")) {
                        sendFriendList();
                    } else {
                        // Assume format "recipient: message"
                        handlePrivateMessage(message);
                    }
                }
            } catch (IOException | SQLException e) {
                System.out.println("Client " + clientId + " disconnected.");
            } finally {
                // Remove client from the online map
                synchronized (clients) {
                    clients.remove(clientId);
                }
                // Close resources
                try {
                    clientSocket.close();
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Send the friend list to the current user.
         * We do a subquery to get the "lastMessage" for each friend, returning 1 row per friend.
         */
        private void sendFriendList() throws SQLException {
            String query = """
                SELECT u2.username,
                       (
                         SELECT m2.content
                         FROM messages m2
                         WHERE
                           (m2.sender_id = u1.user_id AND m2.receiver_id = u2.user_id)
                           OR
                           (m2.sender_id = u2.user_id AND m2.receiver_id = u1.user_id)
                         ORDER BY m2.created_at DESC
                         LIMIT 1
                       ) AS lastMessage
                FROM users u1
                JOIN friends f ON u1.user_id = f.user_id
                JOIN users u2 ON f.friend_id = u2.user_id
                WHERE u1.username = ?
            """;

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, clientId);
            ResultSet rs = stmt.executeQuery();

            List<String> friendData = new ArrayList<>();
            while (rs.next()) {
                String friendUsername = rs.getString("username");
                String lastMsg        = rs.getString("lastMessage");
                if (lastMsg == null) {
                    lastMsg = "No messages yet";
                }
                friendData.add(friendUsername + ":" + lastMsg);
            }
            String joined = String.join(",", friendData);
            out.println("USERLIST:" + joined);
        }

        /**
         * Handles private messages in the format "recipient: message".
         * We also ensure that both directions of friendship exist (sender->recipient and recipient->sender).
         */
        private void handlePrivateMessage(String rawMessage) throws SQLException, IOException {
            if (rawMessage.startsWith("PHOTO:")) {
                // Handle incoming photo
                String[] parts = rawMessage.split(":", 3);
                String recipient = parts[1];
                int fileSize = Integer.parseInt(parts[2]);
                // Read the photo bytes from the socket
                byte[] fileBytes = new byte[fileSize];
                InputStream inputStream = clientSocket.getInputStream();
                inputStream.read(fileBytes, 0, fileSize);

                Socket recipientSocket;
                synchronized (clients) {
                    recipientSocket = clients.get(recipient);
                }
                if (recipientSocket != null) {
                    PrintWriter recipientOut = new PrintWriter(recipientSocket.getOutputStream(), true);
                    recipientOut.println("PHOTO:" + clientId + ":" + fileSize);
                    OutputStream recipientOutputStream = recipientSocket.getOutputStream();
                    recipientOutputStream.write(fileBytes);
                    recipientOutputStream.flush();
                } else {

                    storeOfflinePhotoInDB(clientId, recipient, fileBytes);
                    out.println("User " + recipient + " is offline. Photo stored.");
                }
            } else {


                String[] parts = rawMessage.split(":", 2);
                if (parts.length == 2) {
                    String recipient = parts[0].trim();
                    String content = parts[1].trim();

                    // Insert both directions in the friends table
                    ensureFriendship(clientId, recipient); // e.g. Bob -> David
                    ensureFriendship(recipient, clientId); // e.g. David -> Bob

                    // Forward the message if recipient is online
                    Socket recipientSocket;
                    synchronized (clients) {
                        recipientSocket = clients.get(recipient);
                    }
                    if (recipientSocket != null) {
                        PrintWriter recipientOut = new PrintWriter(recipientSocket.getOutputStream(), true);
                        recipientOut.println(clientId + ": " + content);
                    } else {
                        // Otherwise, store as offline
                        storeOfflineMessageInDB(clientId, recipient, content);
                        out.println("User " + recipient + " is offline. Message stored.");
                    }
                } else {
                    out.println("Invalid message format. Use 'recipient:message'.");
                }
            }

        }



        private void storeOfflinePhotoInDB(String sender, String receiver, byte[] fileBytes) {
            try {
                String insertQuery = """
            INSERT INTO photos (sender_id, receiver_id, photo_data, created_at)
            VALUES (
                (SELECT user_id FROM users WHERE username = ?),
                (SELECT user_id FROM users WHERE username = ?),
                ?,
                NOW()
            )
        """;
                PreparedStatement stmt = connection.prepareStatement(insertQuery);
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                stmt.setBytes(3, fileBytes);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        /**
         * Inserts a row in the 'friends' table if it doesn't already exist.
         * This is crucial to avoid duplicates. We use INSERT IGNORE (MySQL).
         */
        private void ensureFriendship(String user, String friend) throws SQLException {
            String insertQuery = """
                INSERT IGNORE INTO friends (user_id, friend_id)
                VALUES (
                    (SELECT user_id FROM users WHERE username = ?),
                    (SELECT user_id FROM users WHERE username = ?)
                )
            """;
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setString(1, user);
                stmt.setString(2, friend);
                stmt.executeUpdate();
            }
        }

        /**
         * Inserts an offline private message into the database.
         */
        private void storeOfflineMessageInDB(String sender, String receiver, String content) {
            try {
                String insertQuery = """
                    INSERT INTO messages (sender_id, receiver_id, content, status, created_at)
                    VALUES (
                        (SELECT user_id FROM users WHERE username = ?),
                        (SELECT user_id FROM users WHERE username = ?),
                        ?,
                        'offline',
                        NOW()
                    )
                """;
                PreparedStatement stmt = connection.prepareStatement(insertQuery);
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                stmt.setString(3, content);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
