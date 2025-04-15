package sk.ukf.autviz.Controllers;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import javafx.scene.layout.GridPane;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.control.*;

import java.net.URL;
import java.util.*;

public class View3Controller implements Initializable {

    public AnchorPane view3_parent;
    public Button add_edge_button;
    public Button delete_edge_button;
    public Button edit_mode_button;

    public AnchorPane tree_region;
    private Graph graph;
    private com.fxgraph.graph.Model graphModel; // FXGraph model

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing View3Controller");
        Automata automata = Model.getInstance().getCurrentAutomata();
        StateTreeNode rootNode = buildStateTree(automata);
        drawStateTree(rootNode);

        // Vyvolane zmenou na View3 a View3 sa ma updatnut
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (Model.getInstance().getViewFactory().getClientSelectedViewProperty().get().equals("View3")
                            && Model.getInstance().isUpdateView3()) {
                        drawStateTree(buildStateTree(automata));
                        Model.getInstance().setUpdateView3(false);
                    }
                });

        // Vyvolane zmenou updateView3 property na true a sme vo View3
        Model.getInstance().updateView3Property().addListener((obs, oldVal, newVal) -> {
            if (newVal && Model.getInstance().getViewFactory().getClientSelectedViewProperty().get().equals("View3")) {
                drawStateTree(buildStateTree(automata));
                Model.getInstance().setUpdateView2(true);
                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(false);
            }
        });

        addButtonListeners();
        addButtonStyles();
    }

    private StateTreeNode buildStateTree(Automata automata) {
        if (automata.getStates().isEmpty()) {
            return null;
        }
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
            State dummy = new State("dummyroot");
            StateTreeNode root = new StateTreeNode(dummy, true, null);
            for (State s : beginStates) {
                root.addChild(buildStateTreeRecursive(s, automata, expanded, new Transition(dummy, "ε", s)));
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
        tree_region.getChildren().clear();
        if (root == null) {
            return;
        }

        graph = new Graph();
        graphModel = graph.getModel();
        graphModel.clear();
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
        tree_region.getChildren().add(pane);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(tree_region.widthProperty());
        clip.heightProperty().bind(tree_region.heightProperty());
        tree_region.setClip(clip);
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

    private void addButtonStyles() {
        edit_mode_button.styleProperty().bind(
                javafx.beans.binding.Bindings.when(Model.getInstance().editModeProperty())
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

    private void addButtonListeners() {
        add_edge_button.setOnAction(event -> {
            Model.getInstance().disableModes();

            Dialog<EdgeData> dialog = new Dialog<>();
            dialog.setTitle("Add New Transition");
            dialog.setHeaderText("Vyber a zadaj detaily pre nový prechod:");

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            ComboBox<State> sourceCombo = new ComboBox<>();
            sourceCombo.getItems().addAll(Model.getInstance().getCurrentAutomata().getStates());
            sourceCombo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(State item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            sourceCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(State item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            sourceCombo.setPromptText("Vyber 1. stav");

            TextField symbolField = new TextField();
            symbolField.setPromptText("a,b, ...");

            ComboBox<State> targetCombo = new ComboBox<>();
            targetCombo.getItems().addAll(Model.getInstance().getCurrentAutomata().getStates());
            targetCombo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(State item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            targetCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(State item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            targetCombo.setPromptText("Vyber 2. stav");

            grid.add(new Label("Stav 1:"), 0, 0);
            grid.add(sourceCombo, 1, 0);
            grid.add(new Label("Symboly:"), 0, 1);
            grid.add(symbolField, 1, 1);
            grid.add(new Label("Stav 2:"), 0, 2);
            grid.add(targetCombo, 1, 2);

            dialog.getDialogPane().setContent(grid);

            Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
            okButton.setDisable(true);

            symbolField.textProperty().addListener((observable, oldValue, newValue) -> {
                okButton.setDisable(sourceCombo.getValue() == null
                        || targetCombo.getValue() == null);
            });
            sourceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(newVal == null
                        || targetCombo.getValue() == null);
            });
            targetCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(sourceCombo.getValue() == null
                        || newVal == null);
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new EdgeData(sourceCombo.getValue(), symbolField.getText(), targetCombo.getValue());
                }
                return null;
            });

            Optional<EdgeData> result = dialog.showAndWait();
            result.ifPresent(data -> {
                if (data.getSource() != null && data.getTarget() != null) {
                    Transition newTransition = new Transition(data.getSource(), data.getSymbols(), data.getTarget());
                    Model.getInstance().getCurrentAutomata().addTransition(newTransition);
                    StateTreeNode newRoot = buildStateTree(Model.getInstance().getCurrentAutomata());
                    drawStateTree(newRoot);
                    Model.getInstance().setUpdateView1(true);
                    Model.getInstance().setUpdateView3(true);
                }
            });
        });

        delete_edge_button.setOnAction(event -> {
            boolean currentMode = Model.getInstance().isDeleteEdgeMode();
            boolean newMode = !currentMode;
            Model.getInstance().setDeleteEdgeMode(newMode);
            System.out.println("DeleteEdgeMode is " + newMode);
        });

        edit_mode_button.setOnAction(event -> {
            boolean currentMode = Model.getInstance().isEditMode();
            boolean newMode = !currentMode;
            Model.getInstance().setEditMode(newMode);
            System.out.println("EditMode is " + newMode);
        });
    }

    public static class EdgeData {
        public State source;
        public String symbols;
        public State target;
        public EdgeData(State source, String symbols, State target) {
            this.source = source;
            this.symbols = symbols;
            this.target = target;
        }

        public State getSource() {
            return source;
        }

        public String getSymbols() {
            return symbols;
        }

        public State getTarget() {
            return target;
        }
    }
}