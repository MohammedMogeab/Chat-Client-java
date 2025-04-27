package program.chatus.Chattix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Run extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlResource = getClass().getResource("Login.fxml");
        if (fxmlResource == null) {
            throw new IllegalStateException("Login.fxml not found. Ensure the file is located in the same package as 'LoginApp.java'.");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 908, 670);
        primaryStage.setTitle("Chattix application");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
