package program.chatus;

import jakarta.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketClient {

    private Session session;

    public void connect(String uri) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(uri));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("🔌 Connected to server");

        // Send STOMP CONNECT frame
        String connectFrame = "CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000";
        session.getAsyncRemote().sendText(connectFrame);
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("📩 Received: " + message);
        // Handle STOMP MESSAGE/CONNECTED/etc.
    }

    public void sendMessage(String destination, String payload) {
        String sendFrame = "SEND\ndestination:" + destination + "\ncontent-type:text/plain\n\n" + payload + "\u0000";
        session.getAsyncRemote().sendText(sendFrame);
    }
}
