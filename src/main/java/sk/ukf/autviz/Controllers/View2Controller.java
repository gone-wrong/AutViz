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
                // Always unbind any previous binding.
                textProperty().unbind();
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    TransitionRow row = getTableRow().getItem();
                    // Bind the cell's textProperty so that it reflects changes in the state.
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
                    // Bind the cell's textProperty for the destination state.
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

        // These getters return the observable properties from the model objects.
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
}