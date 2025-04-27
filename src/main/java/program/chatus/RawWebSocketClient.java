package program.chatus;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class RawWebSocketClient {

    private Session session;

    public static void main(String[] args) {
        new RawWebSocketClient().start();
    }

    public void start() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("ws://44.203.3.137:8080/ws")); // Your Spring Boot websocket endpoint
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("✅ Connected to server");
        this.session = session;

        // Send STOMP CONNECT frame manually
        String connectFrame = "CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000";
        session.getAsyncRemote().sendText(connectFrame);
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("📨 Message from server:\n" + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("❌ Connection closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("⚠️ Error: " + throwable.getMessage());
    }
}
