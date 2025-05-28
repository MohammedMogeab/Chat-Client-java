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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.scene.Node;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.stereotype.Controller;
import program.chatus.Model.ChatMessage;
import program.chatus.Model.UserSession;
import program.chatus.Util.ContextUtil;
import program.chatus.Model.MessageDTO;
import program.chatus.Model.User;
import program.chatus.Util.DatabaseConnection;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Base64;
import java.nio.file.Files;
import java.time.LocalDateTime;

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



    //card user
    @FXML
    private HBox HboxCardUser;
    //and iside it
    @FXML
    private ImageView imgporfile1;
    // inside  card user Hbox also
    @FXML
    private VBox Vbox_cardUser_User_lastmessage;
    //inside
    @FXML
    private Label LabelUsername;
    @FXML
    private Label LabelLastMessage;
    //inside Hbox card user
    @FXML
    private VBox Vbox_CardUser_lastmessagetime;
    @FXML
    private Label Labellastmessagetime;

//    private Client client;

//    int userId = Integer.parseInt(JWTDocoder.extractuserId(Tokens.getToken())); // assume extractuserId returns int now
    // vairble username
    @FXML
    private Label UsernameInsetting2;
    @FXML
    private Label usernameinsetting;




    // varible group create
    @FXML
    private Button ButtonCancelGroup;
    @FXML
    private Button ButtonCreateGroup;
    @FXML
    private TextField TextFeildGroup;

    @FXML
    private Button microBtn;








    // Server connection variables
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 1234;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);  // Adjust as needed
    private final Map<String, CardUser> userControllerMap = new HashMap<>();

    // The current conversation recipient
    private String currentRecipient = null;
    // The logged-in user from session
    private String username = UserSession.getInstance().getUsername();

    int as = 100;

    // Add new fields for message handling
    private MessageContextMenuController messageContextMenu;
    private CardUser currentCardUser;
    private String selectedMessageId;
    static String delimiter = "<::>";
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
        try {
            byte[] imageBytes = Files.readAllBytes(photoFile.toPath());
            String ImageName=photoFile.getName();
            System.out.println(ImageName);

            dataOut.writeUTF("IMAGE");
            dataOut.writeUTF(username);
            dataOut.writeUTF(currentRecipient);
            dataOut.writeInt(imageBytes.length);
            dataOut.writeUTF(ImageName);
            dataOut.write(imageBytes);
            dataOut.flush();
                         
            System.out.println("Photo sent: " + photoFile.getName());
            Image image = new Image(photoFile.toURI().toString());
            AddMessageImage(photoFile,"right",currentRecipient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void  AddMessageImage(File photo, String alignment, String SelectUserFriend) {
        Platform.runLater(() -> {
            CardUser cardUser = userControllerMap.get(SelectUserFriend);
            MessageImageController imageController = MessageImageController.create(photo, alignment, SelectUserFriend, cardUser);
            if (imageController != null) {
                vboxChat.getChildren().add(imageController.getRoot());
            }
        });
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
        // Existing initialization code
        Sendbtn.setOnAction(e -> sendMessage());
        chat_Search.textProperty().addListener((obs, oldVal, newVal) -> {
            filterFriendList(newVal);
        });

        // Initialize message context menu
        messageContextMenu = MessageContextMenuController.create();

        // Load friend list from database
        loadFriendList();

        // Connect to server on a background thread
        threadPool.execute(this::connectToServer);
    }

    @FXML private ScrollPane chatScrollPane;

    private void filterFriendList(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // Show all friends
            return;
        }
        
        // Filter friends based on search text
        for (javafx.scene.Node node : VboxLeftSide.getChildren()) {
            if (node instanceof HBox) {
                HBox userBox = (HBox) node;
                Label usernameLabel = (Label) ((VBox) userBox.getChildren().get(1)).getChildren().get(0);
                boolean matches = usernameLabel.getText().toLowerCase().contains(searchText.toLowerCase());
                userBox.setVisible(matches);
            }
        }
    }

    private void addMessage(String message, boolean isUserMessage) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));
            messageContainer.setAlignment(isUserMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("System Bold", 16));
            messageLabel.setPrefHeight(44);
            messageLabel.setMaxWidth(338);
            messageLabel.setWrapText(true);
            messageLabel.setPadding(new Insets(10));

            // Add context menu
            messageLabel.setOnContextMenuRequested(e -> {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setContent(message);
                messageDTO.setSender(isUserMessage ? username : currentRecipient);
                messageDTO.setReceiver(isUserMessage ? currentRecipient : username);
                messageDTO.setTimestamp(LocalDateTime.now().toString());
                messageDTO.setStatus("delivered");
                
                messageContextMenu.show(
                    messageLabel.getScene().getWindow(),
                    e.getScreenX(),
                    e.getScreenY(),
                    messageDTO,
                    () -> handleReply(message),
                    () -> handleForward(message),
                    () -> handleDelete(message)
                );
            });

            if (isUserMessage) {
                messageLabel.setStyle("-fx-background-color: #131313; -fx-background-radius: 50; -fx-text-fill: white;");
                VBox.setMargin(messageLabel, new Insets(10, 10, 10, 250));
            } else {
                messageLabel.setStyle("-fx-background-color: #2bc723; -fx-background-radius: 50; -fx-text-fill: white;");
                VBox.setMargin(messageLabel, new Insets(10, 250, 10, 10));
            }

            messageContainer.getChildren().add(messageLabel);
            vboxChat.getChildren().add(messageContainer);

            // Scroll to bottom after adding message
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (vboxChat.getChildren().size() > 0) {
                vboxChat.requestLayout();
                vboxChat.layout();
                
                // Find the ScrollPane parent
                Node parent = vboxChat.getParent();
                while (parent != null && !(parent instanceof ScrollPane)) {
                    parent = parent.getParent();
                }
                
                if (parent instanceof ScrollPane) {
                    ScrollPane scrollPane = (ScrollPane) parent;
                    scrollPane.setVvalue(1.0);
                }
            }
        });
    }

    private void handleReply(String message) {
        chat_text_send.setText("Reply to: " + message);
        chat_text_send.requestFocus();
    }

    private void handleForward(String message) {
        // Show user selection dialog for forwarding
        showForwardDialog(message);
    }

    private void handleDelete(String message) {
        // Show delete confirmation dialog
        showDeleteConfirmation(() -> {
            // Delete message from database and UI
            deleteMessage(message);
        });
    }

    private void showDeleteConfirmation(Runnable onConfirm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ADeleteConfirmation.fxml"));
            Parent root = loader.load();
            
            DeleteConfirmationController controller = loader.getController();
            controller.setOnConfirmAction(onConfirm);
            
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showForwardDialog(String message) {
        // Create a dialog to select recipient
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Forward Message");
        dialog.setHeaderText("Select recipient");

        // Create the custom dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Add user list
        ScrollPane scrollPane = new ScrollPane();
        VBox userList = new VBox(5);
        
        // Populate user list
        for (javafx.scene.Node node : VboxLeftSide.getChildren()) {
            if (node instanceof HBox) {
                HBox userBox = (HBox) node;
                Label usernameLabel = (Label) ((VBox) userBox.getChildren().get(1)).getChildren().get(0);
                
                Button userButton = new Button(usernameLabel.getText());
                userButton.setMaxWidth(Double.MAX_VALUE);
                userButton.setOnAction(e -> {
                    dialog.setResult(usernameLabel.getText());
                    dialog.close();
                });
                
                userList.getChildren().add(userButton);
            }
        }
        
        scrollPane.setContent(userList);
        content.getChildren().add(scrollPane);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(recipient -> {
            if (recipient != null) {
                forwardMessage(message, recipient);
            }
        });
    }

    private void forwardMessage(String message, String recipient) {
        // Send the message to the new recipient
        try {
            byte[] msgBytes = message.getBytes("UTF-8");
            
            dataOut.writeUTF("TEXT");
            dataOut.writeUTF(username);
            dataOut.writeUTF(recipient);
            dataOut.writeInt(msgBytes.length);
            dataOut.write(msgBytes);
            dataOut.flush();
            
        addMessage("Forwarded to " + recipient + ": " + message, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteMessage(String message) {
        // Remove from UI
        for (javafx.scene.Node node : vboxChat.getChildren()) {
            if (node instanceof HBox) {
                HBox messageBox = (HBox) node;
                Label messageLabel = (Label) messageBox.getChildren().get(0);
                if (messageLabel.getText().equals(message)) {
                    vboxChat.getChildren().remove(messageBox);
                    break;
                }
            }
        }
        
        // Delete from database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM messages WHERE content = ? AND (sender_id = ? OR receiver_id = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, message);
            stmt.setInt(2, getUserIdByUsername(username, conn));
            stmt.setInt(3, getUserIdByUsername(username, conn));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingPhoto(String sender, String fileName, int FileSize) {
        try {
            byte[] photoData = new byte[FileSize];
            InputStream inputStream=socket.getInputStream();

            System.out.println("we are in handling photos"+FileSize);
//                byte[] photoData = java.util.Base64.getDecoder().decode(base64Photo);

            int readbyte=0;
            while(readbyte<FileSize){
                int read=inputStream.read(photoData,readbyte,FileSize-readbyte);
                if(read==-1)break;
                readbyte+=read;
            }
            Image image = new Image(new ByteArrayInputStream(photoData));
            
            // Create temporary file for the photo
            File tempFile = File.createTempFile("photo_", fileName);
            Files.write(tempFile.toPath(), photoData);
            
            Platform.runLater(() -> {
                MessageImageController imageController = MessageImageController.create(
                    tempFile,
                    "left",
                    sender,
                    currentCardUser
                );
                if (imageController != null) {
                    vboxChat.getChildren().add(imageController.getRoot());
                }
                
                // Create and store photo message
                ChatMessage photoMessage = new ChatMessage(
                    sender,
                    username,
                    "Photo: " + fileName,
                        "PHOTO:" + sender + ":" + fileName + ":" + photoData,
                    LocalDateTime.now(),
                    "delivered"
                );
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO messages (sender_id, receiver_id, content, message, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, getUserIdByUsername(sender, conn));
                    stmt.setInt(2, getUserIdByUsername(username, conn));
                    stmt.setString(3, photoMessage.getContent());
                    stmt.setString(4, photoMessage.getMessage());
                    stmt.setObject(5, photoMessage.getTimestamp());
                    stmt.setString(6, "delivered");
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                String content = chatMessage.getContent();
                if (content != null && content.startsWith("PHOTO"+delimiter)) {
                    // Handle photo message
                    String[] parts = content.split(delimiter, 4);
                    if (parts.length == 4) {
                        String fileName = parts[2];
                        String FileSize = parts[3];
                        handleIncomingPhoto(chatMessage.getSender(), fileName, Integer.parseInt(FileSize));
                    }
                } else if (content != null && content.startsWith("VIDEO:")) {
                    // Handle video message
                    String[] parts = content.split(":", 4);
                    if (parts.length == 4) {
                        String fileName = parts[2];
                        String base64Video = parts[3];
                        handleIncomingVideo(chatMessage.getSender(), fileName, base64Video);
                    }
                } else if (content != null && content.startsWith("FILE:")) {
                    // Handle file message
                    String[] parts = content.split(":", 5);
                    if (parts.length == 5) {
                        String fileName = parts[2];
                        String fileType = parts[3];
                        String base64File = parts[4];
                        handleIncomingFile(chatMessage.getSender(), fileName, fileType, base64File);
                    }
                } else {
                    // Handle text message
                    addMessage(chatMessage.getMessage(), chatMessage.getSender().equals(username));
                }
            }
            
            // Scroll to bottom after loading messages
            scrollToBottom();
        });
    }

    /**
     * Connects to the server, sends the username, and requests the friend list.
     */
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // Send username to server
            dataOut.writeUTF(username);
            dataOut.flush();

            // Start listening for messages
            startListening();
        } catch (IOException e) {
            Platform.runLater(() -> addMessage("Unable to connect to server.", "left"));
            e.printStackTrace();
        }
    }

    private void startListening() {
        threadPool.execute(() -> {
            try {
                while (!socket.isClosed()) {
                    String type = dataIn.readUTF();
                    String sender = dataIn.readUTF();
                    String receiver = dataIn.readUTF();
                    int length = dataIn.readInt();

                    byte[] data = new byte[length];
                    dataIn.readFully(data);

                    switch (type) {
                        case "TEXT":
                            String msg = new String(data, "UTF-8");
                    Platform.runLater(() -> {
                                if (currentRecipient != null && currentRecipient.equals(sender)) {
                                    addMessage(msg, "left");
                                }
                            });
                            break;

                        case "IMAGE":
                            Platform.runLater(() -> {
                                try {
                                    // Create temporary file for the photo
                                    File tempFile = File.createTempFile("photo_", ".jpg");
                                    Files.write(tempFile.toPath(), data);
                                    
                                    Image image = new Image(tempFile.toURI().toString());
                                    displayPhoto(image, "left");
            } catch (IOException e) {
                e.printStackTrace();
            }
                            });
                            break;

                        case "VIDEO":
                            // Handle video message
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection closed or error occurred: " + e.getMessage());
            }
        });
    }

    private void handleServerCommand(String command) {
        if (command.startsWith("/listFriends")) {
            // Handle friend list update
            String[] friends = command.substring("/listFriends".length()).trim().split(",");
            Platform.runLater(() -> {
                VboxLeftSide.getChildren().clear();
                for (String friend : friends) {
                    if (!friend.trim().isEmpty()) {
                        addUserToLeftSide(friend.trim(), "", "", "");
                    }
                }
            });
        } else if (command.startsWith("/userJoined")) {
            // Handle new user joined
            String newUser = command.substring("/userJoined".length()).trim();
            Platform.runLater(() -> {
                addUserToLeftSide(newUser, "", "", "");
            });
        } else if (command.startsWith("/userLeft")) {
            // Handle user left
            String leftUser = command.substring("/userLeft".length()).trim();
            Platform.runLater(() -> {
                // Remove user from left side
                for (javafx.scene.Node node : VboxLeftSide.getChildren()) {
                    if (node instanceof HBox) {
                        HBox userBox = (HBox) node;
                        Label usernameLabel = (Label) ((VBox) userBox.getChildren().get(1)).getChildren().get(0);
                        if (usernameLabel.getText().equals(leftUser)) {
                            VboxLeftSide.getChildren().remove(userBox);
                            break;
                        }
                    }
                }
            });
        }
    }

    private void handleIncomingVideo(String sender, String fileName, String base64Video) {
        try {
            byte[] videoData = Base64.getDecoder().decode(base64Video);
            
            // Create temporary file for the video
            File tempFile = File.createTempFile("video_", fileName);
            Files.write(tempFile.toPath(), videoData);
            
            Platform.runLater(() -> {
                // Create and store video message
                ChatMessage videoMessage = new ChatMessage(
                    sender,
                    username,
                    "Video: " + fileName,
                    "VIDEO:" + sender + ":" + fileName + ":" + base64Video,
                    LocalDateTime.now(),
                    "delivered"
                );
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO messages (sender_id, receiver_id, content, message, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, getUserIdByUsername(sender, conn));
                    stmt.setInt(2, getUserIdByUsername(username, conn));
                    stmt.setString(3, videoMessage.getContent());
                    stmt.setString(4, videoMessage.getMessage());
                    stmt.setObject(5, videoMessage.getTimestamp());
                    stmt.setString(6, "delivered");
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                // Add video message to chat
                addMessage("Video: " + fileName, false);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingFile(String sender, String fileName, String fileType, String base64File) {
        try {
            byte[] fileData = Base64.getDecoder().decode(base64File);
            
            // Create temporary file
            File tempFile = File.createTempFile("file_", fileName);
            Files.write(tempFile.toPath(), fileData);
            
            Platform.runLater(() -> {
                // Create and store file message
                ChatMessage fileMessage = new ChatMessage(
                    sender,
                    username,
                    "File: " + fileName,
                    "FILE:" + sender + ":" + fileName + ":" + fileType + ":" + base64File,
                    LocalDateTime.now(),
                    "delivered"
                );
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO messages (sender_id, receiver_id, content, message, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, getUserIdByUsername(sender, conn));
                    stmt.setInt(2, getUserIdByUsername(username, conn));
                    stmt.setString(3, fileMessage.getContent());
                    stmt.setString(4, fileMessage.getMessage());
                    stmt.setObject(5, fileMessage.getTimestamp());
                    stmt.setString(6, "delivered");
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                // Add file message to chat
                addMessage("File: " + fileName, false);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayPhoto(Image image, String alignment) {
        Platform.runLater(() -> {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(200);
            imageView.setPreserveRatio(true);

            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));
            messageContainer.setAlignment(alignment.equals("right") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            if ("right".equals(alignment)) {
                VBox.setMargin(messageContainer, new Insets(10, 10, 10, 250));
            } else {
                VBox.setMargin(messageContainer, new Insets(10, 250, 10, 10));
            }

            messageContainer.getChildren().add(imageView);
            vboxChat.getChildren().add(messageContainer);
        });
    }

    private void displayVideoThumbnail(File videoFile, String alignment) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));
            messageContainer.setAlignment(alignment.equals("right") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Label videoLabel = new Label("📹 " + videoFile.getName());
            videoLabel.setStyle("-fx-background-color: #131313; -fx-background-radius: 50; -fx-text-fill: white;");
            videoLabel.setPadding(new Insets(10));

            if ("right".equals(alignment)) {
                VBox.setMargin(messageContainer, new Insets(10, 10, 10, 250));
            } else {
                VBox.setMargin(messageContainer, new Insets(10, 250, 10, 10));
            }

            messageContainer.getChildren().add(videoLabel);
            vboxChat.getChildren().add(messageContainer);
        });
    }

    private void displayFileInfo(File file, String alignment) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));
            messageContainer.setAlignment(alignment.equals("right") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Label fileLabel = new Label("📎 " + file.getName());
            fileLabel.setStyle("-fx-background-color: #131313; -fx-background-radius: 50; -fx-text-fill: white;");
            fileLabel.setPadding(new Insets(10));

            if ("right".equals(alignment)) {
                VBox.setMargin(messageContainer, new Insets(10, 10, 10, 250));
            } else {
                VBox.setMargin(messageContainer, new Insets(10, 250, 10, 10));
            }

            messageContainer.getChildren().add(fileLabel);
            vboxChat.getChildren().add(messageContainer);
        });
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String sender = parts[0];
            String content = parts[1];
            
            // Create ChatMessage with all required fields
            ChatMessage chatMessage = new ChatMessage(
                sender,
                username,
                content,
                content,
                LocalDateTime.now(),
                "delivered"
            );
            
            addMessage(content, sender.equals(username));
            
            // Store message in database
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO messages (sender_id, receiver_id, content, message, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, getUserIdByUsername(sender, conn));
                stmt.setInt(2, getUserIdByUsername(username, conn));
                stmt.setString(3, content);
                stmt.setString(4, content);
                stmt.setObject(5, chatMessage.getTimestamp());
                stmt.setString(6, chatMessage.getStatus());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a message to the currentRecipient, saves it in the DB, and refreshes friend list.
     */
    @FXML
    private void sendMessage() {
        String message = chat_text_send.getText().trim();
        if (!message.isEmpty() && currentRecipient != null) {
            try {
                byte[] msgBytes = message.getBytes("UTF-8");
                
                dataOut.writeUTF("TEXT");
                dataOut.writeUTF(username);
                dataOut.writeUTF(currentRecipient);
                dataOut.writeInt(msgBytes.length);
                dataOut.write(msgBytes);
                dataOut.flush();
                
                addMessage(message, "right");
                chat_text_send.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSendPhoto() {
        if (currentRecipient == null) {
            addMessage("Select a user to chat with.", false);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(chat_text_send.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Check file size (10MB limit for photos)
                if (selectedFile.length() > 10 * 1024 * 1024) {
                    addMessage("Photo size exceeds 10MB limit.", false);
                    return;
                }

                // Read file into byte array
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());

                // Send header first
//                String header = "PHOTO:" + currentRecipient + ":" + selectedFile.getName() + ":" + fileContent.length + "\n";
                String header = "PHOTO:" + currentRecipient + "::" + selectedFile.getName() + "::" + fileContent.length + "\n";

                dataOut.writeUTF("IMAGE");
                dataOut.writeUTF(username);
                dataOut.writeUTF(currentRecipient);
                dataOut.writeInt(header.length());
                dataOut.write(header.getBytes("UTF-8"));
                dataOut.writeInt(fileContent.length);
                dataOut.write(fileContent);
                dataOut.flush();

                // Display photo in sender's chat
                Image image = new Image(selectedFile.toURI().toString());
                displayPhoto(image, "right");

                // Store in database
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO messages (sender_id, receiver_id, content, created_at, status) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, getUserIdByUsername(username, conn));
                    stmt.setInt(2, getUserIdByUsername(currentRecipient, conn));
                    stmt.setString(3, "Photo: " + selectedFile.getName());
                    stmt.setObject(4, LocalDateTime.now());
                    stmt.setString(5, "delivered");
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                addMessage("Error sending photo", false);
            }
        }
    }

    @FXML
    private void handleSendVideo() {
        if (currentRecipient == null) {
            showAlert("Error", "Please select a recipient first");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Video");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov", "*.wmv")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            try {
                // Check file size (max 100MB)
                if (selectedFile.length() > 100 * 1024 * 1024) {
                    showAlert("Error", "Video size exceeds 100MB limit");
                    return;
                }

                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                String base64Video = Base64.getEncoder().encodeToString(fileBytes);
                
                // Send video with new format: VIDEO:recipient:filename:base64data
                dataOut.writeUTF("VIDEO");
                dataOut.writeUTF(currentRecipient);
                dataOut.writeUTF(selectedFile.getName());
                dataOut.writeUTF(base64Video);
                dataOut.flush();
                
                // Display video thumbnail
                displayVideoThumbnail(selectedFile, "right");
            } catch (IOException e) {
                showAlert("Error", "Failed to send video: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSendFile() {
        if (currentRecipient == null) {
            showAlert("Error", "Please select a recipient first");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
            new FileChooser.ExtensionFilter("Archives", "*.zip", "*.rar"),
            new FileChooser.ExtensionFilter("Spreadsheets", "*.xls", "*.xlsx"),
            new FileChooser.ExtensionFilter("Presentations", "*.ppt", "*.pptx")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            try {
                // Check file size (max 50MB)
                if (selectedFile.length() > 50 * 1024 * 1024) {
                    showAlert("Error", "File size exceeds 50MB limit");
                    return;
                }

                // Get file extension
                String fileType = getFileExtension(selectedFile.getName());
                if (!isValidFileType(fileType)) {
                    showAlert("Error", "Invalid file type. Allowed types: pdf, doc, docx, txt, zip, rar, xls, xlsx, ppt, pptx");
                    return;
                }

                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                String base64File = Base64.getEncoder().encodeToString(fileBytes);
                
                // Send file with new format: FILE:recipient:filename:filetype:base64data
                dataOut.writeUTF("FILE");
                dataOut.writeUTF(currentRecipient);
                dataOut.writeUTF(selectedFile.getName());
                dataOut.writeUTF(fileType);
                dataOut.writeUTF(base64File);
                dataOut.flush();
                
                // Display file info
                displayFileInfo(selectedFile, "right");
            } catch (IOException e) {
                showAlert("Error", "Failed to send file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private boolean isValidFileType(String fileType) {
        String[] allowedTypes = {"pdf", "doc", "docx", "txt", "zip", "rar", "xls", "xlsx", "ppt", "pptx"};
        return Arrays.asList(allowedTypes).contains(fileType.toLowerCase());
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
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
    private void addUserToLeftSide( String username, String lastMessage, String time, String imagePath) {
//        Platform.runLater(() -> {
//            // Create the HBox for the friend entry
//            HBox userHBox = new HBox();
//            userHBox.setAlignment(Pos.CENTER_LEFT);
//            userHBox.setPrefHeight(96);
//            userHBox.setPrefWidth(314);
//            userHBox.setStyle("-fx-background-color: #000; -fx-padding: 10; -fx-background-radius: 10;");
//            userHBox.setSpacing(10);
//
//            // Profile image
//            ImageView profileImage = new ImageView();
//            profileImage.setFitWidth(52);
//            profileImage.setFitHeight(60);
//            try {
//                // Try to load user's profile image, fallback to default if not found
//                Image image = new Image(getClass().getResourceAsStream("/program/chatus/images/default_profile.png"));
//                profileImage.setImage(image);
//            } catch (Exception e) {
//                // If default image not found, create a colored circle with initials
//                profileImage.setImage(createDefaultProfileImage(username));
//            }
//            HBox.setMargin(profileImage, new Insets(0, 10, 0, 10));
//
//            // Create VBox for username + last message
//            VBox vboxUserInfo = new VBox(5); // 5 pixels spacing
//            vboxUserInfo.setAlignment(Pos.CENTER_LEFT);
//            vboxUserInfo.setPrefHeight(76);
//            vboxUserInfo.setPrefWidth(190);
//
//            Label nameLabel = new Label(username);
//            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #fcfcfc;");
//
//            Label messageLabel = new Label(lastMessage != null ? lastMessage : "");
//            messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dcdcdc;");
//            messageLabel.setWrapText(true);
//            messageLabel.setMaxWidth(190);
//
//            vboxUserInfo.getChildren().addAll(nameLabel, messageLabel);
//
//            // Time label on the right
//            VBox vboxTimeLastChat = new VBox();
//            vboxTimeLastChat.setAlignment(Pos.TOP_CENTER);
//            Label timeLabel = new Label(time);
//            timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
//            vboxTimeLastChat.getChildren().add(timeLabel);
//
//            // Add all sub-nodes to userHBox
//            userHBox.getChildren().addAll(profileImage, vboxUserInfo, vboxTimeLastChat);
//
//            // Add click handler to load chat
//            userHBox.setOnMouseClicked(event -> {
//                currentRecipient = username;
//                chat_name.setText(username);
//                loadChatMessages(username);
//            });
//
//            // Add hover effect
//            userHBox.setOnMouseEntered(e -> userHBox.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 10; -fx-background-radius: 10;"));
//            userHBox.setOnMouseExited(e -> userHBox.setStyle("-fx-background-color: #000; -fx-padding: 10; -fx-background-radius: 10;"));
//
//            // Add to the left side
//            VboxLeftSide.getChildren().add(userHBox);
//        });

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CardUser.fxml"));
            HBox card = loader.load();

            CardUser controller = loader.getController();
            controller.setUserData(username, lastMessage, time, imagePath);
            System.out.println("the userfriend is "+username);
            userControllerMap.put(username, controller);
            card.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    loadChatMessages( username); // Left click
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    showProfilePopup(event.getScreenX(), event.getScreenY(), username,card);

                    // Add to VBox container

                }

            });
            VboxLeftSide.getChildren().add(card);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    private Popup profilePopup;
    private void showProfilePopup(double x, double y, String username, HBox card) {
        if (card.getScene() == null) {
            Platform.runLater(() -> showProfilePopup(x, y, username, card));
            return;
        }
        Window window = card.getScene().getWindow();
        if (window == null) {
            System.err.println("❌ Card's window is null!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/chatus/ProfilePageController.fxml"));
            VBox profileContent = loader.load();
            ProfilePageController controller = loader.getController();
            controller.setTargetCard(card);

            // Optional: apply a drop shadow if not already in FXML
            profileContent.setEffect(new DropShadow());

            profilePopup = new Popup();
            profilePopup.getContent().add(profileContent);
            profilePopup.setAutoHide(true);
            profilePopup.show(window, x, y);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Image createDefaultProfileImage(String username) {
        // Create a colored circle with user's initials
        int size = 52;
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();
        
        // Generate a color based on username
        int color = username.hashCode();
        Color backgroundColor = Color.rgb(
            (color & 0xFF0000) >> 16,
            (color & 0x00FF00) >> 8,
            color & 0x0000FF
        );
        
        // Fill circle
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                double dx = x - size/2;
                double dy = y - size/2;
                if (dx*dx + dy*dy <= (size/2)*(size/2)) {
                    writer.setColor(x, y, backgroundColor);
                }
            }
        }
        
        return image;
    }

    /**
     * Gets the user_id for a username from the DB.
     */
    private int getUserIdByUsername(String username, Connection connection) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Error: Username is null or empty");
            return -1;
        }

        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                } else {
                    System.err.println("Error: User not found in database: " + username);
                    // Insert the user if they don't exist
                    String insertSql = "INSERT INTO users (username, created_at) VALUES (?, NOW())";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, username.trim());
                        insertStmt.executeUpdate();
                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                return generatedKeys.getInt(1);
                            }
                        }
                    }
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
                        messages.add(new ChatMessage(senderUsername, friendUsername, content, content, LocalDateTime.now(), "delivered"));
                    }
                }
            }
        }
        return messages;
    }

    private void loadFriendList() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get current user's ID
            int currentUserId = getUserIdByUsername(username, conn);
            if (currentUserId == -1) {
                System.err.println("Current user not found in database");
                return;
            }

            // Query to get all users except current user
            String sql = "SELECT u.user_id, u.username, " +
                        "(SELECT m.content FROM messages m " +
                        "WHERE (m.sender_id = u.user_id AND m.receiver_id = ?) " +
                        "OR (m.sender_id = ? AND m.receiver_id = u.user_id) " +
                        "ORDER BY m.created_at DESC LIMIT 1) as last_message, " +
                        "(SELECT m.created_at FROM messages m " +
                        "WHERE (m.sender_id = u.user_id AND m.receiver_id = ?) " +
                        "OR (m.sender_id = ? AND m.receiver_id = u.user_id) " +
                        "ORDER BY m.created_at DESC LIMIT 1) as last_message_time " +
                        "FROM users u " +
                        "WHERE u.user_id != ? " +
                        "ORDER BY CASE WHEN last_message_time IS NULL THEN 1 ELSE 0 END, last_message_time DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentUserId);
                stmt.setInt(2, currentUserId);
                stmt.setInt(3, currentUserId);
                stmt.setInt(4, currentUserId);
                stmt.setInt(5, currentUserId);

                try (ResultSet rs = stmt.executeQuery()) {
                    // Create a list to store the results
                    List<Map<String, Object>> results = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("username", rs.getString("username"));
                        row.put("last_message", rs.getString("last_message"));
                        row.put("last_message_time", rs.getTimestamp("last_message_time"));
                        results.add(row);
                    }

                    // Update UI with the results
                    Platform.runLater(() -> {
                        VboxLeftSide.getChildren().clear();
                        for (Map<String, Object> row : results) {
                            String friendUsername = (String) row.get("username");
                            String lastMessage = (String) row.get("last_message");
                            Timestamp lastMessageTime = (Timestamp) row.get("last_message_time");
                            
                            // Format the time
                            String timeStr = "";
                            if (lastMessageTime != null) {
                                timeStr = lastMessageTime.toLocalDateTime().format(
                                    java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                                );
                            }

                            // Add user to the left side
                            addUserToLeftSide(friendUsername, lastMessage, timeStr, "");
                        }
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading friend list: " + e.getMessage());
            e.printStackTrace();
        }
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


    public void sendMessageAi(ActionEvent event) {
    }
}
