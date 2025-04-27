//package program.chatus.Chatmustdelete;
//
//
//import io.netty.handler.codec.DefaultHeaders;
//import org.springframework.messaging.simp.stomp.StompSession;
//import program.chatus.StompClient;
//
//
//import static javax.swing.UIManager.put;
//
//
//public class ChatClient {
//
//    private StompSession session;
//
//    public void connect(String token) {
//        StompClient client = new StompClient();
//        client.set(new DefaultHeaders() {{
//            put("Authorization", "Bearer " + token);
//        }});
//
//        client.connectAsync("ws://localhost:8087/ws", new StompClient.ConnectionHandler() {
//            @Override
//            public void onConnected(StompSession stompSession) {
//                session = stompSession;
//                System.out.println("✅ Connected to WebSocket!");
//
//                // Subscribe to private messages
//                session.subscribe("/user/queue/messages", (msg) -> {
//                    System.out.println("📩 Private message: " + msg.getBody());
//                });
//
//                // Example group subscription
//                long groupId = 1L;
//                session.subscribe("/topic/group-" + groupId, (msg) -> {
//                    System.out.println("👥 Group message: " + msg.getBody());
//                });
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                t.printStackTrace();
//            }
//
//            @Override
//            public void onDisconnected() {
//                System.out.println("Disconnected");
//            }
//        });
//    }
//
//    public void sendPrivateMessage(MessageDTO messageDTO) {
//        if (session != null && session.isConnected()) {
//            session.send("/app/sendPrivateMessage", messageDTO);
//        } else {
//            System.out.println("Session not connected.");
//        }
//    }
//
//    public void sendGroupMessage(MessageDTO messageDTO) {
//        if (session != null && session.isConnected()) {
//            session.send("/app/sendGroupMessage", messageDTO);
//        } else {
//            System.out.println("Session not connected.");
//        }
//    }
//}
