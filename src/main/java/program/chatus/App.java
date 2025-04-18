package program.chatus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

public class App extends Application {
    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        // Initialize the Spring context
        springContext = new SpringApplicationBuilder(ChataiApplication.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Use Spring to load the FXML and controller
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("Login.fxml")));
        loader.setControllerFactory(springContext::getBean); // Use Spring to instantiate the controller
        Parent parent = loader.load();

        Scene scene = new Scene(parent);
        stage.setTitle("Ai assistant");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Close the Spring context when the application stops
        springContext.close();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(App.class, args);
    }
}