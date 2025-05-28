package program.chatus;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;


public class ProfilePageController {
    @FXML
    private VBox profilePage;

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
    private HBox targetCard;

    public void setTargetCard(HBox card) {
        this.targetCard = card;
    }

    @FXML
    private void onDelete(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/chatus/chattix/ADeleteConfirmation.fxml"));
            Parent root = loader.load();

            DeleteConfirmationController controller = loader.getController();
            Stage popupStage = new Stage();
            controller.setDialogStage(popupStage);

            controller.setOnConfirmAction(() -> {
                if (targetCard != null) {
                    ((Pane) targetCard.getParent()).getChildren().remove(targetCard);
                    System.out.println("User card deleted.");
                }
            });

            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }











    public void onBlock(ActionEvent event) {
    }

    public void onReport(ActionEvent event) {
    }


}
