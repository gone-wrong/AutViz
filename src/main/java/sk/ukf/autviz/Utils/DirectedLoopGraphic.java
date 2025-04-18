package sk.ukf.autviz.Utils;

import com.fxgraph.graph.Graph;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import sk.ukf.autviz.Models.Model;

import java.util.Optional;

public class DirectedLoopGraphic extends Region {

    private final Group group;
    private final CubicCurve curve;
    private final Line arrowLine1;
    private final Line arrowLine2;
    private final Text text;
    private final Rectangle editRect;

    private final double arrowLength = 10;
    private final double arrowWidth = 7;

    private final double nodeRadius = 30;
    private final double offsetAngle = Math.toRadians(30);
    private final double controlOffset = 40; // posun nahor pre kontrolné body

    public DirectedLoopGraphic(Graph graph, DirectedLoop edge) {
        group = new Group();
        curve = new CubicCurve();
        arrowLine1 = new Line();
        arrowLine2 = new Line();
        text = new Text();
        editRect = new Rectangle();

        Model.getInstance().editModeProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                editRect.setStroke(Color.RED);
            } else {
                editRect.setStroke(Color.TRANSPARENT);
            }
        });

        Model.getInstance().deleteEdgeModeProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                editRect.setStroke(Color.RED);
            } else {
                editRect.setStroke(Color.TRANSPARENT);
            }
        });

        // stred uzla (pre self-loop je zdroj = cieľ)
        final DoubleBinding centerX = edge.getSource().getXAnchor(graph, edge);
        final DoubleBinding centerY = edge.getSource().getYAnchor(graph, edge);

        double baseAngle = -Math.PI / 2;

        // vypočítanie bodov na obvode kruhu:
        DoubleBinding aX = Bindings.createDoubleBinding(() ->
                centerX.get() + nodeRadius * Math.cos(baseAngle - offsetAngle), centerX);
        DoubleBinding aY = Bindings.createDoubleBinding(() ->
                centerY.get() + nodeRadius * Math.sin(baseAngle - offsetAngle), centerY);

        DoubleBinding bX = Bindings.createDoubleBinding(() ->
                centerX.get() + nodeRadius * Math.cos(baseAngle + offsetAngle) - 1, centerX);
        DoubleBinding bY = Bindings.createDoubleBinding(() ->
                centerY.get() + nodeRadius * Math.sin(baseAngle + offsetAngle) - 1, centerY);

        // body CubicCurve:
        // začiatok = bod a, koniec = bod b.
        curve.startXProperty().bind(aX);
        curve.startYProperty().bind(aY);
        curve.endXProperty().bind(bX);
        curve.endYProperty().bind(bY);

        // kontrolné body
        DoubleBinding controlX1 = aX;
        DoubleBinding controlY1 = Bindings.createDoubleBinding(() ->
                aY.get() - controlOffset, aY);
        DoubleBinding controlX2 = bX;
        DoubleBinding controlY2 = Bindings.createDoubleBinding(() ->
                bY.get() - controlOffset, bY);

        curve.controlX1Property().bind(controlX1);
        curve.controlY1Property().bind(controlY1);
        curve.controlX2Property().bind(controlX2);
        curve.controlY2Property().bind(controlY2);

        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(1);
        curve.setFill(Color.TRANSPARENT);

        group.getChildren().add(curve);

        // Arrow head: derivácia CubicCurve v bode t=1, ktorá je 3*(end - control2).
        arrowLine1.endXProperty().bind(curve.endXProperty());
        arrowLine1.endYProperty().bind(curve.endYProperty());
        arrowLine2.endXProperty().bind(curve.endXProperty());
        arrowLine2.endYProperty().bind(curve.endYProperty());
        arrowLine1.setStrokeWidth(2);
        arrowLine2.setStrokeWidth(2);

        Runnable updateArrow = () -> {
            double cX2 = curve.getControlX2();
            double cY2 = curve.getControlY2();
            double ex = curve.getEndX();
            double ey = curve.getEndY();
            double dx = 3 * (ex - cX2);
            double dy = 3 * (ey - cY2);
            double d = Math.hypot(dx, dy);
            if (d < 0.1) {
                arrowLine1.setStartX(ex);
                arrowLine1.setStartY(ey);
                arrowLine2.setStartX(ex);
                arrowLine2.setStartY(ey);
            } else {
                double factor = arrowLength / d;
                double factorO = arrowWidth / d;
                double dx2 = dx * factor;
                double dy2 = dy * factor;
                double ox = dx * factorO;
                double oy = dy * factorO;
                arrowLine1.setStartX(ex - dx2 - oy);
                arrowLine1.setStartY(ey - dy2 + ox);
                arrowLine2.setStartX(ex - dx2 + oy);
                arrowLine2.setStartY(ey - dy2 - ox);
            }
        };

        curve.controlX2Property().addListener(e -> updateArrow.run());
        curve.controlY2Property().addListener(e -> updateArrow.run());
        curve.endXProperty().addListener(e -> updateArrow.run());
        curve.endYProperty().addListener(e -> updateArrow.run());
        Platform.runLater(updateArrow);
        group.getChildren().addAll(arrowLine1, arrowLine2);

        // text v strede krivky
        text.textProperty().bind(edge.getTransition().characterProperty());
        final DoubleProperty textWidth = new SimpleDoubleProperty();
        final DoubleProperty textHeight = new SimpleDoubleProperty();
        text.xProperty().bind(controlX1.add(controlX2).divide(2).subtract(textWidth.divide(2)));
        text.yProperty().bind(controlY1);
        Runnable recalculateTextBounds = () -> {
            textWidth.set(text.getLayoutBounds().getWidth());
            textHeight.set(text.getLayoutBounds().getHeight());
        };
        text.boundsInLocalProperty().addListener((obs, oldVal, newVal) -> recalculateTextBounds.run());
        text.textProperty().addListener((obs, oldVal, newVal) -> recalculateTextBounds.run());

        group.getChildren().add(text);

        final double EDIT_EXTRA = 10; // extra šírka
        final double EDIT_HEIGHT = 20;
        editRect.setFill(Color.TRANSPARENT);

        DoubleBinding controlDistance = Bindings.createDoubleBinding(() ->
                        Math.hypot(curve.getControlX2() - curve.getControlX1(), curve.getControlY2() - curve.getControlY1()),
                curve.controlX1Property(), curve.controlX2Property(), curve.controlY1Property(), curve.controlY2Property());

        editRect.widthProperty().bind(controlDistance.add(EDIT_EXTRA));
        editRect.setHeight(EDIT_HEIGHT);

        DoubleBinding midX = curve.controlX1Property().add(curve.controlX2Property()).divide(2);
        DoubleBinding midY = curve.controlY1Property().add(curve.controlY2Property()).divide(2);
        editRect.layoutXProperty().bind(midX.subtract(editRect.widthProperty().divide(2)));
        editRect.layoutYProperty().bind(midY.subtract(EDIT_HEIGHT / 2));

        group.getChildren().add(editRect);

        editRect.setOnMouseClicked(event -> {
            if (Model.getInstance().isEditMode()) {
                TextInputDialog dialog = new TextInputDialog(edge.getTransition().getCharacter());
                dialog.setTitle("Edit Edge Symbols");
                dialog.setHeaderText("Uprav prechodové symboly");
                dialog.setContentText("Zadaj nové symboly (oddelené čiarkou):");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newSymbols -> {
                    edge.getTransition().setSymbols(newSymbols);
                });
                Model.getInstance().getCurrentAutomata().updateAlphabet();
            }
            if (Model.getInstance().isDeleteEdgeMode()) {
                Model.getInstance().getCurrentAutomata().removeTransition(edge.getTransition());
                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(true);
            }
        });

        getChildren().add(group);
        recalculateTextBounds.run();
        this.setPickOnBounds(false);
    }

    public Region getGraphic() {
        return this;
    }
}