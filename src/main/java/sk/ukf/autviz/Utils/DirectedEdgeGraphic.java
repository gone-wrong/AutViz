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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import sk.ukf.autviz.Models.Model;

import java.util.Optional;

public class DirectedEdgeGraphic extends Region {

    private final Group group;
    private final Line line;
    private final Line arrowLine1;
    private final Line arrowLine2;
    private final Text text;
    // "edit rectangle" pre lepšie zachytávanie kliknutí
    private final Rectangle editRect;

    private final double arrowLength = 10;
    private final double arrowWidth = 7;

    public DirectedEdgeGraphic(Graph graph, DirectedEdge edge, StringProperty textProperty) {
        group = new Group();
        line = new Line();
        arrowLine1 = new Line();
        arrowLine2 = new Line();
        text = new Text();
        editRect = new Rectangle();

        final DoubleBinding sourceX = edge.getSource().getXAnchor(graph, edge);
        final DoubleBinding sourceY = edge.getSource().getYAnchor(graph, edge);
        final DoubleBinding targetX = edge.getTarget().getXAnchor(graph, edge);
        final DoubleBinding targetY = edge.getTarget().getYAnchor(graph, edge);

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

        text.textProperty().bind(textProperty);
        text.getStyleClass().add("edge-text");
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

        // event handler pre editáciu textu, ak je edit mode
        editRect.setOnMouseClicked(event -> {
            if (Model.getInstance().isEditMode()) {
                // TextInputDialog, ktorý predvyplní aktuálny symbol prechodu.
                TextInputDialog dialog = new TextInputDialog(text.getText());
                dialog.setTitle("Edit Edge");
                dialog.setHeaderText("Uprav prechodový symbol");
                dialog.setContentText("Nový symbol:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newText -> {
                    text.setText(newText);
                    edge.textProperty().set(newText);
                    edge.getTransition().setCharacter(newText);
                });
            }
        });

        getChildren().add(group);
        // vypnutie zachytávania klikov Region
        this.setPickOnBounds(false);
//        this.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
    }

    public Region getGraphic() {
        return this;
    }
}
