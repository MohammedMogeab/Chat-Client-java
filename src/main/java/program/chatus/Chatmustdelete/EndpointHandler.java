package program.chatus.Chatmustdelete;

import jakarta.websocket.*;

import java.io.IOException;

@ClientEndpoint
public class EndpointHandler {
    private Session session;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("Connected to WebSocket");
        this.session = session;

        String connectFrame = "CONNECT\naccept-version:1.2\nhost:localhost\n\n\u0000";
        session.getBasicRemote().sendText(connectFrame);
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket Error: " + throwable.getMessage());
    }

    public void sendMessage(String destination, String payload) throws IOException {
        String frame = "SEND\ndestination:" + destination + "\ncontent-type:application/json\n\n" + payload + "\u0000";
        session.getBasicRemote().sendText(frame);
    }

    public void subscribe(String destination) throws IOException {
        String frame = "SUBSCRIBE\nid:sub-0\ndestination:" + destination + "\n\n\u0000";
        session.getBasicRemote().sendText(frame);
    }
}
