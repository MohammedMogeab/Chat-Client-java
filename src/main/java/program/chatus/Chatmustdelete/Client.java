package program.chatus.Chatmustdelete;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class Client {

    private final String token;
    private Session session;
    private MessageListener messageListener;

    public Client(String token) {
        this.token = token;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new ClientEndpointConfig.Configurator() {
                        @Override
                        public void beforeRequest(Map<String, List<String>> headers) {
                            headers.put("Authorization", List.of("Bearer " + token));
                            System.out.println("Added Authorization header with Bearer token");
                        }
                    })
                    .build();

            URI uri = URI.create("ws://localhost:8087/ws-native?token=" + token);

            container.connectToServer(new MyEndpoint(), config, uri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyEndpoint extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            System.out.println("Connected to WebSocket");
            Client.this.session = session;

            try {
                String connectFrame = "CONNECT\naccept-version:1.2\nhost:localhost\n\n\u0000";
                session.getBasicRemote().sendText(connectFrame);
            } catch (IOException e) {
                e.printStackTrace();
            }

            session.addMessageHandler(String.class, message -> {
                System.out.println("Received: " + message);

                // ✅ Extract the body only (after headers)
                String[] parts = message.split("\n\n", 2);
                if (parts.length < 2) {
                    System.err.println("⚠️ Malformed STOMP frame (no body): " + message);
                    return;
                }

                String body = parts[1].trim(); // This is the JSON payload

                if (messageListener != null) {
                    messageListener.onMessage(body); // 👈 Only pass the JSON
                }
            });


        }

        @Override
        public void onError(Session session, Throwable thr) {
            System.err.println("WebSocket Error: " + thr.getMessage());
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            System.out.println("Connection closed: " + closeReason);
        }
    }

    public void sendMessage(String destination, String payload) throws IOException {
        if (session != null) {
            String frame = "SEND\ndestination:" + destination + "\ncontent-type:application/json\n\n" + payload + "\u0000";
            session.getBasicRemote().sendText(frame);
        } else {
            System.err.println("Session is null — can't send message");
        }
    }


    public interface MessageListener {
        void onMessage(String message);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }


    public void subscribe(String destination) throws IOException {
        if (session != null) {
            String frame = "SUBSCRIBE\nid:sub-0\ndestination:" + destination + "\n\n\u0000";
            session.getBasicRemote().sendText(frame);
        } else {
            System.err.println("Session is null — can't subscribe");
        }
    }
}
