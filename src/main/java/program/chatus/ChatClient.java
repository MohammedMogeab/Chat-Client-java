package program.chatus;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ChatClient {

    private String serverAddress;
    private int serverPort;
    private String username;

    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    private Thread listeningThread;

    public ChatClient(String serverAddress, int serverPort, String username) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.username = username;
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        startListening();
    }

    // Thread to listen to incoming messages continuously
    private void startListening() {
        listeningThread = new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    String type = dataIn.readUTF();
                    String sender = dataIn.readUTF();
                    String receiver = dataIn.readUTF();
                    int length = dataIn.readInt();

                    byte[] data = new byte[length];
                    dataIn.readFully(data);

                    switch (type) {
                        case "TEXT":
                            String msg = new String(data, StandardCharsets.UTF_8);
                            onTextReceived(sender, receiver, msg);
                            break;

                        case "IMAGE":
                            onImageReceived(sender, receiver, data);
                            break;

                        case "VIDEO":
                            onVideoReceived(sender, receiver, data);
                            break;

                        default:
                            System.err.println("Unknown type received: " + type);
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection closed or error occurred: " + e.getMessage());
            }
        });

        listeningThread.start();
    }

    // Send a text message
    public void sendText(String receiver, String message) throws IOException {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);

        dataOut.writeUTF("TEXT");
        dataOut.writeUTF(username);
        dataOut.writeUTF(receiver);
        dataOut.writeInt(msgBytes.length);
        dataOut.write(msgBytes);
        dataOut.flush();
    }

    // Send an image file
    public void sendImage(String receiver, File imageFile) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

        dataOut.writeUTF("IMAGE");
        dataOut.writeUTF(username);
        dataOut.writeUTF(receiver);
        dataOut.writeInt(imageBytes.length);
        dataOut.write(imageBytes);
        dataOut.flush();
    }

    // Send a video file
    public void sendVideo(String receiver, File videoFile) throws IOException {
        byte[] videoBytes = Files.readAllBytes(videoFile.toPath());

        dataOut.writeUTF("VIDEO");
        dataOut.writeUTF(username);
        dataOut.writeUTF(receiver);
        dataOut.writeInt(videoBytes.length);
        dataOut.write(videoBytes);
        dataOut.flush();
    }

    // Callbacks you can override or hook into

    protected void onTextReceived(String sender, String receiver, String message) {
        System.out.println("Text from " + sender + ": " + message);
        // You can update UI here or notify user
    }

    protected void onImageReceived(String sender, String receiver, byte[] imageData) {
        System.out.println("Image received from " + sender + " (" + imageData.length + " bytes)");
        // You can save file or display image in UI
    }

    protected void onVideoReceived(String sender, String receiver, byte[] videoData) {
        System.out.println("Video received from " + sender + " (" + videoData.length + " bytes)");
        // You can save file or play video in UI
    }

    public void close() throws IOException {
        if (listeningThread != null && listeningThread.isAlive()) {
            listeningThread.interrupt();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
