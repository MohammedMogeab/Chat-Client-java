package program.chatus.Chattix.UI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import program.chatus.ChatController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.sql.SQLException;

import static javafx.application.Application.launch;
@Controller
public class LoginUI {
    @FXML
    private TextField email_login;

    @FXML
    private PasswordField email_password;

    @FXML
    private Button login_bun;

    @FXML
    private Button login_dont_have_acc;


    @FXML
    public void initialize() {
        // Set up event handlers
        login_bun.setOnAction(event -> {
            try {
                handleLogin();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        login_dont_have_acc.setOnAction(event -> {
            try {
                handleCreateAccount();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private void handleLogin() throws SQLException, IOException {
        String email = email_login.getText().trim();
        String password = email_password.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Please enter both email and password.🔒🔑");
            return;
        }
        String JsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", email, password);
        System.out.println(JsonBody);
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8087/login").openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.getOutputStream().write(JsonBody.getBytes());


        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            redirectToChatWindow();
        } else {
            System.out.println("Login failed." + responseCode);
        }

    }


    private void handleCreateAccount() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Sign_In.fxml"));
        Parent root = loader.load();

        // Pass the logged-in user to the ChatController
//        ChatController chatController = loader.getController();

        // Set up the scene and stage
        Stage stage = (Stage) login_dont_have_acc.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Chat Window");
        stage.show();

    }


    public void redirectToChatWindow() {
        try {
            URL fxmlResource = getClass().getResource("/program/chatus/Chattix/hello-view.fxml");

            if (fxmlResource == null) {
                System.err.println("❌ FXML file not found at /program/chatus/Chattix/hello-view.fxml");
                return;
            } else {
                System.out.println("✅ Found FXML at: " + fxmlResource);
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat Window");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

