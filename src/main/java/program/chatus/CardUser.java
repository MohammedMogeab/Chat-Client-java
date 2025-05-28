package program.chatus;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CardUser {

    @FXML
    private ImageView imgporfile1;

    @FXML
    private Label LabelUsername;

    @FXML
    private Label LabelLastMessage;

    @FXML
    private Label Labellastmessagetime;
     LocalTime localTime;
     List<String> imagesname;
    public void setUserData(String username, String lastMessage, String time, String imagePath) {
        LabelUsername.setText(username);
        LabelLastMessage.setText(lastMessage);
        String niceTime = formatTimeSafely(time, "HH:mm", "hh:mm a"); // → "02:30 PM"
        Labellastmessagetime.setText(niceTime);


        try {
//            File folder=new File(getClass().getResource("/program/chatus/Chattix/images/").toURI());
//             File[]files=folder.listFiles();

//                 int rondam=new Random().nextInt(files.length);
                 Image image=new Image(getClass().getResourceAsStream("/program/chatus/Chattix/images/"+username+".png"));


            imgporfile1.setImage(image);
        } catch (Exception e) {
            System.out.println("the image not found");
            imgporfile1.setImage(new Image("https://via.placeholder.com/52x60"));
        }
    }
    public String formatTimeSafely(String timeStr, String inputPattern, String outputPattern) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputPattern);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputPattern);
            LocalTime time = LocalTime.parse(timeStr, inputFormatter);
            return time.format(outputFormatter);
        } catch (Exception e) {
            return timeStr; // fallback: return raw string if parsing fails
        }
    }


    public void setLastMessage(String lastMessage) {
        LabelLastMessage.setText(lastMessage);
    }
    public void setLastMessageTime(String time) {
        Labellastmessagetime.setText(time);
    }
}

