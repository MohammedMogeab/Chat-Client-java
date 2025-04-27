package program.chatus;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleStompClient {

    public static void main(String[] args) {
        try {
            new SimpleStompClient().connectToStompServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectToStompServer() throws IOException {
        // Connect to your backend WebSocket endpoint (usually behind a WebSocket proxy)
        Socket socket = new Socket("44.203.3.137", 8080); // Replace with your server's host and port

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send STOMP CONNECT frame
        writer.write("CONNECT\n");
        writer.write("accept-version:1.1,1.2\n");
        writer.write("heart-beat:10000,10000\n");
        writer.write("\n");
        writer.write("\u0000"); // End of frame
        writer.flush();

        // Listen for the CONNECTED response
        StringBuilder frame = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == 0) break; // End of STOMP frame
            frame.append((char) ch);
        }

        String response = frame.toString();
        if (response.startsWith("CONNECTED")) {
            System.out.println("✅ Connected to STOMP server!");
            System.out.println("🔽 Server Response:\n" + response);
        } else {
            System.out.println("❌ Failed to connect to STOMP server.");
            System.out.println("🔽 Server Response:\n" + response);
        }

        // Keep socket open to continue communication or close it
        // socket.close(); // Uncomment if you want to exit after connect
    }
}
