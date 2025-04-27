package program.chatus.Chatmustdelete;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChatReceiverSwing {
    private JFrame frame;
    private JPanel chatPanel;
    private JScrollPane scrollPane;

    public ChatReceiverSwing() {
        frame = new JFrame("Chat Receiver - soltan");
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(true);
        chatPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(chatPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }


    public void addTextMessage(String message) {
        JLabel label = new JLabel(message);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        chatPanel.add(label);
        frame.revalidate();
    }

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvd2lzIiwiaWF0IjoxNzQ1NTMwMDkwLCJleHAiOjE3NDU1MzE4OTB9.GkHdUgikNJDGOYxvcIGA4HXNS7Vw6KbK8vS2a0q12L0"; // Replace with your valid JWT

    public void addImageMessage(String imageUrl) {
        System.out.println("🔄 addImageMessage called with: " + imageUrl);
        SwingUtilities.invokeLater(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(imageUrl))
                        .header("Authorization", "Bearer " + TOKEN) // 🔐 Auth header
                        .GET()
                        .build();

                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) {
                    addTextMessage("❌ Failed to fetch image (Status: " + response.statusCode() + ")");
                    return;
                }

                byte[] imageBytes = response.body();
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                BufferedImage bufferedImage = ImageIO.read(bais);

                if (bufferedImage == null) {
                    addTextMessage("❌ Could not decode image.");
                    return;
                }

                Image scaledImage = bufferedImage.getScaledInstance(300, -1, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));

                chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                chatPanel.add(imageLabel);
                chatPanel.revalidate();
                chatPanel.repaint();

                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());

            } catch (Exception e) {
                e.printStackTrace();
                addTextMessage("❌ Error loading secure image.");
            }
        });
    }


    public static String getFriendList(String username, String token) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8087/api/messages/private/" + username;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body(); // JSON response as string
    }
    public void loadHistory(String username, String token) {
        try {
            String json = getFriendList(username, token);
            System.out.println("Raw server response: " + json);
            JSONObject response = new JSONObject(json);
            JSONArray chats = response.getJSONArray("messages");

            for (int i = 0; i < chats.length(); i++) {
                JSONObject chat = chats.getJSONObject(i);
//                if (!chat.has("lastMessage")) continue;


//                JSONObject lastMessage = chat.getJSONObject("lastMessage");
                String content = chat.getString("content");
                System.out.println(content);
                String type = "text";

                // Try to infer type if not explicitly provided
                if (content.endsWith(".jpg") || content.endsWith(".png") || content.contains("/uploads/")) {
                    type = "image";
                }

                if ("image".equalsIgnoreCase(type)) {
                    addImageMessage(content);
                } else {
                    addTextMessage(content);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            addTextMessage("❌ Failed to load message history.");
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatReceiverSwing receiver = new ChatReceiverSwing();
            String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvd2lzIiwiaWF0IjoxNzQ1NTMwMDkwLCJleHAiOjE3NDU1MzE4OTB9.GkHdUgikNJDGOYxvcIGA4HXNS7Vw6KbK8vS2a0q12L0";

            Client client = new Client(token);
            client.connect();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                client.subscribe("/user/owis/queue/messages");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            receiver.loadHistory("owis",token);
            client.setMessageListener(message -> {
                JSONObject json = new JSONObject(message);
                String type = json.optString("type", "text");
                System.out.println(type);
                String content = json.getString("content");

                if ("image".equalsIgnoreCase(type)) {
                    SwingUtilities.invokeLater(() -> receiver.addImageMessage(content));
                } else {
                    SwingUtilities.invokeLater(() -> receiver.addTextMessage(content));
                }
            });

        });
    }
}
