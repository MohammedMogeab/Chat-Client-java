package program.chatus.Chatmustdelete;

import program.chatus.Chatmustdelete.Client;

public class Main {
    public static void main(String[] args) {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb2dlYWIiLCJpYXQiOjE3NDU1MDczMTksImV4cCI6MTc0NTUwOTExOX0.-rn6Kb_hKAVitU0oZsoLK46zuUhOT-2mcruRDNUyo-Y";
        Client client = new Client(token);
        client.connect();

        try {
            Thread.sleep(3000);
            client.subscribe("/user/mogeab/queue/messages");

            String recipient = "soltan";
            String payload = String.format("""
{
  "senderUsername": "mogeab",
  "recipientUsername": "%s",
  "content": "Hello! This is from JavaFX",
  "timestamp": "%s"
}
""", recipient, java.time.Instant.now().toString());

            client.sendMessage("/app/sendPrivateMessage", payload);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
