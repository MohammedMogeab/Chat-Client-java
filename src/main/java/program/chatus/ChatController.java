package program.chatus;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.stereotype.Controller;
import program.chatus.Model.ChatMessage;
import program.chatus.Model.UserSession;
import program.chatus.Util.ContextUtil;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ChatController is the JavaFX client. It connects to the server,
 * sends/receives messages, saves messages to the database, and
 * displays them in the UI.
 */
@Controller
public class ChatController extends Application implements Initializable {

    @FXML private TextField chat_Search;
    @FXML private Button chat_call;
    @FXML private ImageView chat_mic;
    @FXML private Button Sendbtn;
    @FXML private ImageView chat_send;
    @FXML private ImageView chat_shre;
    @FXML private VBox vdatal1;
    @FXML private VBox vdatal2;
    @FXML private TextField chat_text_send;
    @FXML private ImageView imgporfile;
    @FXML private VBox vboxChat;
    @FXML private Label LabelChatLeft;
    @FXML private Label LabelChatRight;
    @FXML private VBox VboxLeftSide;
    @FXML private HBox HboxInfo;
    @FXML private VBox vboxUserInfo;
    @FXML private Label chat_name;
    @FXML private VBox profilePage;
    @FXML private StackPane profilePages;
    @FXML private ImageView ImgAi;
    @FXML private VBox pageGroup;
    @FXML private VBox aichat;
    @FXML private VBox normlchat;
    @FXML private Button sendbtnAi;
    @FXML private TextField chat_text_send1;
    @FXML private TextArea textArearesponse;
    @FXML private VBox chatMessageContainer;






    // Server connection variables
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 1234;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);  // Adjust as needed

    // The current conversation recipient
    private String currentRecipient = null;
    // The logged-in user from session
    private String username = UserSession.getInstance().getUsername();

    int as = 100;


    @FXML
    private  void StartChatAi(){

    }

    @FXML
    private void choosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            sendPhoto(selectedFile);
        }
    }


    private void sendPhoto(File photoFile) {
        try (FileInputStream fileInputStream = new FileInputStream(photoFile)) {
            // Read the file into a byte array
            byte[] fileBytes = fileInputStream.readAllBytes();

            // Send a header to indicate this is a photo
            String header = "PHOTO:" + photoFile.getName() + ":" + fileBytes.length + "\n";
            out.println(header);

            // Send the file bytes
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(fileBytes);
            outputStream.flush();

            System.out.println("Photo sent: " + photoFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    @FXML
    void newgroup(ActionEvent event) {
        pageGroup.toFront(); // جلب الصفحة إلى الواجهة
        pageGroup.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pageGroup);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

    }

    @FXML
    void exit() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), profilePage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            profilePage.setVisible(false);
            profilePage.toBack(); // إرجاع الصفحة إلى الخلف بعد الاختفاء
        });
        fadeOut.play();
    }

    @FXML
    void cancel() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), pageGroup);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            pageGroup.setVisible(false);
            pageGroup.toBack(); // إرجاع الصفحة إلى الخلف بعد الاختفاء
        });
        fadeOut.play();
    }

    @FXML
    void creatgroup() {



    }

    @FXML
    void aichatclick(ActionEvent event) {
        aichat.toFront(); // جلب الصفحة إلى الواجهة
        aichat.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(0), aichat);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

    }

    @FXML
    void backTonormalchat() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(0), normlchat);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            aichat.setVisible(false);
            // إرجاع الصفحة إلى الخلف بعد الاختفاء
        });
        fadeOut.play();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // On clicking "Send" button
        Sendbtn.setOnAction(e -> sendMessage());

        // Optionally, watch search text changes
        chat_Search.textProperty().addListener((obs, oldVal, newVal) -> {
            // Filter or update friend list if needed
        });

        // Connect to server on a background thread
        threadPool.execute(this::connectToServer);
    }

    @FXML private ScrollPane chatScrollPane;

    private void addMessage(String message, boolean isUserMessage) {
        // Create a label for the message
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true); // Enable text wrapping
        messageLabel.setMaxWidth(300); // Set a max width for the message bubble
        messageLabel.setPadding(new Insets(10)); // Add padding
        messageLabel.setStyle(
                "-fx-background-color: " + (isUserMessage ? "#0078d7" : "#e1e1e1") + ";" + // Different colors for user and AI messages
                        "-fx-background-radius: 10;" + // Rounded corners
                        "-fx-text-fill: " + (isUserMessage ? "white" : "black") + ";" // Text color
        );

        // Create an HBox to hold the message label
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new Insets(5)); // Add padding around the message
        messageContainer.setAlignment(isUserMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT); // Align the HBox

        // Add the label to the HBox
        messageContainer.getChildren().add(messageLabel);

        // Add the HBox to the chat container
        chatMessageContainer.getChildren().add(messageContainer);

        // Scroll to the bottom of the chat

    }





    private StringBuilder aiResponse; // Stores the AI's response
    private Label aiMessageLabel; // Label for the AI's message
    @FXML
    public void sendMessageAi() {
        var llmInput = chat_text_send1.getText();
        if (llmInput == null || llmInput.isEmpty()) {
            return;
        }

        // Add the user's message to the chat immediately
        addMessage(llmInput, true); // true = user message

        chat_text_send1.setText(""); // Clear the input field

        // Create and show progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setMaxSize(50, 50);

        // Create a transparent overlay to cover the entire screen
        StackPane overlay = new StackPane(progressIndicator);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);"); // Transparent background (no blur)
        overlay.setAlignment(Pos.CENTER); // Center the progress indicator

        // Add the overlay to the root container (e.g., StackPane or BorderPane)
        StackPane rootContainer = (StackPane) chatMessageContainer.getScene().getRoot(); // Assuming the root is a StackPane
        rootContainer.getChildren().add(overlay);

        // Create a label for the AI's response
        Label aiMessageLabel = new Label();
        aiMessageLabel.setWrapText(true); // Enable text wrapping
        aiMessageLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(aiMessageLabel.getText());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                clipboard.setContent(clipboardContent);

                chat_text_send1.setText( clipboard.getString());


            }
        }); // Set a max width for the message bubble
        aiMessageLabel.setPadding(new Insets(10)); // Add padding
        aiMessageLabel.setStyle(
                "-fx-background-color: #e1e1e1;" + // AI message color
                        "-fx-background-radius: 10;" + // Rounded corners
                        "-fx-text-fill: black;" // Text color
        );

        // Create an HBox to hold the AI's message label
        HBox aiMessageContainer = new HBox();
        aiMessageContainer.setPadding(new Insets(5)); // Add padding around the message
        aiMessageContainer.setAlignment(Pos.CENTER_LEFT); // Align the HBox to the left

        // Add the label to the HBox
        aiMessageContainer.getChildren().add(aiMessageLabel);

        // Add the HBox to the chat container
        chatMessageContainer.getChildren().add(aiMessageContainer);

        // Scroll to the bottom of the chat


        // Start the AI response process
        var chatClient = ContextUtil.getContext().getBean(ChatClient.class);
        var llrespons = chatClient.prompt().user(llmInput).stream()
                .content()
                .subscribe(
                        token -> Platform.runLater(() -> {
                            if (rootContainer.getChildren().contains(overlay)) {
                                rootContainer.getChildren().remove(overlay);
                            }
                            // Append the token to the AI's response
                            aiMessageLabel.setText(aiMessageLabel.getText() + token);

                            // Scroll to the bottom of the chat

                        }),
                        error -> Platform.runLater(() -> {
                            // Handle errors
                            addMessage("Error: " + error.getMessage(), false); // false = AI message

                            // Remove the progress indicator on error
                            if (rootContainer.getChildren().contains(overlay)) {
                                rootContainer.getChildren().remove(overlay);
                            }
                        }),
                        () -> Platform.runLater(() -> {
                            // Remove the progress indicator when done
                            if (rootContainer.getChildren().contains(overlay)) {
                                rootContainer.getChildren().remove(overlay);
                            }
                        })
                );
    }

    /**
     * Connects to the server, sends the username, and requests the friend list.
     */
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send username to server
            out.println(username);
            // Request friend list
            out.println("/listFriends");

            // Listen for incoming messages
            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;
                Platform.runLater(() -> processIncomingMessage(msg));
            }
        } catch (IOException e) {
            Platform.runLater(() -> addMessage("Unable to connect to server.", "left"));
            e.printStackTrace();
        }
    }

    /**
     * Processes messages from the server: friend list updates or chat messages.
     */
    private void processIncomingMessage(String message) {

        if (message.startsWith("PHOTO:")) {
            // Handle incoming photo
            String[] parts = message.split(":", 3);
            String fileName = parts[1];
            int fileSize = Integer.parseInt(parts[2]);
            try {
                byte[] fileBytes = new byte[fileSize];
                InputStream inputStream = socket.getInputStream();
                inputStream.read(fileBytes, 0, fileSize);

                // Display the photo in the chat
                Platform.runLater(() -> displayPhoto(fileName, fileBytes));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {



            System.out.println("Received: " + message);
            if (message.startsWith("USERLIST:")) {
                VboxLeftSide.getChildren().clear();
                // Server is sending the friend list
                String list = message.substring("USERLIST:".length());
                if (!list.trim().isEmpty()) {
                    // IMPORTANT: Clear the old friend list first
                    vboxUserInfo.getChildren().clear();

                    String[] friends = list.split(",");
                    for (String friendData : friends) {
                        String[] parts = friendData.split(":", 2);
                        String friendUsername = parts[0].trim();
                        String lastMessage    = (parts.length > 1) ? parts[1].trim() : "No messages yet";

                        // Add friend to the UI
                        addUserToLeftSide(friendUsername, lastMessage, "", "default_profile.png");
                    }
                } else {
                    System.out.println("No friends found.");
                }
            } else if (message.contains(": ")) {
                // Format "sender: content"
                String[] parts = message.split(": ", 2);
                if (parts.length == 2) {
                    String sender  = parts[0].trim();
                    String content = parts[1].trim();

                    // If the sender is me, align right; otherwise, left
                    if (sender.equals(username) || sender.equals("You")) {
                        addMessage(sender + ": " + content, "right");
                    } else {
                        addMessage(sender + ": " + content, "left");
                    }
                } else {
                    addMessage(message, "left");
                }
            } else {
                addMessage(message, "left");
            }
        }
    }


    private void displayPhoto(String fileName, byte[] fileBytes) {
        Image image = new Image(new ByteArrayInputStream(fileBytes));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200); // Adjust size as needed
        imageView.setPreserveRatio(true);

        // Add the image to the chat UI
        HBox messageContainer = new HBox(imageView);
        messageContainer.setAlignment(Pos.CENTER_LEFT); // Adjust alignment as needed
        chatMessageContainer.getChildren().add(messageContainer);
    }



    /**
     * Sends a message to the currentRecipient, saves it in the DB, and refreshes friend list.
     */
    @FXML
    private void sendMessage() {
        String message = chat_text_send.getText().trim();
        if (!message.isEmpty() && out != null && currentRecipient != null) {
            // Send "recipient: message" to the server
            out.println(currentRecipient + ": " + message);

            // Show it in my own chat window
            addMessage("You: " + message, "right");

            // Also save to the DB for persistence
            saveMessage(username, currentRecipient, message);

            // Refresh the friend list so last message is updated
            out.println("/listFriends");

            chat_text_send.clear();
        } else if (currentRecipient == null) {
            addMessage("Select a user to chat with.", "left");
        }
    }

    /**
     * Saves a message in the local DB. (Optional if your design stores everything on the server)
     */
    private void saveMessage(String sender, String receiver, String content) {
        String query = """
            INSERT INTO messages (sender_id, receiver_id, content, created_at)
            VALUES (?, ?, ?, NOW())
        """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, getUserIdByUsername(sender, connection));
            stmt.setInt(2, getUserIdByUsername(receiver, connection));
            stmt.setString(3, content);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sample exit handler for a profile pane.
     */
    @FXML
    public void exit(ActionEvent event) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), profilePage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            profilePage.setVisible(false);
            profilePage.toBack();
        });
        fadeOut.play();
    }

    // UI transitions/animations
    @FXML
    void datals() {
        double width = vdatal1.getBoundsInParent().getWidth();
        TranslateTransition moveOut = new TranslateTransition(Duration.millis(as), vdatal1);
        moveOut.setFromX(0);
        moveOut.setToX(-width);
        moveOut.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(as / 2), vdatal1);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition moveIn = new TranslateTransition(Duration.millis(as), vdatal2);
        moveIn.setFromX(width);
        moveIn.setToX(0);
        moveIn.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(as / 2), vdatal2);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        moveOut.setOnFinished(e -> {
            vdatal1.setVisible(false);
            vdatal2.setVisible(true);
            moveIn.play();
            fadeIn.play();
        });

        vdatal2.setVisible(true);
        new ParallelTransition(moveOut, fadeOut).play();
    }

    @FXML
    void datalss() {
        double width = vdatal2.getBoundsInParent().getWidth();
        TranslateTransition moveOut = new TranslateTransition(Duration.millis(as), vdatal2);
        moveOut.setFromX(0);
        moveOut.setToX(-width);
        moveOut.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(as / 2), vdatal2);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition moveIn = new TranslateTransition(Duration.millis(as), vdatal1);
        moveIn.setFromX(width);
        moveIn.setToX(0);
        moveIn.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(as / 2), vdatal1);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        moveOut.setOnFinished(e -> {
            vdatal2.setVisible(false);
            vdatal1.setVisible(true);
            moveIn.play();
            fadeIn.play();
        });

        vdatal1.setVisible(true);
        new ParallelTransition(moveOut, fadeOut).play();
    }

    @FXML
    void profile() {
        profilePage.toFront();
        profilePage.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), profilePage);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Adds a chat bubble to the VBox (left or right).
     */
    private void addMessage(String text, String alignment) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));
            messageContainer.setAlignment(alignment.equals("right") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Label messageLabel = new Label(text);
            messageLabel.setFont(Font.font("System Bold", 16));
            messageLabel.setPrefHeight(44);
            messageLabel.setMaxWidth(338);
            messageLabel.setWrapText(true);
            messageLabel.setPadding(new Insets(10));

            if ("right".equals(alignment)) {
                messageLabel.setStyle("-fx-background-color: #131313; -fx-background-radius: 50; -fx-text-fill: white;");
                VBox.setMargin(messageLabel, new Insets(10, 10, 10, 250));
            } else {
                messageLabel.setStyle("-fx-background-color: #2bc723; -fx-background-radius: 50; -fx-text-fill: white;");
                VBox.setMargin(messageLabel, new Insets(10, 250, 10, 10));
            }

            messageContainer.getChildren().add(messageLabel);
            vboxChat.getChildren().add(messageContainer);
        });
    }

    /**
     * Adds a friend entry (username + last message) to the left sidebar.
     */
    private void addUserToLeftSide(String username, String lastMessage, String time, String imagePath) {
        // Create the HBox for the friend entry
        HBox userHBox = new HBox();
        userHBox.setAlignment(Pos.CENTER_LEFT);
        userHBox.setPrefHeight(96);
        userHBox.setPrefWidth(314);
        userHBox.setStyle("-fx-background-color: #000; -fx-padding: 10; -fx-background-radius: 10;");

        // Profile image
        ImageView profileImage = new ImageView();
        profileImage.setFitWidth(52);
        profileImage.setFitHeight(60);
        // (Load your image or placeholder)
        // Example:
        // try {
        //     Image image = new Image(getClass().getResourceAsStream("/" + imagePath));
        //     profileImage.setImage(image);
        // } catch (Exception e) {
        //     profileImage.setImage(new Image("https://via.placeholder.com/52x60"));
        // }
        HBox.setMargin(profileImage, new Insets(0, 10, 0, 10));

        // Create VBox for username + last message
        VBox vboxUserInfo = new VBox();
        vboxUserInfo.setAlignment(Pos.CENTER_LEFT);
        vboxUserInfo.setPrefHeight(76);
        vboxUserInfo.setPrefWidth(190);

        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #fcfcfc;");

        Label messageLabel = new Label(lastMessage);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dcdcdc;");

        vboxUserInfo.getChildren().addAll(nameLabel, messageLabel);

        // (Optional) Time label on the right
        VBox vboxTimeLastChat = new VBox();
        vboxTimeLastChat.setAlignment(Pos.TOP_CENTER);
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
        vboxTimeLastChat.getChildren().add(timeLabel);

        // Add all sub-nodes to userHBox
        userHBox.getChildren().addAll(profileImage, vboxUserInfo, vboxTimeLastChat);

        // **Finally**, add userHBox to the container you use for the friend list.
        // If your friend list is in VboxLeftSide, do this:
        VboxLeftSide.getChildren().add(userHBox);

        // Or if you decided to use vboxUserInfo for the entire friend list,
        // you would do: vboxUserInfo.getChildren().add(userHBox);
        // Just be consistent with whichever container you cleared.

        // (Optional) Set a click event to load chat:
        userHBox.setOnMouseClicked(event -> loadChatMessages(username));
    }


    /**
     * Loads conversation history with the selected friend.
     */
    private void loadChatMessages(String friendUsername) {
        currentRecipient = friendUsername;
        Platform.runLater(() -> {
            vboxChat.getChildren().clear();
            List<ChatMessage> messages;
            try {
                messages = getMessagesForUser(friendUsername);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (messages == null || messages.isEmpty()) {
                System.out.println("No messages found for: " + friendUsername);
                return;
            }

            for (ChatMessage chatMessage : messages) {
                Label chatLabel = new Label(chatMessage.getMessage());
                chatLabel.setFont(Font.font("System Bold", 16));
                chatLabel.setMaxWidth(338);
                chatLabel.setPrefHeight(44);
                chatLabel.setPadding(new Insets(10));

                // If the message's sender is the friend, show on left; if me, on right
                if (chatMessage.getSender().equals(friendUsername)) {
                    chatLabel.setStyle("-fx-background-color: #2bc723; -fx-background-radius: 50; -fx-text-fill: white;");
                    VBox.setMargin(chatLabel, new Insets(10, 250, 10, 10));
                } else {
                    chatLabel.setStyle("-fx-background-color: #131313; -fx-background-radius: 50; -fx-text-fill: white;");
                    VBox.setMargin(chatLabel, new Insets(10, 10, 10, 250));
                }
                vboxChat.getChildren().add(chatLabel);
            }
        });
    }

    /**
     * Gets the user_id for a username from the DB.
     */
    private int getUserIdByUsername(String username, Connection connection) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return -1;
    }

    /**
     * Fetches messages between me (username) and friendUsername from the DB.
     */
    private List<ChatMessage> getMessagesForUser(String friendUsername) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            int currentUserId = getUserIdByUsername(username, connection);
            int friendUserId  = getUserIdByUsername(friendUsername, connection);
            if (currentUserId == -1 || friendUserId == -1) {
                System.out.println("User not found.");
                return messages;
            }
            String sql = """
                SELECT m.sender_id, m.content, u.username
                FROM messages m
                JOIN users u ON m.sender_id = u.user_id
                WHERE ((m.sender_id = ? AND m.receiver_id = ?)
                    OR  (m.sender_id = ? AND m.receiver_id = ?))
                ORDER BY m.created_at ASC
            """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, currentUserId);
                statement.setInt(2, friendUserId);
                statement.setInt(3, friendUserId);
                statement.setInt(4, currentUserId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String senderUsername = rs.getString("username");
                        String content        = rs.getString("content");
                        messages.add(new ChatMessage(senderUsername, content));
                    }
                }
            }
        }
        return messages;
    }

    // Standard JavaFX application entry point
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml")); // Ensure correct FXML path
        primaryStage.setTitle("Chatus");
        primaryStage.setScene(new Scene(root));
        primaryStage.setWidth(400);
        primaryStage.setWidth(500);
        primaryStage.show();
    }
}
