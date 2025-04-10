package sk.ukf.autviz.Controllers;


import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import sk.ukf.autviz.Models.Automata;
import sk.ukf.autviz.Models.State;
import sk.ukf.autviz.Models.Transition;
import sk.ukf.autviz.Models.Model; // Naša Model trieda, ako singleton
import sk.ukf.autviz.Utils.CircleCell;
import sk.ukf.autviz.Utils.DirectedEdge;
import sk.ukf.autviz.Utils.DirectedLoop;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class View1Controller implements Initializable {

    @FXML
    public AnchorPane view1_parent;
    public AnchorPane graph_region;
    public Button add_state_button;
    public Button delete_state_button;
    public Button add_edge_button;
    public Button delete_edge_button;
    public Button edit_mode_button;

    private Graph graph;
    private com.fxgraph.graph.Model graphModel; // model z FXGraph

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing View1Controller");
        Automata automata = Model.getInstance().getCurrentAutomata();
        if (automata == null) {
            automata = createSampleAutomaton();
            Model.getInstance().setCurrentAutomata(automata);
        }
        updateVisualization(automata);

        addButtonListeners();
    }

    public void updateVisualization(Automata automata) {
        graph_region.getChildren().clear();

        graph = new Graph();
        graphModel = graph.getModel();
        graph.beginUpdate();

        // mapa pre prepojenie názvov stavov s grafickými bunkami
        Map<String, ICell> stateMapping = new HashMap<>();
        for (State s : automata.getStates()) {
            AbstractCell cell = new CircleCell(s);
            graphModel.addCell(cell);
            stateMapping.put(s.getName(), cell);
        }

        for (Transition t : automata.getTransitions()) {
            ICell source = stateMapping.get(t.getStateSource().getName());
            ICell target = stateMapping.get(t.getStateDestination().getName());
            if (source != null && target != null) {
                // ak je zdroj rovnaký ako cieľ, ide o self-loop.
                if (t.getStateSource().equals(t.getStateDestination())) {
                    DirectedLoop dLoop = new DirectedLoop(source, t);
                    dLoop.textProperty().set(t.getCharacter());
                    graphModel.addEdge(dLoop);
                } else {
                    DirectedEdge dEdge = new DirectedEdge(source, target, t);
                    dEdge.textProperty().set(t.getCharacter());
                    graphModel.addEdge(dEdge);
                }
            }
        }
        graph.endUpdate();

        // graph.layout(new AbegoTreeLayout(200, 150, org.abego.treelayout.Configuration.Location.Top));

        BorderPane pane = new BorderPane(graph.getCanvas());
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        graph_region.getChildren().add(pane);

        // clipping aby sa grafické prvky nezobrazovali mimo view1_parent
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(graph_region.widthProperty());
        clip.heightProperty().bind(graph_region.heightProperty());
        graph_region.setClip(clip);
    }

    private Automata createSampleAutomaton() {
        Automata a = new Automata();

        // vytvorenie 5 stavov
        State q0 = new State("q0");
        State q1 = new State("q1");
        State q2 = new State("qqqqq2"); // Begin a End
        State q3 = new State("q3");
        State q4 = new State("q4"); // End

        // nastavenie príznakov:
        q2.setStateBegin(true);
        q2.setStateEnd(true);
        q4.setStateEnd(true);

        // Pridanie stavov do automatu
        a.addState(q0);
        a.addState(q1);
        a.addState(q4);
        a.addState(q3);
        a.addState(q2);

//        State q5 = new State("q5");
//        State q6 = new State("q6");
//        State q7 = new State("q7");
//        State q8 = new State("q8");
//        State q9 = new State("q9");
//        State q10 = new State("q10");
//        State q11 = new State("q11");
//        State q12 = new State("q12");
//        State q13 = new State("q13");
//        State q14 = new State("q14");
//        a.addState(q5);
//        a.addState(q6);
//        a.addState(q7);
//        a.addState(q8);
//        a.addState(q9);
//        a.addState(q10);
//        a.addState(q11);
//        a.addState(q12);
//        a.addState(q13);
//        a.addState(q14);

        // Pridanie symbolov abecedy
        a.addAlphabet("a");
        a.addAlphabet("b");
        a.addAlphabet("c");
        a.addAlphabet("d");

        // Definovanie prechodov (napr. vytvoríme cyklus)
        a.addTransition(new Transition(q2, "aaaaaa", q0));
        a.addTransition(new Transition(q0, "b", q1));
        a.addTransition(new Transition(q0, "a", q3));
        a.addTransition(new Transition(q1, "a", q3));
        a.addTransition(new Transition(q1, "b", q1));
        a.addTransition(new Transition(q3, "b", q4));
        a.addTransition(new Transition(q4, "a", q2));

//        State q1 = new State("q1");
//        a.addState(q1);
//        a.addTransition(new Transition(q1, "a", q1));

        return a;
    }

    private void addButtonListeners() {
        edit_mode_button.setOnAction(event -> {
            boolean currentMode = Model.getInstance().isEditMode();
            boolean newMode = !currentMode;
            Model.getInstance().setEditMode(newMode);
            System.out.println("Edit mode is now " + (newMode ? "ON" : "OFF"));
        });

        add_state_button.setOnAction(event -> {
            System.out.println(Model.getInstance().getCurrentAutomata());
        });
    }
}