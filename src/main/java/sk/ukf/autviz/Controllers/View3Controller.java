package sk.ukf.autviz.Controllers;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import sk.ukf.autviz.Models.Automata;
import sk.ukf.autviz.Models.State;
import sk.ukf.autviz.Models.StateTreeNode;
import sk.ukf.autviz.Models.Transition;
import sk.ukf.autviz.Models.Model;
import sk.ukf.autviz.Utils.CircleCell;
import sk.ukf.autviz.Utils.DirectedEdge;

import java.net.URL;
import java.util.*;

public class View3Controller implements Initializable {

    public AnchorPane view3_parent;

    private Graph graph;
    private com.fxgraph.graph.Model graphModel; // FXGraph model

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing View3Controller");
        Automata automata = Model.getInstance().getCurrentAutomata();
        StateTreeNode rootNode = buildStateTree(automata);
        drawStateTree(rootNode);
    }

    private StateTreeNode buildStateTree(Automata automata) {
        Set<String> expanded = new HashSet<>();
        List<State> beginStates = new ArrayList<>();
        for (State s : automata.getStates()) {
            if (Boolean.TRUE.equals(s.isStateBegin())) {
                beginStates.add(s);
            }
        }

        if (beginStates.isEmpty() && !automata.getStates().isEmpty()) {
            beginStates.add(automata.getStates().iterator().next());
        }

        if (beginStates.size() > 1) {
            State dummy = new State("root");
            StateTreeNode root = new StateTreeNode(dummy, true, null);
            for (State s : beginStates) {
                root.addChild(buildStateTreeRecursive(s, automata, expanded, null));
            }
            return root;
        } else {
            return buildStateTreeRecursive(beginStates.get(0), automata, expanded, null);
        }
    }

    private StateTreeNode buildStateTreeRecursive(State current, Automata automata, Set<String> expanded, Transition transition) {
        if (expanded.contains(current.getName())) {
            return new StateTreeNode(current, false, transition);
        }
        expanded.add(current.getName());

        StateTreeNode node = new StateTreeNode(current, true, transition);
        // rekurzívne vytvoríme strom pre každý prechod vychádzajúci z current
        for (Transition t : automata.getTransitions()) {
            if (t.getStateSource().getName().equals(current.getName())) {
                StateTreeNode childNode = buildStateTreeRecursive(t.getStateDestination(), automata, expanded, t);
                node.addChild(childNode);
            }
        }
        return node;
    }

    private void drawStateTree(StateTreeNode root) {
        graph = new Graph();
        graphModel = graph.getModel();
        graph.beginUpdate();

        // mapa nodov stromu a ich grafických reprezentácií
        Map<StateTreeNode, ICell> mapping = new HashMap<>();
        addStateTreeNodes(root, mapping);

        graph.endUpdate();

        PositionWrapper posWrapper = new PositionWrapper(50);
        assignTreePositions(root, mapping, 0, posWrapper);

        BorderPane pane = new BorderPane(graph.getCanvas());
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        view3_parent.getChildren().add(pane);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view3_parent.widthProperty());
        clip.heightProperty().bind(view3_parent.heightProperty());
        view3_parent.setClip(clip);
    }

    private void addStateTreeNodes(StateTreeNode node, Map<StateTreeNode, ICell> mapping) {
        ICell cell = new CircleCell(node.getState(), false);
        graphModel.addCell(cell);
        mapping.put(node, cell);

        for (StateTreeNode child : node.getChildren()) {
            addStateTreeNodes(child, mapping);
            ICell childCell = mapping.get(child);
            if (child.getTransition() != null) {
                DirectedEdge edge = new DirectedEdge(cell, childCell, child.getTransition());
                edge.setStateTreeViz(true);
                graphModel.addEdge(edge);
            }
        }
    }

    private static class PositionWrapper {
        public double value;
        public PositionWrapper(double value) {
            this.value = value;
        }
    }

    private static final double HORIZONTAL_SPACING = 150;
    private static final double VERTICAL_SPACING = 100;

    private void assignTreePositions(StateTreeNode node, Map<StateTreeNode, ICell> mapping, int level, PositionWrapper posWrapper) {
        ICell cell = mapping.get(node);

        if (!node.getChildren().isEmpty()) {
            double startX = posWrapper.value;
            for (StateTreeNode child : node.getChildren()) {
                assignTreePositions(child, mapping, level + 1, posWrapper);
            }
            double endX = posWrapper.value;
            double parentX = (startX + endX) / 2.0;
            double parentY = level * VERTICAL_SPACING;

            cell.getGraphic(null).setLayoutX(parentX);
            cell.getGraphic(null).setLayoutY(parentY);
        } else {
            double x = posWrapper.value;
            double y = level * VERTICAL_SPACING;
            cell.getGraphic(null).setLayoutX(x);
            cell.getGraphic(null).setLayoutY(y);
            posWrapper.value += HORIZONTAL_SPACING;
        }
    }
}