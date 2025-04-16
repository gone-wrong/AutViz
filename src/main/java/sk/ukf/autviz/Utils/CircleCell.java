package sk.ukf.autviz.Utils;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import sk.ukf.autviz.Models.State;
import sk.ukf.autviz.Models.Model;

import java.util.Optional;


public class CircleCell extends AbstractCell {

    private Region view;
    private State state;
    private Text label;

    private final Polygon beginIndicator;
    private final Circle endIndicator;

    // state Objekt typu State, ktorý obsahuje názov a príznaky.
    public CircleCell(State state, boolean customStyle) {
        this.state = state;
        // kruh pre stav.
        Circle circle = new Circle(30);
        if (customStyle) {
            circle.fillProperty().bind(
                    Bindings.when(state.isActiveProperty())
                            .then(Color.GREEN)
                            .otherwise(Color.LIGHTBLUE)
            );
        } else {
            circle.setFill(Color.LIGHTBLUE);
        }
        circle.setStroke(Color.DARKBLUE);
        circle.setStrokeWidth(1);

        // textový prvok so štítkom stavu.
        label = new Text();
        label.textProperty().bind(state.nameProperty());

        StackPane circlePane = new StackPane(circle, label);
        circlePane.setPrefSize(60, 60);

        StackPane basePane = new StackPane();
        basePane.setPrefSize(60, 60);

        beginIndicator = new Polygon(
                20.0, 25.0,
                0.0, 0.0,
                0.0, 50.0
        );
        beginIndicator.setFill(Color.TRANSPARENT);
        beginIndicator.setStroke(Color.BLACK);
        beginIndicator.setStrokeWidth(2);
        beginIndicator.setTranslateX(-30);
        if (customStyle) {
            beginIndicator.visibleProperty().bind(state.stateBeginProperty());
            basePane.getChildren().add(beginIndicator);
        }

        basePane.getChildren().add(circlePane);

        endIndicator = new Circle(34);
        endIndicator.setFill(Color.TRANSPARENT);
        endIndicator.setStroke(Color.DARKBLUE);
        endIndicator.setStrokeWidth(1);
        endIndicator.visibleProperty().bind(state.stateEndProperty());

        StackPane outerPane = new StackPane();
        outerPane.setPrefSize(68, 68);
        outerPane.getChildren().addAll(endIndicator, basePane);

        view = outerPane;

        view.setOnMouseClicked(event -> {
            if (Model.getInstance().isEditMode()) {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit State");
                dialog.setHeaderText("Editovanie stavu");

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                TextField nameField = new TextField();
                nameField.setText(state.getName());

                CheckBox beginCheck = new CheckBox("Začiatočný");
                beginCheck.setSelected(state.isStateBegin());
                boolean originalBegin = state.isStateBegin();
                CheckBox endCheck = new CheckBox("Koncový");
                endCheck.setSelected(state.isStateEnd());

                grid.add(new Label("Nové meno stavu:"), 0, 0);
                grid.add(nameField, 1, 0);
                grid.add(beginCheck, 1, 1);
                grid.add(endCheck, 1, 2);

                dialog.getDialogPane().setContent(grid);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);

                nameField.textProperty().addListener((obs, oldText, newText) -> {
                    String trimmed = newText.trim();
                    boolean disable = trimmed.isEmpty() || isDuplicate(trimmed, state);
                    okButton.setDisable(disable);
                });

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    String newName = nameField.getText().trim();
                    if (newName.isEmpty() || isDuplicate(newName, state)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid State Name");
                        alert.setHeaderText(null);
                        alert.setContentText("Meno stavu nemôže byť prázdne alebo duplicitné.");
                        alert.showAndWait();
                        return;
                    }
                    state.setStateName(newName);
                    state.setStateBegin(beginCheck.isSelected());
                    state.setStateEnd(endCheck.isSelected());
                    if(originalBegin != beginCheck.isSelected()) {
                        Model.getInstance().setUpdateView3(true);
                    }
                }
            }
        });
    }

    private boolean isDuplicate(String name, State currentState) {
        for (State s : Model.getInstance().getCurrentAutomata().getStates()) {
            if (s != currentState && s.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public State getState() {
        return state;
    }

    public CircleCell(String label) {
        this(new State(label), true);
    }

    @Override
    public Region getGraphic(Graph graph) {
        return view;
    }
}