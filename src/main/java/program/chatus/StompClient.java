package program.chatus;

import io.netty.handler.codec.DefaultHeaders;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public class StompClient {

    private final WebSocketClient client = new WebSocketClient();
    private Session session;

    public void connect(String username) throws Exception {
        client.start();

        URI uri = new URI("ws://localhost:8080/ws"); // Your Spring Boot WebSocket endpoint

        Future<Session> fut = client.connect(new WebSocketAdapter() {
            @Override
            public void onWebSocketConnect(Session sess) {
                super.onWebSocketConnect(sess);
                session = sess;
                System.out.println("✅ WebSocket connected");

                // Send CONNECT STOMP frame
                sendFrame("""
                    CONNECT
                    accept-version:1.2
                    heart-beat:10000,10000

                    \0
                    """);

                // Subscribe to user's queue
                sendFrame("""
                    SUBSCRIBE
                    id:sub-1
                    destination:/user/%s/queue/messages

                    \0
                    """.formatted(username));
            }

            @Override
            public void onWebSocketText(String message) {
                System.out.println("📥 Received: " + message);
                // Handle incoming message here (update JavaFX UI)
            }
        }, uri);

        session = fut.get();
    }

    public void sendPrivateMessage(String from, String to, String content) {
        String json = """
            {
              "senderUsername": "%s",
              "recipientUsername": "%s",
              "content": "%s"
            }
            """.formatted(from, to, content);

        String stompFrame = """
            SEND
            destination:/app/sendPrivateMessage
            content-type:application/json

            %s\0
            """.formatted(json);

        sendFrame(stompFrame);
    }

    private void sendFrame(String frame) {
        if (session != null && session.isOpen()) {
            session.getRemote().sendStringByFuture(frame);
        }
    }

    public void disconnect() throws Exception {
        if (session != null) {
            sendFrame("DISCONNECT\n\n\0");
            session.close();
        }
        client.stop();
    }


}
