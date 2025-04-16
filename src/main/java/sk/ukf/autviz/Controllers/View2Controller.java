package sk.ukf.autviz.Controllers;

import javafx.beans.value.ChangeListener;
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
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (Model.getInstance().getViewFactory().getClientSelectedViewProperty().get().equals("View2")
                            && Model.getInstance().isUpdateView2()) {
                        buildTransitionTable(automata);
                        Model.getInstance().setUpdateView2(false);
                    }
                });
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

private void buildTransitionTable(Automata automata) {
    System.out.println("Building transition table");
    ObservableList<TransitionRow> rows = FXCollections.observableArrayList();
    for (Transition t : automata.getTransitions()) {
        rows.add(new TransitionRow(t.getStateSource(), t, t.getStateDestination()));
    }
    transition_table.setItems(rows);

    transition_table.setEditable(true);

    TableColumn<TransitionRow, String> sourceCol = new TableColumn<>("Source");
    sourceCol.setCellValueFactory(cellData -> cellData.getValue().sourceProperty());
    sourceCol.setPrefWidth(COLUMN_PREF_WIDTH);

    sourceCol.setCellFactory(col -> new TextFieldTableCell<TransitionRow, String>(new DefaultStringConverter()) {
        private TransitionRow currentRow;
        private final ChangeListener<Boolean> beginEndListener = (obs, oldVal, newVal) -> updateText();

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                if (currentRow != null) {
                    currentRow.getSource().stateBeginProperty().removeListener(beginEndListener);
                    currentRow.getSource().stateEndProperty().removeListener(beginEndListener);
                    currentRow = null;
                }
                setText(null);
            } else {
                TransitionRow row = getTableRow().getItem();
                if (currentRow != row) {
                    if (currentRow != null) {
                        currentRow.getSource().stateBeginProperty().removeListener(beginEndListener);
                        currentRow.getSource().stateEndProperty().removeListener(beginEndListener);
                    }
                    currentRow = row;
                    currentRow.getSource().stateBeginProperty().addListener(beginEndListener);
                    currentRow.getSource().stateEndProperty().addListener(beginEndListener);
                }
                updateText();
            }
        }

        private void updateText() {
            if (currentRow != null) {
                String rawName = currentRow.getSource().nameProperty().get();
                setText(decorateStateName(rawName,
                        currentRow.getSource().stateBeginProperty().get(),
                        currentRow.getSource().stateEndProperty().get()));
            }
        }
        @Override
        public void commitEdit(String newValue) {
            newValue = newValue.replace("► ", "").replace(" !", "").trim();
            super.commitEdit(newValue);
            TransitionRow row = getTableRow().getItem();
            row.getSource().nameProperty().set(newValue);
            getTableView().refresh();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            if (getTableRow() != null && getTableRow().getItem() != null) {
                TransitionRow row = getTableRow().getItem();
                String rawName = row.getSource().nameProperty().get();
                setText(decorateStateName(rawName,
                        row.getSource().stateBeginProperty().get(),
                        row.getSource().stateEndProperty().get()));
            }
        }
    });

    sourceCol.setOnEditCommit(event -> {
        TransitionRow row = event.getRowValue();
        String newValue = event.getNewValue();
        newValue = newValue.replace("► ", "").replace(" !", "").trim();
        row.getSource().nameProperty().set(newValue);
        transition_table.refresh();
    });

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

    TableColumn<TransitionRow, String> destinationCol = new TableColumn<>("Destination");
    destinationCol.setCellValueFactory(cellData -> cellData.getValue().destinationProperty());
    destinationCol.setPrefWidth(COLUMN_PREF_WIDTH);
    destinationCol.setCellFactory(col -> new TextFieldTableCell<TransitionRow, String>(new DefaultStringConverter()) {
        private TransitionRow currentRow;
        private final ChangeListener<Boolean> beginEndListener = (obs, oldVal, newVal) -> updateText();

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                if (currentRow != null) {
                    currentRow.getSource().stateBeginProperty().removeListener(beginEndListener);
                    currentRow.getSource().stateEndProperty().removeListener(beginEndListener);
                    currentRow = null;
                }
                setText(null);
            } else {
                TransitionRow row = getTableRow().getItem();
                if (currentRow != row) {
                    if (currentRow != null) {
                        currentRow.getSource().stateBeginProperty().removeListener(beginEndListener);
                        currentRow.getSource().stateEndProperty().removeListener(beginEndListener);
                    }
                    currentRow = row;
                    currentRow.getSource().stateBeginProperty().addListener(beginEndListener);
                    currentRow.getSource().stateEndProperty().addListener(beginEndListener);
                }
                updateText();
            }
        }

        // Method to update the cell text based on the current state of the source.
        private void updateText() {
            if (currentRow != null) {
                String rawName = currentRow.getSource().nameProperty().get();
                setText(decorateStateName(rawName,
                        currentRow.getSource().stateBeginProperty().get(),
                        currentRow.getSource().stateEndProperty().get()));
            }
        }

        @Override
        public void commitEdit(String newValue) {
            newValue = newValue.replace("► ", "").replace(" !", "").trim();
            super.commitEdit(newValue);
            TransitionRow row = getTableRow().getItem();
            row.getSource().nameProperty().set(newValue);
            getTableView().refresh();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
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

    TableColumn<TransitionRow, Void> deleteColumn = new TableColumn<>("");
    deleteColumn.setPrefWidth(50);
    deleteColumn.setCellFactory(col -> new TableCell<TransitionRow, Void>() {
        private final Button deleteButton = new Button("-");
        {
            deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            deleteButton.setOnAction(event -> {
                TransitionRow currentRow = getTableView().getItems().get(getIndex());
                Model.getInstance().getCurrentAutomata().removeTransition(currentRow.getTransition());
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
                okButton.setDisable(sourceCombo.getValue() == null ||
                        targetCombo.getValue() == null);
            });
            sourceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(newVal == null ||
                        targetCombo.getValue() == null);
            });
            targetCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(sourceCombo.getValue() == null
                        || newVal == null);
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new NewEdgeData(sourceCombo.getValue(), symbolField.getText(), targetCombo.getValue());
                }
                return null;
            });

            Optional<NewEdgeData> result = dialog.showAndWait();
            result.ifPresent(data -> {
                if (data.getSource() != null && data.getTarget() != null) {
                    Transition newTransition = new Transition(data.getSource(), data.getSymbols(), data.getTarget());
                    Model.getInstance().getCurrentAutomata().addTransition(newTransition);
                    buildTransitionTable(Model.getInstance().getCurrentAutomata());

                    Model.getInstance().setUpdateView1(true);
                    Model.getInstance().setUpdateView3(true);
                }
            });
        });
    }

    private void showDeleteStatesDialog() {
        Dialog<List<State>> dialog = new Dialog<>();
        dialog.setTitle("Delete States");
        dialog.setHeaderText("Vyber stavy na vymazanie:");

        ButtonType deleteButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        Map<CheckBox, State> checkBoxStateMap = new HashMap<>();

        Automata automata = Model.getInstance().getCurrentAutomata();
        for (State state : automata.getStates()) {
            CheckBox cb = new CheckBox(state.getName());
            checkBoxStateMap.put(cb, state);
            vbox.getChildren().add(cb);
        }

        dialog.getDialogPane().setContent(vbox);

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
                automata.getTransitions().removeIf(t ->
                        selectedStates.contains(t.getStateSource()) ||
                                selectedStates.contains(t.getStateDestination()));

                automata.getStates().removeAll(selectedStates);

                System.out.println("Deleted states: " + selectedStates.stream()
                        .map(State::getName)
                        .collect(Collectors.joining(", ")));

                buildTransitionTable(automata);
                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(true);
            }
        });
    }
}