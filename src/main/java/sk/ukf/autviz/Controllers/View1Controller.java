package sk.ukf.autviz.Controllers;


import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
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

        // Vyvolane zmenou na View1 a View1 sa ma updatnut
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (Model.getInstance().getViewFactory().getClientSelectedViewProperty().get().equals("View1")
                    && Model.getInstance().isUpdateView1()) {
                    updateVisualization();
                    Model.getInstance().setUpdateView1(false);
                    }
                });
        // Vyvolane zmenou updateView1 property na true a sme vo View1
        Model.getInstance().updateView1Property().addListener((obs, oldVal, newVal) -> {
            if (newVal && Model.getInstance().getViewFactory().getClientSelectedViewProperty().get().equals("View1")) {
                updateVisualization();
                Model.getInstance().setUpdateView2(true);
                Model.getInstance().setUpdateView3(true);
                Model.getInstance().setUpdateView1(false);
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

            Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            dialog.getEditor().textProperty().addListener((obs, oldText, newText) -> {
                okButton.setDisable(newText.trim().isEmpty());
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }

            String name = result.get().trim();
            if (name.isEmpty()) {
                Alert emptyAlert = new Alert(Alert.AlertType.ERROR);
                emptyAlert.setTitle("Invalid State Name");
                emptyAlert.setHeaderText(null);
                emptyAlert.setContentText("Meno stavu nemôže byť prázdne. Prosím zadaj platné meno.");
                emptyAlert.showAndWait();
                return;
            }

            State newState = new State(name);

            if (newState.isDuplicateIn(Model.getInstance().getCurrentAutomata().getStates())) {
                Alert duplicateAlert = new Alert(Alert.AlertType.ERROR);
                duplicateAlert.setTitle("Duplicate State");
                duplicateAlert.setHeaderText(null);
                duplicateAlert.setContentText("Stav s menom \"" + name + "\" už existuje. Prosím, zvoľ iné meno.");
                duplicateAlert.showAndWait();
                return;
            }

            Model.getInstance().getCurrentAutomata().addState(newState);

            AbstractCell newStateCell = new CircleCell(newState, true);

            Region graphicNode = newStateCell.getGraphic(graph);
            attachClickHandlers(graphicNode, newState);
            StateCellData cellData = new StateCellData(newStateCell, graphicNode);
            stateMapping.put(newState, cellData);
            Model.getInstance().setUpdateView2(true);
            Model.getInstance().setUpdateView3(true);
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
            dialog.setHeaderText("Zadaj symboly pre nový prechod");
            dialog.setContentText("Symboly (a, b, ...):");
            dialog.getEditor().setPromptText("Sem zadaj symboly ...");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(symbols -> {
                Transition newTransition = new Transition(source, symbols, target);
                Model.getInstance().getCurrentAutomata().addTransition(newTransition);
                Model.getInstance().setUpdateView2(true);
                Model.getInstance().setUpdateView3(true);
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
        graphicNode.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
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
                Model.getInstance().setUpdateView2(true);
                Model.getInstance().setUpdateView3(true);
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