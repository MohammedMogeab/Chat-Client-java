package program.chatus.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 1234;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Create a new thread to handle this client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                dataIn = new DataInputStream(clientSocket.getInputStream());
                dataOut = new DataOutputStream(clientSocket.getOutputStream());

                // Get username from client
                username = dataIn.readUTF();
                clients.put(username, this);
                System.out.println("User registered: " + username);

                // Send updated friend list to all clients
                broadcastFriendList();

                // Handle client messages
                while (true) {
                    String type = dataIn.readUTF();

                    if ("TEXT".equals(type)) {
                        String message = dataIn.readUTF();
                        handleTextMessage(message);
                    } else if ("PHOTO".equals(type)) {
                        String recipient = dataIn.readUTF();
                        String fileName = dataIn.readUTF();
                        int fileLength = dataIn.readInt();
                        byte[] fileBytes = new byte[fileLength];
                        dataIn.readFully(fileBytes);
                        handlePhotoMessage(recipient, fileName, fileBytes);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client " + username + ": " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void handleTextMessage(String message) {
            String[] parts = message.split(":", 2);
            if (parts.length == 2) {
                String recipient = parts[0];
                String content = parts[1];
                
                ClientHandler recipientHandler = clients.get(recipient);
                if (recipientHandler != null) {
                    try {
                        recipientHandler.dataOut.writeUTF("TEXT");
                        recipientHandler.dataOut.writeUTF(username + ":" + content);
                        recipientHandler.dataOut.flush();
                    } catch (IOException e) {
                        System.err.println("Error sending message to " + recipient + ": " + e.getMessage());
                    }
                }
            }
        }

        private void handlePhotoMessage(String recipient, String fileName, byte[] fileBytes) {
            ClientHandler recipientHandler = clients.get(recipient);
            if (recipientHandler != null) {
                try {
                    recipientHandler.dataOut.writeUTF("PHOTO");
                    recipientHandler.dataOut.writeUTF(username);
                    recipientHandler.dataOut.writeUTF(fileName);
                    recipientHandler.dataOut.writeInt(fileBytes.length);
                    recipientHandler.dataOut.write(fileBytes);
                    recipientHandler.dataOut.flush();
                } catch (IOException e) {
                    System.err.println("Error sending photo to " + recipient + ": " + e.getMessage());
                }
            }
        }

        private void broadcastFriendList() {
            String friendList = String.join(",", clients.keySet());
            for (ClientHandler client : clients.values()) {
                try {
                    client.dataOut.writeUTF("TEXT");
                    client.dataOut.writeUTF("/listFriends:" + friendList);
                    client.dataOut.flush();
                } catch (IOException e) {
                    System.err.println("Error broadcasting friend list: " + e.getMessage());
                }
            }
        }

        private void cleanup() {
            try {
                if (username != null) {
                    clients.remove(username);
                    broadcastFriendList();
                }
                if (dataIn != null) dataIn.close();
                if (dataOut != null) dataOut.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
} 