package program.chatus;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ChatServer: listens on port 1234, accepts client connections,
 * and handles incoming commands/messages using DataInputStream/DataOutputStream.
 */
public class ChatServer {
    private static final int PORT = 1234;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    // Map of (username -> ClientHandler) for all connected (online) clients
    private static final HashMap<String, ClientHandler> clients = new HashMap<>();

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
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private String username;
        private Connection connection;

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
                dataIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                dataOut = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

                // First message from client is the username
                username = dataIn.readUTF();
                System.out.println("Client " + username + " connected.");

                // Add the client to the "online" map
                synchronized (clients) {
                    clients.put(username, this);
                }

                // Send updated friend list to all clients
                broadcastFriendList();

                // Continuously read messages from the client
                while (true) {
                    String type = dataIn.readUTF();
                    String sender = dataIn.readUTF();
                    String receiver = dataIn.readUTF();
                    String ImageName=dataIn.readUTF();
                    int length = dataIn.readInt();

                    byte[] data = new byte[length];
                    dataIn.readFully(data);

                    switch (type) {
                        case "TEXT":
                            handleTextMessage(sender, receiver, data);
                            break;
                        case "IMAGE":
                            handleImageMessage(sender, receiver, ImageName,data);
                            break;
                        case "VIDEO":
                            handleVideoMessage(sender, receiver, data);
                            break;
                        case "FILE":
                            handleFileMessage(sender, receiver, data);
                            break;
                    }
                }
            } catch (IOException | SQLException e) {
                System.out.println("Client " + username + " disconnected.");
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        private void handleTextMessage(String sender, String receiver, byte[] data) throws SQLException {
            String message = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            
            // Ensure friendship exists
            ensureFriendship(sender, receiver);
            ensureFriendship(receiver, sender);

            // Forward message if recipient is online
            ClientHandler recipientHandler = clients.get(receiver);
            if (recipientHandler != null) {
                try {
                    recipientHandler.dataOut.writeUTF("TEXT");
                    recipientHandler.dataOut.writeUTF(sender);
                    recipientHandler.dataOut.writeUTF(receiver);
                    recipientHandler.dataOut.writeInt(data.length);
                    recipientHandler.dataOut.write(data);
                    recipientHandler.dataOut.flush();
                } catch (IOException e) {
                    System.err.println("Error sending message to " + receiver + ": " + e.getMessage());
                }
            } else {
                storeOfflineMessageInDB(sender, receiver, message);
            }
        }

        private void handleImageMessage(String sender, String receiver,String ImageName, byte[] data) throws SQLException, IOException {
            // Validate file size (max 10MB)
            if (data.length > 10 * 1024 * 1024) {
                try {
                    dataOut.writeUTF("ERROR");
                    dataOut.writeUTF("Image size exceeds 10MB limit");
                    dataOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    return;
                }

            // Forward image if recipient is online
            ClientHandler recipientHandler = clients.get(receiver);
            if (recipientHandler != null) {
                try {
                    recipientHandler.dataOut.writeUTF("IMAGE");
                    recipientHandler.dataOut.writeUTF(sender);
                    recipientHandler.dataOut.writeUTF(receiver);
                    recipientHandler.dataOut.writeInt(data.length);
                    recipientHandler.dataOut.writeUTF(ImageName);
                    recipientHandler.dataOut.write(data);
                    recipientHandler.dataOut.flush();

                    // Save image to server storage
                    Path foldersPath = Paths.get("photosChat");
                    if (!Files.exists(foldersPath)) {
                        Files.createDirectories(foldersPath);
                    }
                    String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
                    Path filePath = foldersPath.resolve(ImageName);
                    Files.write(filePath, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                // Save image to server storage
                Path foldersPath = Paths.get("photosChat");
                if (!Files.exists(foldersPath)) {
                    Files.createDirectories(foldersPath);
                }
                String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
                Path filePath = foldersPath.resolve(ImageName);
                Files.write(filePath, data);
                storeOfflineImageInDB(sender, receiver, fileName);
            }
        }

        private void handleVideoMessage(String sender, String receiver, byte[] data) throws SQLException {
                // Validate file size (max 100MB)
            if (data.length > 100 * 1024 * 1024) {
                try {
                    dataOut.writeUTF("ERROR");
                    dataOut.writeUTF("Video size exceeds 100MB limit");
                    dataOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // Forward video if recipient is online
            ClientHandler recipientHandler = clients.get(receiver);
            if (recipientHandler != null) {
                try {
                    recipientHandler.dataOut.writeUTF("VIDEO");
                    recipientHandler.dataOut.writeUTF(sender);
                    recipientHandler.dataOut.writeUTF(receiver);
                    recipientHandler.dataOut.writeInt(data.length);
                    recipientHandler.dataOut.write(data);
                    recipientHandler.dataOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                } else {
                storeOfflineVideoInDB(sender, receiver, data);
            }
        }

        private void handleFileMessage(String sender, String receiver, byte[] data) throws SQLException {
                // Validate file size (max 50MB)
            if (data.length > 50 * 1024 * 1024) {
                try {
                    dataOut.writeUTF("ERROR");
                    dataOut.writeUTF("File size exceeds 50MB limit");
                    dataOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    return;
                }

            // Forward file if recipient is online
            ClientHandler recipientHandler = clients.get(receiver);
            if (recipientHandler != null) {
                try {
                    recipientHandler.dataOut.writeUTF("FILE");
                    recipientHandler.dataOut.writeUTF(sender);
                    recipientHandler.dataOut.writeUTF(receiver);
                    recipientHandler.dataOut.writeInt(data.length);
                    recipientHandler.dataOut.write(data);
                    recipientHandler.dataOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                } else {
                storeOfflineFileInDB(sender, receiver, data);
            }
        }

        private void broadcastFriendList() {
            String friendList = String.join(",", clients.keySet());
            for (ClientHandler client : clients.values()) {
                try {
                    client.dataOut.writeUTF("TEXT");
                    client.dataOut.writeUTF("SERVER");
                    client.dataOut.writeUTF("ALL");
                    byte[] data = ("/listFriends:" + friendList).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    client.dataOut.writeInt(data.length);
                    client.dataOut.write(data);
                    client.dataOut.flush();
                } catch (IOException e) {
                    System.err.println("Error broadcasting friend list: " + e.getMessage());
                }
            }
        }

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

        private void storeOfflineImageInDB(String sender, String receiver,String imageData) {
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
                stmt.setString(3, imageData);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void storeOfflineVideoInDB(String sender, String receiver, byte[] videoData) {
            try {
                String insertQuery = """
                    INSERT INTO messages (sender_id, receiver_id, content, message, status, created_at)
                    VALUES (
                        (SELECT user_id FROM users WHERE username = ?),
                        (SELECT user_id FROM users WHERE username = ?),
                        'Video message',
                        ?,
                        'offline',
                        NOW()
                    )
                """;
                PreparedStatement stmt = connection.prepareStatement(insertQuery);
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                stmt.setBytes(3, videoData);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void storeOfflineFileInDB(String sender, String receiver, byte[] fileData) {
            try {
                String insertQuery = """
                    INSERT INTO messages (sender_id, receiver_id, content, message, status, created_at)
                    VALUES (
                        (SELECT user_id FROM users WHERE username = ?),
                        (SELECT user_id FROM users WHERE username = ?),
                        'File message',
                        ?,
                        'offline',
                        NOW()
                    )
                """;
                PreparedStatement stmt = connection.prepareStatement(insertQuery);
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                stmt.setBytes(3, fileData);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void cleanup() {
            try {
                if (username != null) {
                    synchronized (clients) {
                        clients.remove(username);
                    }
                    broadcastFriendList();
                }
                if (dataIn != null) dataIn.close();
                if (dataOut != null) dataOut.close();
                if (clientSocket != null) clientSocket.close();
                if (connection != null) connection.close();
            } catch (IOException | SQLException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}
