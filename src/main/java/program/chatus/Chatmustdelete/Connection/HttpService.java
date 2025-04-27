package program.chatus.Chatmustdelete.Connection;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class HttpService {
    private static final HttpClient client = HttpClient.newHttpClient();
   public  static String get(String url,String token) {
       HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
               .uri(URI.create(url))
               .GET();

       if (token != null && !token.isEmpty())
           requestBuilder.header("Authorization", "Bearer " + token);


       HttpRequest request = requestBuilder.build();
        http

       

   }


}
