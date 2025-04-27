package program.chatus;

public class Main {
    public static void main(String[] args) throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.connect("ws://localhost:8080/ws");

        // Wait a few seconds for connection
        Thread.sleep(3000);

        // Send a message
        client.sendMessage("/app/sendPrivateMessage", "{\"senderUsername\":\"alice\",\"recipientUsername\":\"bob\",\"content\":\"Hello\"}");
    }
}
