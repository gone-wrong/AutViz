package sk.ukf.autviz.Utils;

import com.fxgraph.graph.ICell;
import com.fxgraph.edges.AbstractEdge;
import com.fxgraph.graph.Graph;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;
import sk.ukf.autviz.Models.Transition;

public class DirectedEdge extends AbstractEdge {

    private final StringProperty textProperty;
    private Transition transition;  // referencia na modelový prechod

    public DirectedEdge(ICell source, ICell target, Transition transition) {
        super(source, target);
        this.transition = transition;
        textProperty = new SimpleStringProperty();
        textProperty.set(transition.getCharacter());
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
    public DirectedEdgeGraphic getGraphic(Graph graph) {
        return new DirectedEdgeGraphic(graph, this, textProperty);
    }
}
