package sk.ukf.autviz.Controllers;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;
import sk.ukf.autviz.Models.Automata;
import sk.ukf.autviz.Models.Model;
import sk.ukf.autviz.Models.State;
import sk.ukf.autviz.Models.Transition;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class View2Controller implements Initializable {

    private static final double COLUMN_PREF_WIDTH = 120;
    private static final double FIXED_CELL_SIZE = 31;
    private static final double HEADER_HEIGHT = 50;

    public AnchorPane view2_parent;
    public AnchorPane table_region;
    public TableView<TransitionRow> transition_table;
    public Button add_state_button;
    public Button delete_state_button;
    public Button add_edge_button;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing View2Controller");
        Automata automata = Model.getInstance().getCurrentAutomata();
        buildTransitionTable(automata);
        addButtonListeners();
        // Vyvolane zmenou na View2 a View2 sa ma updatnut
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (Model.getInstance().getViewFactory().getClientSelectedViewProperty().get().equals("View2")
                            && Model.getInstance().isUpdateView2()) {
                        buildTransitionTable(automata);
                        Model.getInstance().setUpdateView2(false);
                    }
                });
        // Vyvolane zmenou updateView2 property na true a sme vo View2
        Model.getInstance().updateView2Property().addListener((obs, oldVal, newVal) -> {
            if (newVal && Model.getInstance().getViewFactory().getClientSelectedViewProperty().get().equals("View2")) {
                buildTransitionTable(automata);
                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(true);
                Model.getInstance().setUpdateView2(false);
            }
        });
        transition_table.setEditable(true);
    }
/*
    private void buildTransitionTable(Automata automata) {
        System.out.println("Building transition table");
        ObservableList<TransitionRow> rows = FXCollections.observableArrayList();
        for (Transition t : automata.getTransitions()) {
            rows.add(new TransitionRow(t.getStateSource(), t, t.getStateDestination()));
        }
        transition_table.setItems(rows);

        TableColumn<TransitionRow, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(cellData -> cellData.getValue().sourceProperty());
        sourceCol.setPrefWidth(COLUMN_PREF_WIDTH);
        sourceCol.setCellFactory(col -> new TableCell<TransitionRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                textProperty().unbind();
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    TransitionRow row = getTableRow().getItem();
                    textProperty().bind(Bindings.createStringBinding(
                            () -> decorateStateName(
                                    row.getSource().nameProperty().get(),
                                    row.getSource().stateBeginProperty().get(),
                                    row.getSource().stateEndProperty().get()
                            ),
                            row.getSource().nameProperty(),
                            row.getSource().stateBeginProperty(),
                            row.getSource().stateEndProperty()
                    ));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        TableColumn<TransitionRow, String> symbolCol = new TableColumn<>("Symbol(s)");
        symbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        symbolCol.setPrefWidth(COLUMN_PREF_WIDTH);

        TableColumn<TransitionRow, String> destinationCol = new TableColumn<>("Destination");
        destinationCol.setCellValueFactory(cellData -> cellData.getValue().destinationProperty());
        destinationCol.setPrefWidth(COLUMN_PREF_WIDTH);
        destinationCol.setCellFactory(col -> new TableCell<TransitionRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                textProperty().unbind();
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    TransitionRow row = getTableRow().getItem();
                    textProperty().bind(Bindings.createStringBinding(
                            () -> decorateStateName(
                                    row.getDestination().nameProperty().get(),
                                    row.getDestination().stateBeginProperty().get(),
                                    row.getDestination().stateEndProperty().get()
                            ),
                            row.getDestination().nameProperty(),
                            row.getDestination().stateBeginProperty(),
                            row.getDestination().stateEndProperty()
                    ));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        TableColumn<TransitionRow, Void> deleteColumn = new TableColumn<>("");
        deleteColumn.setPrefWidth(50);
        deleteColumn.setCellFactory(col -> new TableCell<TransitionRow, Void>() {
            private final Button deleteButton = new Button("-");

            {
                // Style the delete button if desired.
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                // Set up the action for when the button is clicked.
                deleteButton.setOnAction(event -> {
                    TransitionRow currentRow = getTableView().getItems().get(getIndex());
                    // Remove from the automata model.
                    Model.getInstance().getCurrentAutomata().removeTransition(currentRow.getTransition());
                    // Remove the row from the TableView.
                    getTableView().getItems().remove(currentRow);
                    // Optionally, if other views depend on the model, trigger an update.
                    // For example:
                    Model.getInstance().setUpdateView1(true);
                    Model.getInstance().setUpdateView3(true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Set all your columns into the TableView
        transition_table.getColumns().setAll(sourceCol, symbolCol, destinationCol, deleteColumn);
    }
*/

private void buildTransitionTable(Automata automata) {
    System.out.println("Building transition table");
    ObservableList<TransitionRow> rows = FXCollections.observableArrayList();
    for (Transition t : automata.getTransitions()) {
        rows.add(new TransitionRow(t.getStateSource(), t, t.getStateDestination()));
    }
    transition_table.setItems(rows);

    // Allow table editing.
    transition_table.setEditable(true);

    // --- Source Column with Inline Editing and Decoration ---
    TableColumn<TransitionRow, String> sourceCol = new TableColumn<>("Source");
    sourceCol.setCellValueFactory(cellData -> cellData.getValue().sourceProperty());
    sourceCol.setPrefWidth(COLUMN_PREF_WIDTH);

    // Use a custom cell factory that creates a TextFieldTableCell.
    sourceCol.setCellFactory(col -> new TextFieldTableCell<TransitionRow, String>(new DefaultStringConverter()) {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            // If not editing, display decorated text.
            if (!isEditing()) {
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    TransitionRow row = getTableRow().getItem();
                    // Get the raw state name from the source object.
                    String rawName = row.getSource().nameProperty().get();
                    // Decorate it (e.g., add "► " if it is a beginning state, " !" if it is an end state).
                    setText(decorateStateName(rawName,
                            row.getSource().stateBeginProperty().get(),
                            row.getSource().stateEndProperty().get()));
                }
            }
        }
        @Override
        public void commitEdit(String newValue) {
            // Remove any decorations before updating the underlying value.
            newValue = newValue.replace("► ", "").replace(" !", "").trim();
            super.commitEdit(newValue);
            // Update the underlying model
            TransitionRow row = getTableRow().getItem();
            row.getSource().nameProperty().set(newValue);
            // Refresh the table to ensure decorations are re-computed.
            getTableView().refresh();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            // When canceling, reset the cell text with the decorated text.
            if (getTableRow() != null && getTableRow().getItem() != null) {
                TransitionRow row = getTableRow().getItem();
                String rawName = row.getSource().nameProperty().get();
                setText(decorateStateName(rawName,
                        row.getSource().stateBeginProperty().get(),
                        row.getSource().stateEndProperty().get()));
            }
        }
    });

    // Update the model when an edit is committed.
    sourceCol.setOnEditCommit(event -> {
        TransitionRow row = event.getRowValue();
        String newValue = event.getNewValue();
        // Remove decoration if any before updating (if needed).
        newValue = newValue.replace("► ", "").replace(" !", "").trim();
        row.getSource().nameProperty().set(newValue);
        // Refresh the table so the decoration is re‑computed.
        transition_table.refresh();
    });

    // --- Symbol Column (Editable) ---
    TableColumn<TransitionRow, String> symbolCol = new TableColumn<>("Symboly");
    symbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
    symbolCol.setPrefWidth(COLUMN_PREF_WIDTH);
    symbolCol.setCellFactory(TextFieldTableCell.forTableColumn());
    symbolCol.setOnEditCommit(event -> {
        TransitionRow row = event.getRowValue();
        String newValue = event.getNewValue();
        newValue = newValue.trim();
        row.getTransition().setSymbols(newValue);
        transition_table.refresh();
    });

    // --- Destination Column with Inline Editing and Decoration ---
    TableColumn<TransitionRow, String> destinationCol = new TableColumn<>("Destination");
    destinationCol.setCellValueFactory(cellData -> cellData.getValue().destinationProperty());
    destinationCol.setPrefWidth(COLUMN_PREF_WIDTH);
    destinationCol.setCellFactory(col -> new TextFieldTableCell<TransitionRow, String>(new DefaultStringConverter()) {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (!isEditing()) {
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    TransitionRow row = getTableRow().getItem();
                    String rawName = row.getDestination().nameProperty().get();
                    setText(decorateStateName(rawName,
                            row.getDestination().stateBeginProperty().get(),
                            row.getDestination().stateEndProperty().get()));
                }
            }
        }
        @Override
        public void commitEdit(String newValue) {
            // Remove any decorations before updating the underlying value.
            newValue = newValue.replace("► ", "").replace(" !", "").trim();
            super.commitEdit(newValue);
            // Update the underlying model
            TransitionRow row = getTableRow().getItem();
            row.getSource().nameProperty().set(newValue);
            // Refresh the table to ensure decorations are re-computed.
            getTableView().refresh();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            // When canceling, reset the cell text with the decorated text.
            if (getTableRow() != null && getTableRow().getItem() != null) {
                TransitionRow row = getTableRow().getItem();
                String rawName = row.getSource().nameProperty().get();
                setText(decorateStateName(rawName,
                        row.getSource().stateBeginProperty().get(),
                        row.getSource().stateEndProperty().get()));
            }
        }
    });
    destinationCol.setOnEditCommit(event -> {
        TransitionRow row = event.getRowValue();
        String newValue = event.getNewValue();
        newValue = newValue.replace("► ", "").replace(" !", "").trim();
        row.getDestination().nameProperty().set(newValue);
        transition_table.refresh();
    });

    // --- Delete Column (Button) ---
    TableColumn<TransitionRow, Void> deleteColumn = new TableColumn<>("");
    deleteColumn.setPrefWidth(50);
    deleteColumn.setCellFactory(col -> new TableCell<TransitionRow, Void>() {
        private final Button deleteButton = new Button("-");
        {
            deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            deleteButton.setOnAction(event -> {
                TransitionRow currentRow = getTableView().getItems().get(getIndex());
                // Remove the transition from the model.
                Model.getInstance().getCurrentAutomata().removeTransition(currentRow.getTransition());
                // Remove row from the table.
                getTableView().getItems().remove(currentRow);
                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(true);
            });
        }
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(deleteButton);
            }
        }
    });

    // Add all columns to the table.
    transition_table.getColumns().setAll(sourceCol, symbolCol, destinationCol, deleteColumn);
}

    private String decorateStateName(String name, boolean isBegin, boolean isEnd) {
        String prefix = isBegin ? "► " : "";
        String suffix = isEnd ? " !" : "";
        return prefix + name + suffix;
    }

    public static class TransitionRow {
        private final State source;
        private final Transition transition;
        private final State destination;

        public TransitionRow(State source, Transition transition, State destination) {
            this.source = source;
            this.transition = transition;
            this.destination = destination;
        }

        public StringProperty sourceProperty() {
            return source.nameProperty();
        }

        public StringProperty symbolProperty() {
            return transition.characterProperty();
        }

        public StringProperty destinationProperty() {
            return destination.nameProperty();
        }

        public Transition getTransition() {
            return transition;
        }

        public State getSource() {
            return source;
        }

        public State getDestination() {
            return destination;
        }
    }

    public class NewStateData {
        private final String name;
        private final boolean begin;
        private final boolean end;

        public NewStateData(String name, boolean begin, boolean end) {
            this.name = name;
            this.begin = begin;
            this.end = end;
        }

        public String getName() {
            return name;
        }

        public boolean isBegin() {
            return begin;
        }

        public boolean isEnd() {
            return end;
        }
    }

    public static class NewEdgeData {
        private final State source;
        private final String symbols;
        private final State target;

        public NewEdgeData(State source, String symbols, State target) {
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

    private void addButtonListeners() {
        add_state_button.setOnAction(event -> {
            Model.getInstance().disableModes();

            Dialog<NewStateData> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Add New State");
            dialog.setHeaderText("Zadaj detaily pre nový stav:");

            ButtonType addButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField();
            nameField.setPromptText("Meno stavu");

            CheckBox beginCheckBox = new CheckBox("Začiatočný");
            CheckBox endCheckBox = new CheckBox("Koncový");

            grid.add(new Label("Meno:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(beginCheckBox, 0, 1);
            grid.add(endCheckBox, 1, 1);

            dialog.getDialogPane().setContent(grid);

            Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
            addButton.setDisable(true);
            nameField.textProperty().addListener((obs, oldText, newText) -> {
                addButton.setDisable(newText.trim().isEmpty());
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    String stateName = nameField.getText().trim();
                    boolean isBegin = beginCheckBox.isSelected();
                    boolean isEnd = endCheckBox.isSelected();
                    return new NewStateData(stateName, isBegin, isEnd);
                }
                return null;
            });

            Optional<NewStateData> result = dialog.showAndWait();
            result.ifPresent(newStateData -> {
                String stateName = newStateData.getName();
                State newState = new State(stateName);
                newState.setStateBegin(newStateData.isBegin());
                newState.setStateEnd(newStateData.isEnd());

                if (newState.isDuplicateIn(Model.getInstance().getCurrentAutomata().getStates())) {
                    javafx.scene.control.Alert duplicateAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    duplicateAlert.setTitle("Duplicate State");
                    duplicateAlert.setHeaderText(null);
                    duplicateAlert.setContentText("Stav s menom \"" + stateName + "\" už existuje. Prosím, zvoľ iné meno.");
                    duplicateAlert.showAndWait();
                    return;
                }
                Model.getInstance().getCurrentAutomata().addState(newState);

                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(true);
            });
        });

        delete_state_button.setOnAction(event -> {
            Model.getInstance().disableModes();
            showDeleteStatesDialog();
        });

        add_edge_button.setOnAction(event -> {
            Model.getInstance().disableModes();

            Dialog<NewEdgeData> dialog = new Dialog<>();
            dialog.setTitle("Add New Transition");
            dialog.setHeaderText("Vyber a zadaj detaily pre nový prechod:");

            // Set up the dialog buttons.
            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            // Create a GridPane and add controls.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            // ComboBox for source state
            ComboBox<State> sourceCombo = new ComboBox<>();
            sourceCombo.getItems().addAll(Model.getInstance().getCurrentAutomata().getStates());
            // Set a cell factory and converter so that the state is displayed by its name.
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

            // TextField for symbols
            TextField symbolField = new TextField();
            symbolField.setPromptText("a,b, ...");

            // ComboBox for target state
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

            // Enable/Disable OK button based on whether required fields are filled.
            Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
            okButton.setDisable(true);
            // Listen to changes in the text field and combo boxes.
            symbolField.textProperty().addListener((observable, oldValue, newValue) -> {
                okButton.setDisable(newValue.trim().isEmpty() ||
                        sourceCombo.getValue() == null ||
                        targetCombo.getValue() == null);
            });
            sourceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(symbolField.getText().trim().isEmpty() ||
                        newVal == null || targetCombo.getValue() == null);
            });
            targetCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(symbolField.getText().trim().isEmpty() ||
                        sourceCombo.getValue() == null || newVal == null);
            });

            // Convert the result to a NewEdgeData object when OK is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new NewEdgeData(sourceCombo.getValue(), symbolField.getText(), targetCombo.getValue());
                }
                return null;
            });

            Optional<NewEdgeData> result = dialog.showAndWait();
            result.ifPresent(data -> {
                // Create and add a new transition to the automata model.
                if (data.getSource() != null && data.getTarget() != null && !data.getSymbols().trim().isEmpty()) {
                    Transition newTransition = new Transition(data.getSource(), data.getSymbols(), data.getTarget());
                    Model.getInstance().getCurrentAutomata().addTransition(newTransition);
                    // (Optionally) Rebuild the table to show the new transition
                    buildTransitionTable(Model.getInstance().getCurrentAutomata());

                    Model.getInstance().setUpdateView1(true);
                    Model.getInstance().setUpdateView3(true);
                }
            });
        });
    }

    private void showDeleteStatesDialog() {
        // Create a dialog to list all states with checkboxes.
        Dialog<List<State>> dialog = new Dialog<>();
        dialog.setTitle("Delete States");
        dialog.setHeaderText("Vyber stavy na vymazanie:");

        // Add OK and CANCEL buttons.
        ButtonType deleteButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);

        // Create a VBox and a Map to hold CheckBoxes associated with each state.
        VBox vbox = new VBox(10);
        Map<CheckBox, State> checkBoxStateMap = new HashMap<>();

        // Get the list of states from the Automata.
        Automata automata = Model.getInstance().getCurrentAutomata();
        for (State state : automata.getStates()) {
            CheckBox cb = new CheckBox(state.getName());
            checkBoxStateMap.put(cb, state);
            vbox.getChildren().add(cb);
        }

        dialog.getDialogPane().setContent(vbox);

        // Convert the result when OK is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return checkBoxStateMap.entrySet().stream()
                        .filter(entry -> entry.getKey().isSelected())
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
            }
            return null;
        });

        Optional<List<State>> result = dialog.showAndWait();
        result.ifPresent(selectedStates -> {
            if (!selectedStates.isEmpty()) {
                // Remove transitions that involve any of the selected states.
                automata.getTransitions().removeIf(t ->
                        selectedStates.contains(t.getStateSource()) ||
                                selectedStates.contains(t.getStateDestination()));

                // Remove the selected states.
                automata.getStates().removeAll(selectedStates);

                // Optionally, log the result.
                System.out.println("Deleted states: " + selectedStates.stream()
                        .map(State::getName)
                        .collect(Collectors.joining(", ")));

                // Update the TableView by rebuilding the transition table.
                buildTransitionTable(automata);
                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(true);
            }
        });
    }
}