package sk.ukf.autviz.Utils;

import com.fxgraph.graph.Graph;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle; // Debug
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import sk.ukf.autviz.Models.Model;

import java.util.Optional;

public class DirectedEdgeGraphic extends Region {

    private final Group group = new Group();
    private Line line;
    private CubicCurve curve;
    private final Line arrowLine1 = new Line();
    private final Line arrowLine2 = new Line();
    private final Text text = new Text();
    private final Rectangle editRect = new Rectangle();

    private final double arrowLength = 10;
    private final double arrowWidth = 7;

    private final double curveOffset = 30;

    private final DirectedEdge edge;
    private final Graph graph;

    public DirectedEdgeGraphic(Graph graph, DirectedEdge edge) {
        this.graph = graph;
        this.edge = edge;

        text.textProperty().bind(edge.getTransition().characterProperty());
        text.getStyleClass().add("edge-text");
        editRect.setWidth(30);
        rebuildGraphic();

        edge.getTransition().hasOppositeProperty().addListener(
                (obs, oldVal, newVal) -> Platform.runLater(this::rebuildGraphic));

        editRect.setOnMouseClicked(event -> {
            if (Model.getInstance().isEditMode()) {
                TextInputDialog dialog = new TextInputDialog(edge.getTransition().getCharacter());
                dialog.setTitle("Edit Edge Symbols");
                dialog.setHeaderText("Uprav prechodové symboly");
                dialog.setContentText("Zadaj nové symboly (oddelené čiarkou):");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newSymbols -> edge.getTransition().setSymbols(newSymbols));
            }
            if (Model.getInstance().isDeleteEdgeMode()) {
                Model.getInstance().getCurrentAutomata().removeTransition(edge.getTransition());
                Model.getInstance().setUpdateView1(true);
            }
        });

        getChildren().setAll(group);
        this.setPickOnBounds(false);
    }


    private void rebuildGraphic() {
        if (line != null) {
            line.startXProperty().unbind();
            line.startYProperty().unbind();
            line.endXProperty().unbind();
            line.endYProperty().unbind();
        }
        if (curve != null) {
            curve.startXProperty().unbind();
            curve.startYProperty().unbind();
            curve.endXProperty().unbind();
            curve.endYProperty().unbind();
            curve.controlX1Property().unbind();
            curve.controlY1Property().unbind();
            curve.controlX2Property().unbind();
            curve.controlY2Property().unbind();
        }
        arrowLine1.startXProperty().unbind();
        arrowLine1.startYProperty().unbind();
        arrowLine1.endXProperty().unbind();
        arrowLine1.endYProperty().unbind();

        arrowLine2.startXProperty().unbind();
        arrowLine2.startYProperty().unbind();
        arrowLine2.endXProperty().unbind();
        arrowLine2.endYProperty().unbind();

        text.xProperty().unbind();
        text.yProperty().unbind();

        group.getChildren().clear();

        final DoubleBinding sourceX = edge.getSource().getXAnchor(graph, edge);
        final DoubleBinding sourceY = edge.getSource().getYAnchor(graph, edge);
        final DoubleBinding targetX = edge.getTarget().getXAnchor(graph, edge);
        final DoubleBinding targetY = edge.getTarget().getYAnchor(graph, edge);

        boolean isCurved = edge.getTransition().hasOppositeProperty().get();

        if (isCurved && !edge.isStateTreeViz()) {
            curve = new CubicCurve();
            final double radius = 30.0;

            final double offsetAngle = 10.0;
            final double offsetAngleRad = Math.toRadians(offsetAngle);

            DoubleBinding dx = targetX.subtract(sourceX);
            DoubleBinding dy = targetY.subtract(sourceY);

            DoubleBinding baseAngle = Bindings.createDoubleBinding(() ->
                    Math.atan2(dy.get(), dx.get()), dx, dy);

            DoubleBinding newStartX = Bindings.createDoubleBinding(
                    () -> sourceX.get() + radius * Math.cos(baseAngle.get() + offsetAngleRad),
                    sourceX, baseAngle);
            DoubleBinding newStartY = Bindings.createDoubleBinding(
                    () -> sourceY.get() + radius * Math.sin(baseAngle.get() + offsetAngleRad),
                    sourceY, baseAngle);

            DoubleBinding newEndX = Bindings.createDoubleBinding(
                    () -> targetX.get() - radius * Math.cos(baseAngle.get() - offsetAngleRad),
                    targetX, baseAngle);
            DoubleBinding newEndY = Bindings.createDoubleBinding(
                    () -> targetY.get() - radius * Math.sin(baseAngle.get() - offsetAngleRad),
                    targetY, baseAngle);


            curve.startXProperty().bind(newStartX);
            curve.startYProperty().bind(newStartY);
            curve.endXProperty().bind(newEndX);
            curve.endYProperty().bind(newEndY);


            DoubleBinding midX = sourceX.add(targetX).divide(2);
            DoubleBinding midY = sourceY.add(targetY).divide(2);

            DoubleBinding d = Bindings.createDoubleBinding(() -> {
                double dxVal = dx.get();
                double dyVal = dy.get();
                return Math.hypot(dxVal, dyVal);
            }, dx, dy);

            DoubleBinding controlX = Bindings.createDoubleBinding(() -> {
                double mid = midX.get();
                double dyVal = dy.get();
                double dVal = d.get();
                return mid - (dVal > 0 ? (dyVal / dVal * curveOffset) : 0);
            }, midX, dy, d);
            DoubleBinding controlY = Bindings.createDoubleBinding(() -> {
                double mid = midY.get();
                double dxVal = dx.get();
                double dVal = d.get();
                return mid + (dVal > 0 ? (dxVal / dVal * curveOffset) : 0);
            }, midY, dx, d);
            curve.controlX1Property().bind(controlX);
            curve.controlY1Property().bind(controlY);
            curve.controlX2Property().bind(controlX);
            curve.controlY2Property().bind(controlY);

            curve.setStroke(Color.BLACK);
            curve.setStrokeWidth(1);
            curve.setFill(Color.TRANSPARENT);

            group.getChildren().add(curve);
//            Debug
//            Circle controlMarker1 = new Circle(4, Color.RED);
//            Circle controlMarker2 = new Circle(4, Color.RED);
//
//            controlMarker1.centerXProperty().bind(curve.controlX1Property());
//            controlMarker1.centerYProperty().bind(curve.controlY1Property());
//            controlMarker2.centerXProperty().bind(curve.controlX2Property());
//            controlMarker2.centerYProperty().bind(curve.controlY2Property());
//
//            group.getChildren().addAll(controlMarker1, controlMarker2);

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
                double dxA = 3 * (ex - cX2);
                double dyA = 3 * (ey - cY2);
                double dA = Math.hypot(dxA, dyA);
                if (dA < 0.1) {
                    arrowLine1.setStartX(ex);
                    arrowLine1.setStartY(ey);
                    arrowLine2.setStartX(ex);
                    arrowLine2.setStartY(ey);
                } else {
                    double factor = arrowLength / dA;
                    double factorO = arrowWidth / dA;
                    double dx2 = dxA * factor;
                    double dy2 = dyA * factor;
                    double ox = dxA * factorO;
                    double oy = dyA * factorO;
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

            final DoubleProperty textWidth = new SimpleDoubleProperty();
            final DoubleProperty textHeight = new SimpleDoubleProperty();
            text.xProperty().bind(controlX.subtract(textWidth.divide(2)));
            text.yProperty().bind(controlY.subtract(textHeight.divide(2)));
            Runnable recalculateTextBounds = () -> {
                textWidth.set(text.getLayoutBounds().getWidth());
                textHeight.set(text.getLayoutBounds().getHeight());
            };
            text.boundsInLocalProperty().addListener((obs, oldVal, newVal) -> recalculateTextBounds.run());
            text.textProperty().addListener((obs, oldVal, newVal) -> recalculateTextBounds.run());
            group.getChildren().add(text);

            editRect.setFill(Color.TRANSPARENT);
            // debugging
            editRect.setStroke(Color.RED);
            DoubleBinding edgeLength = Bindings.createDoubleBinding(() -> {
                double dxE = targetX.get() - sourceX.get();
                double dyE = targetY.get() - sourceY.get();
                return Math.hypot(dxE, dyE);
            }, sourceX, sourceY, targetX, targetY);
            editRect.heightProperty().bind(edgeLength.divide(4));

            editRect.xProperty().bind(controlX.subtract(editRect.widthProperty().divide(2)));
            editRect.yProperty().bind(controlY.subtract(editRect.heightProperty().divide(2)));

            DoubleBinding angle = Bindings.createDoubleBinding(() -> {
                double dxE = targetX.get() - sourceX.get();
                double dyE = targetY.get() - sourceY.get();
                return Math.toDegrees(Math.atan2(dyE, dxE));
            }, sourceX, sourceY, targetX, targetY);
            editRect.rotateProperty().bind(angle.subtract(90));

            group.getChildren().add(editRect);
            recalculateTextBounds.run();

        } else {
            line = new Line();
            line.startXProperty().bind(sourceX);
            line.startYProperty().bind(sourceY);
            line.endXProperty().bind(targetX);
            line.endYProperty().bind(targetY);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1);
            group.getChildren().add(line);

            arrowLine1.setStroke(Color.BLACK);
            arrowLine1.setStrokeWidth(2);
            arrowLine2.setStroke(Color.BLACK);
            arrowLine2.setStrokeWidth(2);

            Runnable updateArrow = () -> {
                double sx = line.getStartX();
                double sy = line.getStartY();
                double ex = line.getEndX();
                double ey = line.getEndY();
                double dx = ex - sx;
                double dy = ey - sy;
                double d = Math.hypot(dx, dy);
                if (d > 0) {
                    double r = 31;
                    ex = ex - (dx / d * r);
                    ey = ey - (dy / d * r);
                }
                arrowLine1.setEndX(ex);
                arrowLine1.setEndY(ey);
                arrowLine2.setEndX(ex);
                arrowLine2.setEndY(ey);

                if (ex == sx && ey == sy) {
                    arrowLine1.setStartX(ex);
                    arrowLine1.setStartY(ey);
                    arrowLine2.setStartX(ex);
                    arrowLine2.setStartY(ey);
                } else {
                    double distance = Math.hypot(sx - ex, sy - ey);
                    double factor = arrowLength / distance;
                    double factorO = arrowWidth / distance;
                    double dx2 = (sx - ex) * factor;
                    double dy2 = (sy - ey) * factor;
                    double ox = (sx - ex) * factorO;
                    double oy = (sy - ey) * factorO;
                    arrowLine1.setStartX(ex + dx2 - oy);
                    arrowLine1.setStartY(ey + dy2 + ox);
                    arrowLine2.setStartX(ex + dx2 + oy);
                    arrowLine2.setStartY(ey + dy2 - ox);
                }
            };

            line.startXProperty().addListener(e -> updateArrow.run());
            line.startYProperty().addListener(e -> updateArrow.run());
            line.endXProperty().addListener(e -> updateArrow.run());
            line.endYProperty().addListener(e -> updateArrow.run());
            Platform.runLater(updateArrow);

            group.getChildren().addAll(arrowLine1, arrowLine2);

            final DoubleBinding midX = sourceX.add(targetX).divide(2);
            final DoubleBinding midY = sourceY.add(targetY).divide(2);
            final DoubleProperty textWidth = new SimpleDoubleProperty();
            final DoubleProperty textHeight = new SimpleDoubleProperty();

            // centrovanie textu
            text.xProperty().bind(midX.subtract(textWidth.divide(2)));
            text.yProperty().bind(midY.subtract(textHeight.divide(2)));
            Runnable recalculateTextBounds = () -> {
                textWidth.set(text.getLayoutBounds().getWidth());
                textHeight.set(text.getLayoutBounds().getHeight());
            };
            text.boundsInLocalProperty().addListener((obs, oldVal, newVal) -> recalculateTextBounds.run());
            text.textProperty().addListener((obs, oldVal, newVal) -> recalculateTextBounds.run());
            group.getChildren().add(text);

            final double EDIT_RECT_WIDTH = 20;
            editRect.setFill(Color.TRANSPARENT);
            // debugging
            editRect.setStroke(Color.RED);
            editRect.setWidth(EDIT_RECT_WIDTH);
            // binding pre výpočet dĺžky hrany:
            DoubleBinding edgeLength = Bindings.createDoubleBinding(() -> {
                double dx = targetX.get() - sourceX.get();
                double dy = targetY.get() - sourceY.get();
                return Math.hypot(dx, dy);
            }, sourceX, sourceY, targetX, targetY);
            editRect.heightProperty().bind(edgeLength.divide(4));
            // editRect tak, aby jeho stred bol na strede hrany
            editRect.layoutXProperty().bind(midX.subtract(editRect.widthProperty().divide(2)));
            editRect.layoutYProperty().bind(midY.subtract(editRect.heightProperty().divide(2)));

            DoubleBinding angle = Bindings.createDoubleBinding(() -> {
                double dx = targetX.get() - sourceX.get();
                double dy = targetY.get() - sourceY.get();
                return Math.toDegrees(Math.atan2(dy, dx));
            }, sourceX, sourceY, targetX, targetY);
            editRect.rotateProperty().bind(angle.subtract(90));
            group.getChildren().add(editRect);
            recalculateTextBounds.run();
        }

        getChildren().setAll(group);
    }

    public Region getGraphic() {
        return this;
    }
}
