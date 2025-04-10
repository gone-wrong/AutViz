package sk.ukf.autviz.Utils;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import javafx.scene.control.TextInputDialog;
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

        // ak je stav počiatočný (stateBegin), pridaj trojuholníkový indikátor.
        if (state.isStateBegin() && showBeginIndicator) {
            // trojuholník, ktorý bude slúžiť ako indikátor počiatočného stavu.
            Polygon triangle = new Polygon();
            // body trojuholníka tak, aby jeho "hrot" bol mimo kruhu,
            // ale celý indikátor sa nepresunul – použijeme fixný offset.
            triangle.getPoints().addAll(
                    20.0, 25.0,
                    0.0, 0.0,
                    0.0, 50.0
            );
            triangle.setFill(Color.TRANSPARENT);
            triangle.setStroke(Color.BLACK);
            triangle.setStrokeWidth(2);
            triangle.setTranslateX(-30);

            basePane.getChildren().add(triangle);
        }

        basePane.getChildren().add(circlePane);

        // ak je stav koncový (stateEnd), pridaj vonkajší kruh (outer border)
        if (state.isStateEnd()) {
            Circle outer = new Circle(32);
            outer.setFill(Color.TRANSPARENT);
            outer.setStroke(Color.DARKBLUE);
            outer.setStrokeWidth(1);

            StackPane outerPane = new StackPane();
            outerPane.setPrefSize(64, 64);
            outerPane.getChildren().addAll(outer, basePane);
            view = outerPane;
        } else {
            view = basePane;
        }

        view.setOnMouseClicked(event -> {
            if (Model.getInstance().isEditMode()) {
                TextInputDialog dialog = new TextInputDialog(label.getText());
                dialog.setTitle("Edit State");
                dialog.setHeaderText("Uprav názov stavu");
                dialog.setContentText("Nový názov:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(state::setStateName);
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