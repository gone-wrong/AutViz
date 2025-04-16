package sk.ukf.autviz.Utils;

import com.fxgraph.graph.ICell;
import com.fxgraph.edges.AbstractEdge;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sk.ukf.autviz.Models.Transition;

public class DirectedLoop extends AbstractEdge {

    private final StringProperty textProperty;
    private Transition transition;  // referencia na modelový prechod

    public DirectedLoop(ICell source, Transition transition) {
        // Pre self-loop source aj target sú ten istý.
        super(source, source);
        textProperty = new SimpleStringProperty();
        this.transition = transition;
    }

    public StringProperty textProperty() {
        return textProperty;
    }

    public Transition getTransition() {
        return transition;
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    @Override
    public DirectedLoopGraphic getGraphic(com.fxgraph.graph.Graph graph) {
        return new DirectedLoopGraphic(graph, this);
    }
}
