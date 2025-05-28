package program.chatus;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import program.chatus.Model.UserSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;

import static javafx.application.Application.launch;
@Controller
public class LoginController {
    @FXML
    private TextField email_login;

    @FXML
    private PasswordField email_password;

    @FXML
    private Button login_bun;

    @FXML
    private Button login_dont_have_acc;

    private UserDetails userDAO = new UserDetails();

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



        if (email.isEmpty()) {
            System.out.println("Please enter both email and password.");
            return;
        }

        if (password.isEmpty()) {
            System.out.println("Please enter both email and password.");
        }




        // Authenticate the user
        String user = userDAO.loginUser(email);
        System.out.println(user);
        if (user != null) {
            UserSession.getInstance().setUsername(user);
            System.out.println("Login successful! Redirecting to chat window...");
//            connectusertoserversocket(user);
            redirectToChatWindow();


    } else {
            System.out.println("Invalid email or password.");
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

//    private void connectusertoserversocket(String user) throws IOException {
//        Socket socket = SocketClients.getSocket();
//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//        out.println(user);
//        // after this hhhhh the serversocket will what? will make will....
//
//
//    }

    private void redirectToChatWindow() {
        try {
            // Load the chat window FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            // Pass the logged-in user to the ChatController
            ChatController chatController = loader.getController();




            // Set up the scene and stage
            Stage stage = (Stage) login_bun.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat Window");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}