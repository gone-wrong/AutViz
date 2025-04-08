package sk.ukf.autviz.Utils;

import com.fxgraph.graph.ICell;
import com.fxgraph.edges.AbstractEdge;
import com.fxgraph.graph.Graph;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;
import sk.ukf.autviz.Utils.DirectedEdgeGraphic;

public class DirectedEdge extends AbstractEdge {

    private final StringProperty textProperty;

    public DirectedEdge(ICell source, ICell target) {
        // Predpokladáme, že DirectedEdge je vždy smerovaná.
        super(source, target);
        textProperty = new SimpleStringProperty();
    }

    public StringProperty textProperty() {
        return textProperty;
    }

    @Override
    public DirectedEdgeGraphic getGraphic(Graph graph) {
        return new DirectedEdgeGraphic(graph, this, textProperty);
    }
}