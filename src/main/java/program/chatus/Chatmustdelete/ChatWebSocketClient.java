package program.chatus.Chatmustdelete;

import java.net.URI;
import javax.websocket.*;

@ClientEndpoint
public class ChatWebSocketClient {

    private Session session;

    public void connect(String uri) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to server");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Disconnected: " + reason);
    }

    public void send(String message) {
        session.getAsyncRemote().sendText(message);
    }
}
