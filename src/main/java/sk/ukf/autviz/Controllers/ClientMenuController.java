package sk.ukf.autviz.Controllers;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import sk.ukf.autviz.Models.Model;
import sk.ukf.autviz.Models.State;

import java.net.URL;
import java.sql.SQLOutput;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class ClientMenuController implements Initializable {
    public Button view1_btn;
    public Button view2_btn;
    public Button view3_btn;
    public Button determinize_btn;
    public Button minimize_btn;
    public VBox client_menu;
    public Label alphabetLabel;
    public TextField slovoField;
    public Button spracovatButton;
    public HBox znakSlovoHBox;
    public Button backButton;
    public Button forwardButton;
    public Button cancelButton;

    private Pair<List<State>, List<String>> currentWordPath;
    private int currentStep = 0;
    private State currentState;
    private String currentWord;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ClientMenuController Initialize");
        addListeners();
        alphabetLabel.textProperty().bind(Model.getInstance().getCurrentAutomata().alphabetProperty());
    }

    private void addListeners() {
        view1_btn.setOnAction(event -> onView1());
        view2_btn.setOnAction(event -> onView2());
        view3_btn.setOnAction(event -> onView3());

        determinize_btn.setOnAction(event -> onDeterminize());
        minimize_btn.setOnAction(event -> onMinimize());

        spracovatButton.setOnAction(event -> onSpracovat());

        backButton.setOnAction(event -> onBack());
        forwardButton.setOnAction(event -> onForward());
        cancelButton.setOnAction(event -> onCancel());

    }

    private void onView1() {
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().set("View1");
    }

    private void onView2() {
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().set("View2");
    }

    private void onView3() {
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().set("View3");
    }

    private void onDeterminize() {
        if (!Model.getInstance().getCurrentAutomata().checkDetermination()) {
            Model.getInstance().getCurrentAutomata().determinize();
            Model.getInstance().setUpdateView1(true);
            Model.getInstance().setUpdateView2(true);
            Model.getInstance().setUpdateView3(true);
        }
    }

    private void onMinimize() {
        Model.getInstance().getCurrentAutomata().BrzozowskiMinimalize();
        Model.getInstance().setUpdateView1(true);
        Model.getInstance().setUpdateView2(true);
        Model.getInstance().setUpdateView3(true);
    }

    private void onSpracovat() {
        String word = slovoField.getText().trim();
        if (!word.isEmpty() && isWordValid(word)) {
            znakSlovoHBox.setVisible(true);
            slovoField.setDisable(true);
            view1_btn.setDisable(true);
            view2_btn.setDisable(true);
            view3_btn.setDisable(true);
            determinize_btn.setDisable(true);
            minimize_btn.setDisable(true);
            spracovatButton.setDisable(true);

            startWordVisualization(word);
        } else {
            System.out.println("Entered word is empty or contains invalid symbols.");
        }
    }

    private boolean isWordValid(String word) {
        Set<String> alphabetSet = Model.getInstance().getCurrentAutomata().getAlphabet();
        for (char c : word.toCharArray()) {
            if (!alphabetSet.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    private void onCancel() {
        znakSlovoHBox.setVisible(false);
        view1_btn.setDisable(false);
        view2_btn.setDisable(false);
        view3_btn.setDisable(false);
        determinize_btn.setDisable(false);
        minimize_btn.setDisable(false);
        slovoField.setDisable(false);
        spracovatButton.setDisable(false);

        currentState.setActive(false);
        currentState = null;
        currentWord = null;
    }

    public void startWordVisualization(String word) {
        currentWordPath = Model.getInstance().getCurrentAutomata().getWordPath(word);
        currentStep = 0;
        currentWord = word;
        currentState = null;
        updateWordVisualization();
    }

    private void updateWordVisualization() {
        if (currentWordPath.getValue().isEmpty()) {
            slovoField.setText(currentWord.substring(currentStep));
        } else {
            slovoField.setText(currentWordPath.getValue().get(currentStep));
        }

        if (currentState != null) {
            currentState.setActive(false);
        }
        currentState = currentWordPath.getKey().get(currentStep);
        currentState.setActive(true);
    }

    private void onBack() {
        if (currentStep == 0) return;
        currentStep--;
        updateWordVisualization();
    }

    private void onForward() {
        if (currentStep == currentWordPath.getKey().size() - 1) return;
        currentStep++;
        updateWordVisualization();
    }
}
