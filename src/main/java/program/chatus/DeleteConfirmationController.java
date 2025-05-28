package program.chatus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class DeleteConfirmationController {

    private Runnable onConfirmAction;
    private Stage dialogStage;

    public void setOnConfirmAction(Runnable action) {
        this.onConfirmAction = action;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void onConfirm(ActionEvent event) {
        if (onConfirmAction != null) {
            onConfirmAction.run();
        }
        dialogStage.close();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        dialogStage.close();
    }
}
