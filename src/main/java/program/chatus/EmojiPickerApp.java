//package program.chatus;
//
//import com.vdurmont.emoji.EmojiParser;
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.stage.Stage;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//public class EmojiPickerApp extends Application {
//
//    private TextField chatInput = new TextField();
//    private Map<String, List<String>> emojiMap = new LinkedHashMap<>();
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        loadEmojisFromFile("emoji_aliases_chat_categorized.txt");
//
//        TabPane tabPane = new TabPane();
//
//        for (Map.Entry<String, List<String>> entry : emojiMap.entrySet()) {
//            String category = entry.getKey();
//            List<String> aliases = entry.getValue();
//
//            GridPane grid = new GridPane();
//            grid.setPadding(new Insets(10));
//            grid.setHgap(10);
//            grid.setVgap(10);
//
//            int col = 0, row = 0;
//            for (String alias : aliases) {
//                String unicodeEmoji = EmojiParser.parseToUnicode(":" + alias + ":");
//
//                Button emojiButton = new Button(unicodeEmoji);
//                emojiButton.setFont(Font.font(24));
//                emojiButton.setOnAction(e -> chatInput.appendText(unicodeEmoji));
//
//                grid.add(emojiButton, col++, row);
//                if (col >= 8) {
//                    col = 0;
//                    row++;
//                }
//            }
//
//            ScrollPane scrollPane = new ScrollPane(grid);
//            scrollPane.setFitToWidth(true);
//            tabPane.getTabs().add(new Tab(category, scrollPane));
//        }
//
//        VBox root = new VBox(10, tabPane, chatInput);
//        root.setPadding(new Insets(10));
//
//        primaryStage.setScene(new Scene(root, 600, 500));
//        primaryStage.setTitle("Emoji Picker Chat UI");
//        primaryStage.show();
//    }
//
//    private void loadEmojisFromFile(String filePath) {
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            String currentCategory = null;
//
//            while ((line = reader.readLine()) != null) {
//                if (line.startsWith("###")) {
//                    currentCategory = line.replace("###", "").trim();
//                    emojiMap.put(currentCategory, new ArrayList<>());
//                } else if (!line.isEmpty() && currentCategory != null) {
//                    String[] parts = line.split(" : ");
//                    if (parts.length == 2) {
//                        String alias = parts[1].trim();  // only the alias (e.g., smile)
//                        emojiMap.get(currentCategory).add(alias);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
