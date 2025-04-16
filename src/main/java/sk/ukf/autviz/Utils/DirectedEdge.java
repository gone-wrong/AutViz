package sk.ukf.autviz.Utils;

import com.fxgraph.graph.ICell;
import com.fxgraph.edges.AbstractEdge;
import com.fxgraph.graph.Graph;
import sk.ukf.autviz.Models.Transition;

public class DirectedEdge extends AbstractEdge {

    private final Transition transition;  // referencia na modelový prechod
    private boolean StateTreeViz = false;

    public DirectedEdge(ICell source, ICell target, Transition transition) {
        super(source, target);
        this.transition = transition;
    }

    public Transition getTransition() {
        return transition;
    }

    @Override
    public DirectedEdgeGraphic getGraphic(Graph graph) {
        return new DirectedEdgeGraphic(graph, this);
    }

    public boolean isStateTreeViz() {
        return StateTreeViz;
    }

    public void setStateTreeViz(boolean stateTreeViz) {
        StateTreeViz = stateTreeViz;
    }
}
