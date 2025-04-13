package sk.ukf.autviz.Controllers;


import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import sk.ukf.autviz.Models.*;
import sk.ukf.autviz.Utils.CircleCell;
import sk.ukf.autviz.Utils.DirectedEdge;
import sk.ukf.autviz.Utils.DirectedLoop;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class View1Controller implements Initializable {

    public AnchorPane view1_parent;
    public AnchorPane graph_region;
    public Button add_state_button;
    public Button delete_state_button;
    public Button add_edge_button;
    public Button delete_edge_button;
    public Button edit_mode_button;

    private Graph graph;
    private com.fxgraph.graph.Model graphModel; // model z FXGraph
    private Map<State, StateCellData> stateMapping = new HashMap<>();

    private String newStateName = "";

    private State selectedEdgeSource = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing View1Controller");
        Automata automata = Model.getInstance().getCurrentAutomata();
        if (automata == null) {
            automata = createSampleAutomaton();
            Model.getInstance().setCurrentAutomata(automata);
        }
        this.graph = new Graph();
        this.graphModel = graph.getModel();

        Model.getInstance().automataChangedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                updateVisualization();
                Model.getInstance().setAutomataChanged(false);
            }
        });

        buildVisualization(automata);
        addButtonListeners();
        addButtonStyles();
    }

    public void buildVisualization(Automata automata) {
        graph.beginUpdate();

        // naplnenie mapy pre prepojenie stavov s info ich grafických buniek
        for (State state : automata.getStates()) {
            AbstractCell cell = new CircleCell(state, true);
            graphModel.addCell(cell);
            Region graphicNode = cell.getGraphic(graph);
            StateCellData cellData = new StateCellData(cell, graphicNode);
            stateMapping.put(state, cellData);

            attachClickHandlers(graphicNode, state);
        }

        for (Transition t : automata.getTransitions()) {
            StateCellData sourceData = stateMapping.get(t.getStateSource());
            StateCellData targetData = stateMapping.get(t.getStateDestination());
            if (sourceData != null && targetData != null) {
                ICell source = sourceData.getCell();
                ICell target = targetData.getCell();
                if (t.getStateSource().equals(t.getStateDestination())) {
                    DirectedLoop dLoop = new DirectedLoop(source, t);
                    dLoop.textProperty().set(t.getCharacter());
                    graphModel.addEdge(dLoop);
                } else {
                    DirectedEdge dEdge = new DirectedEdge(source, target, t);
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

    public void updateVisualization() {
        updateAllStatePositions();
        Automata automata = Model.getInstance().getCurrentAutomata();
        graphModel.clear();
        graph.beginUpdate();

        for (State s : automata.getStates()) {
            StateCellData cellData = stateMapping.get(s);
            // State je v Modeli ale nie je v stateMapping = bol pridany v inom view
            // vytvorime mu stateMapping a vsetko potrebne na vizualizaciu
            if (cellData == null) {
                AbstractCell cell = new CircleCell(s, true);
                graphModel.addCell(cell);
                Region graphicNode = cell.getGraphic(graph);
                attachClickHandlers(graphicNode, s);
                cellData = new StateCellData(cell, graphicNode);
                stateMapping.put(s, cellData);
            } else {
                Region graphicNode = cellData.getGraphicNode();
                graphicNode.setLayoutX(cellData.getLayoutX());
                graphicNode.setLayoutY(cellData.getLayoutY());
                graphModel.addCell(cellData.getCell());
            }
        }

        for (Transition t : automata.getTransitions()) {
            StateCellData sourceData = stateMapping.get(t.getStateSource());
            StateCellData targetData = stateMapping.get(t.getStateDestination());
            if (sourceData != null && targetData != null) {
                ICell source = sourceData.getCell();
                ICell target = targetData.getCell();
                if (t.getStateSource().equals(t.getStateDestination())) {
                    DirectedLoop dLoop = new DirectedLoop(source, t);
                    dLoop.textProperty().set(t.getCharacter());
                    graphModel.addEdge(dLoop);
                } else {
                    DirectedEdge dEdge = new DirectedEdge(source, target, t);
                    graphModel.addEdge(dEdge);
                }
            }
        }

        graph.endUpdate();
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

        a.addAlphabet("a");
        a.addAlphabet("b");
        a.addAlphabet("c");

        // testovanie curved DirectedEdge
        a.addTransition(new Transition(q1, "b", q0));
        a.addTransition(new Transition(q2, "aaaaaa", q0));
        a.addTransition(new Transition(q0, "b", q1));
        a.addTransition(new Transition(q0, "b", q3));
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

    public void updateAllStatePositions() {
        for (Map.Entry<State, StateCellData> entry : stateMapping.entrySet()) {
            StateCellData cellData = entry.getValue();
            Region graphicNode = cellData.getCell().getGraphic(graph);
            // current layout positions.
            double currentX = graphicNode.getLayoutX();
            double currentY = graphicNode.getLayoutY();
            // update the stored properties.
            cellData.setLayoutX(currentX);
            cellData.setLayoutY(currentY);
//            System.out.println("Updated " + entry.getKey().getName()
//                    + " -> X: " + currentX
//                    + ", Y: " + currentY);
        }
    }

    private void addButtonListeners() {
        delete_edge_button.setOnAction(event -> {
            boolean currentMode = Model.getInstance().isDeleteEdgeMode();
            boolean newMode = !currentMode;
            Model.getInstance().setDeleteEdgeMode(newMode);
            System.out.println("DeleteEdgeMode is " + newMode);
        });

        add_edge_button.setOnAction(event -> {
            StateCellData sourceData = stateMapping.get(selectedEdgeSource);
            if (sourceData != null) {
                Region node = sourceData.getGraphicNode();
                node.setStyle("");
            }
            selectedEdgeSource = null;
            boolean currentMode = Model.getInstance().isAddEdgeMode();
            boolean newMode = !currentMode;
            Model.getInstance().setAddEdgeMode(newMode);
            System.out.println("AddEdgeMode is " + newMode);
        });

        edit_mode_button.setOnAction(event -> {
            boolean currentMode = Model.getInstance().isEditMode();
            boolean newMode = !currentMode;
            Model.getInstance().setEditMode(newMode);
            System.out.println("EditMode is " + newMode);
        });

        delete_state_button.setOnAction(event -> {
            boolean currentMode = Model.getInstance().isDeleteStateMode();
            boolean newMode = !currentMode;
            Model.getInstance().setDeleteStateMode(newMode);
            System.out.println("DeleteStateMode is " + newMode);
        });

        add_state_button.setOnAction(event -> {
            Model.getInstance().disableModes();
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Add New State");
            dialog.setHeaderText("Zadaj meno pre nový stav:");
            dialog.setContentText("Meno stavu:");
            dialog.getEditor().setPromptText("...");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }

            String name = result.get();
            if (!name.trim().isEmpty()) {
                newStateName = name.trim();
            } else {
                newStateName = "";
            }

            State newState = new State(newStateName);

            Model.getInstance().getCurrentAutomata().addState(newState);

            AbstractCell newStateCell = new CircleCell(newState, true);

            Region graphicNode = newStateCell.getGraphic(graph);
            attachClickHandlers(graphicNode, newState);
            StateCellData cellData = new StateCellData(newStateCell, graphicNode);
            stateMapping.put(newState, cellData);
            updateVisualization();
        });
    }

    private void processStateCellClick(State clickedState) {
        if (selectedEdgeSource == null) {
            selectedEdgeSource = clickedState;
            System.out.println("Edge source selected: " + selectedEdgeSource.getName());
            StateCellData cellData = stateMapping.get(selectedEdgeSource);
            if (cellData != null) {
                Region node = cellData.getGraphicNode();
                node.setStyle("-fx-border-color: red;-fx-background-radius: 51%;-fx-border-radius: 50%;");
            }
        } else {
            State source = selectedEdgeSource;
            State target = clickedState;
            System.out.println("Edge target selected: " + target.getName());

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add New Edge");
            dialog.setHeaderText("Enter symbols for the transition");
            dialog.setContentText("Symbols (e.g., a, b):");
            dialog.getEditor().setPromptText("Enter symbols here...");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(symbols -> {
                Transition newTransition = new Transition(source, symbols, target);
                Model.getInstance().getCurrentAutomata().addTransition(newTransition);
                updateVisualization();
            });
            StateCellData sourceData = stateMapping.get(selectedEdgeSource);
            if (sourceData != null) {
                Region node = sourceData.getGraphicNode();
                node.setStyle("");
            }
            selectedEdgeSource = null;
            Model.getInstance().setAddEdgeMode(false);
            System.out.println("Edge addition mode deactivated.");
        }
    }

    private void attachClickHandlers(Region graphicNode, State state) {
        graphicNode.setOnMouseClicked(event -> {
            if (Model.getInstance().isAddEdgeMode()) {
                processStateCellClick(state);
                event.consume();
            }

            if (Model.getInstance().isDeleteStateMode()) {
                System.out.println("Deleting state: " + state.getName());
                Automata automata = Model.getInstance().getCurrentAutomata();
                automata.getTransitions().removeIf(t ->
                        t.getStateSource().equals(state) || t.getStateDestination().equals(state)
                );
                automata.removeState(state);
                stateMapping.remove(state);
                updateVisualization();
            }
        });

    }

    private void addButtonStyles() {
        edit_mode_button.styleProperty().bind(
                javafx.beans.binding.Bindings.when(Model.getInstance().editModeProperty())
                        .then("-fx-background-color: #66cc66; -fx-text-fill: white;")
                        .otherwise("")
        );

        delete_state_button.styleProperty().bind(
                javafx.beans.binding.Bindings.when(Model.getInstance().deleteStateModeProperty())
                        .then("-fx-background-color: #66cc66; -fx-text-fill: white;")
                        .otherwise("")
        );

        delete_edge_button.styleProperty().bind(
                javafx.beans.binding.Bindings.when(Model.getInstance().deleteEdgeModeProperty())
                        .then("-fx-background-color: #66cc66; -fx-text-fill: white;")
                        .otherwise("")
        );

        add_edge_button.styleProperty().bind(
                javafx.beans.binding.Bindings.when(Model.getInstance().addEdgeModeProperty())
                        .then("-fx-background-color: #66cc66; -fx-text-fill: white;")
                        .otherwise("")
        );
    }

}