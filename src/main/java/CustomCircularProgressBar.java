import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class CustomCircularProgressBar extends Application {
    private double progress = 0.0;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(300, 300);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Animation Timer to update the progress
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawProgress(gc);

                // Increment progress
                progress += 0.01;
                if (progress > 1.0) {
                    progress = 1.0;
                    stop();
                }
            }
        };
        timer.start();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, 300, 300);

        primaryStage.setTitle("Custom Circular Progress");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawProgress(GraphicsContext gc) {
        double centerX = gc.getCanvas().getWidth() / 2;
        double centerY = gc.getCanvas().getHeight() / 2;
        double radius = 100;
        double lineWidth = 15;

        // Clear the canvas
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // Draw Background Circle
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(lineWidth);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw Progress Arc
        gc.setStroke(Color.DODGERBLUE);
        gc.setLineWidth(lineWidth);
        gc.strokeArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                90, -progress * 360, javafx.scene.shape.ArcType.OPEN);

        // Draw Percentage Text in the center
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(30));
        gc.fillText(Math.round(progress * 100) + "%", centerX - 30, centerY + 10);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
