package program.chatus;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import program.chatus.Model.MessageDTO;

import java.io.IOException;

public class MessageContextMenuController {
    private static final String FXML_PATH = "MessageContextMenu.fxml";
    private static final String CSS_PATH = "MessageContextMenu.css";

    @FXML private VBox menuContainer;
    @FXML private Button replyButton;
    @FXML private Button forwardButton;
    @FXML private Button copyButton;
    @FXML private Button deleteButton;

    private Popup popup;
    private MessageDTO currentMessage;
    private Runnable onReply;
    private Runnable onForward;
    private Runnable onDelete;

    @FXML
    public void initialize() {
        popup = new Popup();
        popup.getContent().add(menuContainer);

        // Add hover effects
        addHoverEffect(replyButton);
        addHoverEffect(forwardButton);
        addHoverEffect(copyButton);
        addHoverEffect(deleteButton);

        // Set up button actions
        replyButton.setOnAction(e -> {
            if (onReply != null) onReply.run();
            popup.hide();
        });

        forwardButton.setOnAction(e -> {
            if (onForward != null) onForward.run();
            popup.hide();
        });

        copyButton.setOnAction(e -> {
            if (currentMessage != null) {
                ClipboardContent content = new ClipboardContent();
                content.putString(currentMessage.getContent());
                Clipboard.getSystemClipboard().setContent(content);
            }
            popup.hide();
        });

        deleteButton.setOnAction(e -> {
            if (onDelete != null) onDelete.run();
            popup.hide();
        });
    }

    private void addHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #f0f0f0;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: white;"));
    }

    private void applyStyles() {
        try {
            String cssPath = getClass().getResource(CSS_PATH).toExternalForm();
            if (cssPath != null && menuContainer != null) {
                menuContainer.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + e.getMessage());
        }
    }

    public void show(Window owner, double screenX, double screenY, MessageDTO message,
                    Runnable onReply, Runnable onForward, Runnable onDelete) {
        if (popup == null || menuContainer == null) {
            System.err.println("Context menu not properly initialized");
            return;
        }
        this.currentMessage = message;
        this.onReply = onReply;
        this.onForward = onForward;
        this.onDelete = onDelete;

        // Update button states based on message type
        boolean isMediaMessage = message.getContent().startsWith("PHOTO:") || 
                               message.getContent().startsWith("VIDEO:") || 
                               message.getContent().startsWith("FILE:");
        copyButton.setDisable(isMediaMessage);

        popup.show(owner, screenX, screenY);
    }

    public static MessageContextMenuController create() {
        try {
            FXMLLoader loader = new FXMLLoader(MessageContextMenuController.class.getResource("MessageContextMenu.fxml"));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
} 