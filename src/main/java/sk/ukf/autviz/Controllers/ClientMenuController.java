package sk.ukf.autviz.Controllers;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sk.ukf.autviz.Models.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

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
    public Button saveButton;
    public Button loadButton;

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

        saveButton.setOnAction(event -> onSave());
        loadButton.setOnAction(event -> onLoad());

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
        if (!word.isEmpty() && isWordValid(word)
                && !Model.getInstance().getCurrentAutomata().getStates().stream().filter(State::isStateBegin).toList().isEmpty()) {
            Model.getInstance().getViewFactory().getClientSelectedViewProperty().set("View1");
            Model.getInstance().setVizMode(true);
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
            System.out.println("invalid word or automata.");
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
        Model.getInstance().setVizMode(false);
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

    private void onSave() {
        try {
            saveData();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void onLoad() {
        try {
            loadData();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void saveData() throws JSONException {
        JSONObject data = new JSONObject();

        Map<State, StateCellData> stateMapping = Model.getInstance().getStateMapping();

        JSONArray statesData = new JSONArray();
        for (State s : Model.getInstance().getCurrentAutomata().getStates()){
            JSONObject singleStateData = new JSONObject();
            singleStateData.put("name", s.getName());
            singleStateData.put("begin", s.isStateBegin());
            singleStateData.put("end", s.isStateEnd());
            singleStateData.put("layoutX", stateMapping.get(s).getCell().getGraphic(null).getLayoutX());
            singleStateData.put("layoutY", stateMapping.get(s).getCell().getGraphic(null).getLayoutY());
            statesData.put(singleStateData);
        }
        data.put("states", statesData);

        JSONArray transitionData = new JSONArray();
        for (Transition t : Model.getInstance().getCurrentAutomata().getTransitions()){
            JSONObject singleTransitionData = new JSONObject();
            singleTransitionData.put("source", t.getStateSource().getName());
            singleTransitionData.put("character", t.getCharacter());
            singleTransitionData.put("destination", t.getStateDestination().getName());
            transitionData.put(singleTransitionData);
        }
        data.put("transitions", transitionData);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", ".json"));
        File selectedFile = fileChooser.showSaveDialog(view1_btn.getScene().getWindow());
        if (selectedFile != null) {

            try (BufferedWriter bw = Files.newBufferedWriter(selectedFile.toPath(), StandardCharsets.UTF_8)) {
                bw.write(data.toString());
            } catch (Exception ignored){
            }
        }

    }

    public void loadData() throws JSONException {
        StringBuilder stringData = new StringBuilder();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(view1_btn.getScene().getWindow());
        if (selectedFile != null) {

            try (BufferedReader br = Files.newBufferedReader(selectedFile.toPath(), StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    stringData.append(line);
                }
            } catch (Exception e) {
                //TODO
            }
        } else {
            return;
        }

        Model.getInstance().setStateMapping(new HashMap<>());

        JSONObject data = new JSONObject(stringData.toString());
        Automata a = Model.getInstance().getCurrentAutomata();
        a.getStates().clear();
        a.getTransitions().clear();

        Map<String, State> tmpStates = new HashMap<>();
        for (Object o : data.getJSONArray("states")){
            if (o instanceof JSONObject){
                JSONObject stateData = (JSONObject)o;
                State s = new State(stateData.getString("name"));
                s.setStateBegin(stateData.getBoolean("begin"));
                s.setStateEnd(stateData.getBoolean("end"));
                a.addState(s);
                tmpStates.put(s.getName(), s);
                Model.getInstance().getStateMapping().put(s, null);
            }
        }
        for (Object o : data.getJSONArray("transitions")){
            if (o instanceof JSONObject){
                JSONObject transitionData = (JSONObject)o;
                State source = tmpStates.get(transitionData.getString("source"));
                State destination = tmpStates.get(transitionData.getString("destination"));
                a.addTransition(new Transition(source, transitionData.getString("character"), destination));
            }
        }
        Model.getInstance().setCurrentAutomata(a);

        Model.getInstance().setUpdateStateMapping(true);

        for (Object o : data.getJSONArray("states")){
            if (o instanceof JSONObject){
                JSONObject stateData = (JSONObject)o;
                State s = tmpStates.get(stateData.getString("name"));

                Model.getInstance().getStateMapping().get(s).getCell().getGraphic(null).setLayoutX(stateData.getDouble("layoutX"));
                Model.getInstance().getStateMapping().get(s).getCell().getGraphic(null).setLayoutY(stateData.getDouble("layoutY"));
            }
        }
        Model.getInstance().setUpdateView1(true);
        Model.getInstance().setUpdateView2(true);
        Model.getInstance().setUpdateView3(true);
    }
}
