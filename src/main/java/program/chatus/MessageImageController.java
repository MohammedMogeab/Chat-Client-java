package program.chatus;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MessageImageController {
    @FXML private HBox messageContainer;
    @FXML private StackPane imageContainer;
    @FXML private ImageView messageImageView;

    private String alignment;
    private String selectUserFriend;
    private CardUser cardUser;

    public void initialize() {
        // Set up hover effects
        messageImageView.setOnMouseEntered(e -> {
            messageImageView.setStyle("-fx-cursor: hand;");
            imageContainer.setEffect(new DropShadow(10, Color.BLACK));
        });

        messageImageView.setOnMouseExited(e -> {
            messageImageView.setStyle("");
            imageContainer.setEffect(null);
        });

        // Set up click handler
        messageImageView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                showImageInNewWindow();
            }
        });
    }

    public void setImage(File photo, String alignment, String selectUserFriend, CardUser cardUser) {
        this.alignment = alignment;
        this.selectUserFriend = selectUserFriend;
        this.cardUser = cardUser;

        try {
            // Load and set the image
            Image image = new Image(new FileInputStream(photo));
            messageImageView.setImage(image);

            // Set alignment
            messageContainer.setAlignment(alignment.equals("right") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            // Set background color based on alignment
            imageContainer.setStyle(
                "-fx-background-color: " + (alignment.equals("right") ? "#131313" : "#2bc723") + ";" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 5;"
            );

            // Set margins based on alignment
            if (alignment.equals("right")) {
                HBox.setMargin(messageContainer, new Insets(10, 10, 10, 250));
            } else {
                HBox.setMargin(messageContainer, new Insets(10, 250, 10, 10));
            }

            // Update last message in friend list
            if (cardUser != null) {
                Platform.runLater(() -> {
                    cardUser.setLastMessage("Photo");
                    cardUser.setLastMessageTime(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
                });
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showImageInNewWindow() {
        // Create a new stage for the image preview
        Stage imageStage = new Stage();
        imageStage.initModality(Modality.NONE);
        imageStage.initStyle(StageStyle.UNDECORATED);

        // Create a container for the image with a semi-transparent background
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");

        // Create a new ImageView for the preview
        ImageView previewImageView = new ImageView(messageImageView.getImage());
        
        // Set larger preview dimensions
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        
        // Set preview size to 80% of screen size while maintaining aspect ratio
        previewImageView.setFitWidth(screenWidth * 0.8);
        previewImageView.setFitHeight(screenHeight * 0.8);
        previewImageView.setPreserveRatio(true);
        previewImageView.setSmooth(true);

        // Add click handler to close the preview
        root.setOnMouseClicked(e -> imageStage.close());

        // Add escape key handler to close the preview
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                imageStage.close();
            }
        });

        // Add the image to the container
        root.getChildren().add(previewImageView);

        // Create and show the scene
        Scene scene = new Scene(root);
        imageStage.setScene(scene);
        imageStage.show();

        // Center the stage on screen
        imageStage.centerOnScreen();
    }

    public static MessageImageController create(File photo, String alignment, String selectUserFriend, CardUser cardUser) {
        try {
            FXMLLoader loader = new FXMLLoader(MessageImageController.class.getResource("MessageImage.fxml"));
            HBox root = loader.load();
            MessageImageController controller = loader.getController();
            controller.setImage(photo, alignment, selectUserFriend, cardUser);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HBox getRoot() {
        return messageContainer;
    }
} 