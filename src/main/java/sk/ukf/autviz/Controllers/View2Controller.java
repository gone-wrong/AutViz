package sk.ukf.autviz.Controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import sk.ukf.autviz.Models.Automata;
import sk.ukf.autviz.Models.Model;
import sk.ukf.autviz.Models.State;
import sk.ukf.autviz.Models.Transition;

import java.net.URL;
import java.util.ResourceBundle;

public class View2Controller implements Initializable {

    private static final double COLUMN_PREF_WIDTH = 120;
    private static final double FIXED_CELL_SIZE = 30;
    private static final double HEADER_HEIGHT = 50;

    public AnchorPane view2_parent;
    public AnchorPane table_region;
    public TableView<TransitionRow> transition_table;
    public Button add_state_button;
    public Button delete_state_button;
    public Button add_edge_button;
    public Button delete_edge_button;
    public Button edit_mode_button;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing View2Controller");
        Automata automata = Model.getInstance().getCurrentAutomata();

        buildTransitionTable(automata);
        addButtonListeners();
        addButtonStyles();

        transition_table.prefHeightProperty().bind(Bindings.size(transition_table.getItems())
                .multiply(FIXED_CELL_SIZE)
                .add(HEADER_HEIGHT));
    }

    private void buildTransitionTable(Automata automata) {
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

        transition_table.getColumns().setAll(sourceCol, symbolCol, destinationCol);
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

    private void addButtonListeners() {
        add_state_button.setOnAction(event -> {
            // Disable any other modes
            Model.getInstance().disableModes();

            // Create a new Dialog to add state information
            javafx.scene.control.Dialog<NewStateData> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Add New State");
            dialog.setHeaderText("Enter details for the new state:");

            // Set the button types.
            javafx.scene.control.ButtonType addButtonType = new javafx.scene.control.ButtonType("Add", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, javafx.scene.control.ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
            nameField.setPromptText("State name");

            javafx.scene.control.CheckBox beginCheckBox = new javafx.scene.control.CheckBox("Začiatočný");
            javafx.scene.control.CheckBox endCheckBox = new javafx.scene.control.CheckBox("Koncový");

            grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(beginCheckBox, 0, 1);
            grid.add(endCheckBox, 1, 1);

            dialog.getDialogPane().setContent(grid);

            javafx.scene.Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
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

            java.util.Optional<NewStateData> result = dialog.showAndWait();
            result.ifPresent(newStateData -> {
                State newState = new State(newStateData.getName());
                newState.setStateBegin(newStateData.isBegin());
                newState.setStateEnd(newStateData.isEnd());

                Model.getInstance().getCurrentAutomata().addState(newState);

                Model.getInstance().setUpdateView1(true);
                Model.getInstance().setUpdateView3(true);
            });
        });

        delete_state_button.setOnAction(event -> {
            System.out.println(Model.getInstance().getCurrentAutomata().toString());
        });
    }
}