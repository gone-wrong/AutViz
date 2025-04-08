package sk.ukf.autviz.Utils;

import com.fxgraph.graph.Graph;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class DirectedEdgeGraphic extends Region {

    private final Group group;
    private final Line line;
    private final Line arrowLine1;
    private final Line arrowLine2;
    private final Text text;

    private final double arrowLength = 10;
    private final double arrowWidth = 7;

    public DirectedEdgeGraphic(Graph graph, DirectedEdge edge, StringProperty textProperty) {
        group = new Group();
        line = new Line();
        arrowLine1 = new Line();
        arrowLine2 = new Line();
        text = new Text();

        // Väzby na súradnice zdrojového a cieľového uzla
        final DoubleBinding sourceX = edge.getSource().getXAnchor(graph, edge);
        final DoubleBinding sourceY = edge.getSource().getYAnchor(graph, edge);
        final DoubleBinding targetX = edge.getTarget().getXAnchor(graph, edge);
        final DoubleBinding targetY = edge.getTarget().getYAnchor(graph, edge);

        // Viažeme vlastnosti hlavnej čiary
        line.startXProperty().bind(sourceX);
        line.startYProperty().bind(sourceY);
        line.endXProperty().bind(targetX);
        line.endYProperty().bind(targetY);

        // Nastavenie farby a hrúbky hlavnej čiary
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1);
        group.getChildren().add(line);

        // Nastavenie šípky – farba a hrúbka
        arrowLine1.setStroke(Color.BLACK);
        arrowLine1.setStrokeWidth(2);
        arrowLine2.setStroke(Color.BLACK);
        arrowLine2.setStrokeWidth(2);

        // Runnable, ktorý aktualizuje pozície šípky
        Runnable updateArrow = () -> {
            double sx = line.getStartX();
            double sy = line.getStartY();
            double ex = line.getEndX();
            double ey = line.getEndY();

            // Debug výpis pôvodných hodnôt
            //System.out.println("Original Source: (" + sx + ", " + sy + ")");
            //System.out.println("Original Target (center): (" + ex + ", " + ey + ")");

            // Upraviť cieľové súradnice tak, aby boli na okraji uzla (predpokladáme polomer = 30)
            double dx = ex - sx;
            double dy = ey - sy;
            double d = Math.hypot(dx, dy);
            if (d > 0) {
                double r = 31; // polomer cieľového kruhu
                ex = ex - (dx / d * r);
                ey = ey - (dy / d * r);
            }

            //System.out.println("Adjusted Target (edge): (" + ex + ", " + ey + ")");

            // Nastav koncové body arrowLine1 a arrowLine2 na upravené hodnoty
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

        // Pridaj listeneri pre aktualizáciu šípky
        line.startXProperty().addListener(e -> updateArrow.run());
        line.startYProperty().addListener(e -> updateArrow.run());
        line.endXProperty().addListener(e -> updateArrow.run());
        line.endYProperty().addListener(e -> updateArrow.run());

        // Pre istotu použijeme Platform.runLater, aby sa arrow aktualizovala po vykreslení layoutu
        Platform.runLater(updateArrow);

        group.getChildren().addAll(arrowLine1, arrowLine2);

        // Nastavenie textového štítku
        text.textProperty().bind(textProperty);
        text.getStyleClass().add("edge-text");
        // Jednoduché centrovanie textu – môžeš doladiť podľa potreby
        text.xProperty().bind(sourceX.add(targetX).divide(2).subtract(text.layoutBoundsProperty().get().getWidth() / 2));
        text.yProperty().bind(sourceY.add(targetY).divide(2).subtract(text.layoutBoundsProperty().get().getHeight() / 2));
        group.getChildren().add(text);

        getChildren().add(group);
    }

    public Region getGraphic() {
        return this;
    }
}
