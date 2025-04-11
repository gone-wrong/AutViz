package sk.ukf.autviz.Utils;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    public CircleCell(State state, boolean showBeginIndicator) {
        this.state = state;
        // kruh pre stav.
        Circle circle = new Circle(30);
        circle.setFill(Color.LIGHTBLUE);
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
        // Bind its visibility to state.begin (only if we want to show the indicator).
        if (showBeginIndicator) {
            beginIndicator.visibleProperty().bind(state.stateBeginProperty());
            basePane.getChildren().add(beginIndicator);
        }

        // Add the main circle and label.
        basePane.getChildren().add(circlePane);

        // Create the end indicator as an outer circle.
        endIndicator = new Circle(34);
        endIndicator.setFill(Color.TRANSPARENT);
        endIndicator.setStroke(Color.DARKBLUE);
        endIndicator.setStrokeWidth(1);
        // Bind its visibility to the state's end property.
        endIndicator.visibleProperty().bind(state.stateEndProperty());

        // Place both the outer circle and the basePane into an outer pane.
        StackPane outerPane = new StackPane();
        outerPane.setPrefSize(68, 68);
        outerPane.getChildren().addAll(endIndicator, basePane);

        view = outerPane;

        view.setOnMouseClicked(event -> {
            if (Model.getInstance().isEditMode()) {
                // Create a custom dialog with a grid pane layout.
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit State");
                dialog.setHeaderText("Edit state properties");

                // Create a GridPane for the input controls.
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                // Create a TextField prepopulated with the current state name.
                TextField nameField = new TextField();
                nameField.setText(state.getName());

                // Create CheckBoxes for "begin" and "end" flags.
                CheckBox beginCheck = new CheckBox("Beginning State");
                beginCheck.setSelected(state.isStateBegin());
                CheckBox endCheck = new CheckBox("Ending State");
                endCheck.setSelected(state.isStateEnd());

                // Add labels and controls to the grid.
                grid.add(new Label("State name:"), 0, 0);
                grid.add(nameField, 1, 0);
                grid.add(beginCheck, 1, 1);
                grid.add(endCheck, 1, 2);

                dialog.getDialogPane().setContent(grid);

                // Add OK and Cancel buttons.
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Show the dialog and wait for user input.
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Update the state with the new values.
                    state.setStateName(nameField.getText());
                    state.setStateBegin(beginCheck.isSelected());
                    state.setStateEnd(endCheck.isSelected());
                }
            }
        });
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