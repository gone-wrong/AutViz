package sk.ukf.autviz.Utils;

import com.fxgraph.graph.Graph;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class DirectedLoopGraphic extends Region {

    private final Group group;
    private final CubicCurve curve;
    private final Line arrowLine1;
    private final Line arrowLine2;
    private final Text text;

    // Parametre pre arrow head – upraviteľné podľa vzhľadu
    private final double arrowLength = 10;
    private final double arrowWidth = 7;

    // Parametre pre self-loop
    private final double nodeRadius = 30;
    private final double offsetAngle = Math.toRadians(30); // 30° v radianoch
    private final double controlOffset = 40; // posun nahor pre kontrolné body

    public DirectedLoopGraphic(Graph graph, DirectedLoop edge, StringProperty textProperty) {
        group = new Group();
        curve = new CubicCurve();
        arrowLine1 = new Line();
        arrowLine2 = new Line();
        text = new Text();

        // Získaj stred uzla (pre self-loop je zdroj = cieľ)
        final DoubleBinding centerX = edge.getSource().getXAnchor(graph, edge);
        final DoubleBinding centerY = edge.getSource().getYAnchor(graph, edge);

        // Východiskový uhol pre vrchný bod kruhu je -90° (alebo -PI/2)
        double baseAngle = -Math.PI / 2;

        // Vypočítaj body na obvode kruhu:
        // Bod a: rotácia o -offsetAngle od vrchu (posunutie doľava)
        DoubleBinding aX = Bindings.createDoubleBinding(() ->
                centerX.get() + nodeRadius * Math.cos(baseAngle - offsetAngle), centerX);
        DoubleBinding aY = Bindings.createDoubleBinding(() ->
                centerY.get() + nodeRadius * Math.sin(baseAngle - offsetAngle), centerY);

        // Bod b: rotácia o +offsetAngle od vrchu (posunutie doprava)
        DoubleBinding bX = Bindings.createDoubleBinding(() ->
                centerX.get() + nodeRadius * Math.cos(baseAngle + offsetAngle) - 1, centerX);
        DoubleBinding bY = Bindings.createDoubleBinding(() ->
                centerY.get() + nodeRadius * Math.sin(baseAngle + offsetAngle) - 1, centerY);

        // Nastavíme body CubicCurve:
        // Začiatok = bod a, koniec = bod b.
        curve.startXProperty().bind(aX);
        curve.startYProperty().bind(aY);
        curve.endXProperty().bind(bX);
        curve.endYProperty().bind(bY);

        // Kontrolné body: budú rovnaké ako body a, b, ale posunuté nahor (znížená hodnota y)
        DoubleBinding controlX1 = aX; // môžeme ponechať x rovnaké
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

        // Arrow head: Vypočítame deriváciu CubicCurve v bode t=1, ktorá je 3*(end - control2).
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

        // Nastav textový štítok – umiestni ho približne v strede krivky
        text.textProperty().bind(textProperty);
        double width = text.getBoundsInLocal().getWidth();
        text.xProperty().bind(Bindings.createDoubleBinding(() ->
                ((curve.getControlX1() + curve.getControlX2()) / 2) - width / 2, curve.controlX1Property(), curve.controlX2Property()));
        text.yProperty().bind(Bindings.createDoubleBinding(() ->
                (curve.getControlY1() + curve.getControlY2()) / 2, curve.controlY1Property(), curve.controlY2Property()));

        group.getChildren().add(text);

        getChildren().add(group);
    }

    public Region getGraphic() {
        return this;
    }
}