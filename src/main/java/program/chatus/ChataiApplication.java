package program.chatus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "program.chatus")
public class ChataiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChataiApplication.class, args);
	}
}