package program.chatus.Chatmustdelete;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChatListFetcher {

    public static String getFriendList(String username, String token) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8087/api/chat/list/" + username;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body(); // JSON response as string
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String username = "mogeab";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb2dlYWIiLCJpYXQiOjE3NDU1MTQ3ODIsImV4cCI6MTc0NTUxNjU4Mn0.hbBjRNNE5-ROgf3s3KFhC_op_umsGcLmilGuoA5qxjs"; // Replace with real token

        String response = getFriendList(username, token);
        System.out.println("🧑‍🤝‍🧑 Friend list response: " + response);
    }
}
