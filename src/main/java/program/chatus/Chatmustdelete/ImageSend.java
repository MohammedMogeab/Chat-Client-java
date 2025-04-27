package program.chatus.Chatmustdelete;
import program.chatus.Chatmustdelete.Client;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.io.*;

public class ImageSend {
        public static void main(String[] args) {
            String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb2dlYWIiLCJpYXQiOjE3NDU1MzAyMzMsImV4cCI6MTc0NTUzMjAzM30.O7DN6MFDDOIIs0QpVCQhodrdFeU6rSC6l1KaGfgcCLM"; // replace with valid token
            Client client = new Client(token);
            client.connect();

            try {
                Thread.sleep(3000);
                client.subscribe("/user/mogeab/queue/messages");

                // 1. Upload the image via REST and get back the image URL
                String imagePath = "C:\\Users\\Lenovo\\OneDrive\\Desktop\\photo2.jpg"; // your local image path
                 if( !new File(imagePath).exists()){
                     System.out.println("File not found");
                     return;
                 }
                String imageUrl = uploadImage(imagePath, token);
                 if (imageUrl == null) {
                     return;
                 }

                // 2. Send image URL via WebSocket
                String recipient = "owis";
                String payload = String.format("""
{
  "senderUsername": "mogeab",
  "recipientUsername": "%s",
  "content": "%s",
  "type": "image",
  "timestamp": "%s"
}
""", recipient, imageUrl, java.time.Instant.now().toString());

                client.sendMessage("/app/sendPrivateImage", payload);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static String uploadImage(String path, String token) throws Exception {
            URL url = new URL("http://localhost:8087/api/upload"); // your backend endpoint
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                Files.copy(new File(path).toPath(), os);
            }

            try (InputStream is = conn.getInputStream()) {
                return new String(is.readAllBytes());
            }
        }
    }


