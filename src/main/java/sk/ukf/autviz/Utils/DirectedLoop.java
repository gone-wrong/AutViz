package sk.ukf.autviz.Utils;

import com.fxgraph.graph.ICell;
import com.fxgraph.edges.AbstractEdge;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DirectedLoop extends AbstractEdge {

    private final StringProperty textProperty;

    public DirectedLoop(ICell source) {
        // Pre self-loop source aj target sú ten istý.
        super(source, source);
        textProperty = new SimpleStringProperty();
    }

    public StringProperty textProperty() {
        return textProperty;
    }

    @Override
    public DirectedLoopGraphic getGraphic(com.fxgraph.graph.Graph graph) {
        return new DirectedLoopGraphic(graph, this, textProperty);
    }
}
