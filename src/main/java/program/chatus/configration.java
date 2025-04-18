package program.chatus;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class configration {
    @Bean
    public ChatClient chatclient(ChatClient.Builder chatcleint){
     return chatcleint.build();
    }
}
