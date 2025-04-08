package sk.ukf.autviz.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sk.ukf.autviz.Models.Automata;
import sk.ukf.autviz.Models.Model;
import sk.ukf.autviz.Models.State;
import sk.ukf.autviz.Models.Transition;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;

public class View2Controller implements Initializable {


    public TableView<TransitionRow> transition_table;

    private static final double COLUMN_PREF_WIDTH = 120;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing View2Controller");
        // Získaj aktuálny automat z Modelu; ak nie je, vytvor ho
        Automata automata = Model.getInstance().getCurrentAutomata();
        buildTransitionMatrix(automata);
        // transition_table.setEditable(false);
    }

    private void buildTransitionMatrix(Automata automata) {
        // Získaj abecedu a stavy z automatu
        // Ak chceš zachovať poradie vloženia, nepoužívaj sort()
        List<String> alphabet = new ArrayList<>(automata.getAlphabet());
        // Ak potrebuješ zoradenie, môžeš použiť Collections.sort(alphabet);
        List<State> states = new ArrayList<>(automata.getStates());
        // Ak chceš zoradiť stavy podľa mena, môžeš použiť:
        // states.sort(Comparator.comparing(State::getName));

        // Vyčisti existujúce stĺpce v tabuľke
        transition_table.getColumns().clear();

        // Vytvor prvý stĺpec pre názov stavu
        TableColumn<TransitionRow, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(cellData -> cellData.getValue().stateNameProperty());
        stateColumn.setPrefWidth(COLUMN_PREF_WIDTH);

        // Vlastný cell factory pre zvýraznenie začiatkových a koncových stavov
        stateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Získame aktuálny riadok
                    TransitionRow row = getTableView().getItems().get(getIndex());
                    String displayText = item;
                    if (row.isBegin() && row.isEnd()) {
                        // Ak je stav oboje, napr. pridáme symbol pred a za názov
                        displayText = "► " + item + " ★";
                    } else if (row.isBegin()) {
                        // Ak je stav len začiatkový, pridáme symbol pred názov
                        displayText = "► " + item;
                    } else if (row.isEnd()) {
                        // Ak je stav len koncový, pridáme symbol za názov
                        displayText = item + " ★";
                    }
                    setText(displayText);
                    // Môžeš pridať aj vlastný štýl, ak chceš
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        transition_table.getColumns().add(stateColumn);

        // Vytvorenie stĺpcov pre každý symbol z abecedy
        for (String symbol : alphabet) {
            TableColumn<TransitionRow, String> symbolCol = new TableColumn<>(symbol);
            symbolCol.setPrefWidth(COLUMN_PREF_WIDTH);
            symbolCol.setCellValueFactory(cellData -> cellData.getValue().getTransition(symbol));
            transition_table.getColumns().add(symbolCol);
        }

        // Vytvor ObservableList riadkov pre TableView
        ObservableList<TransitionRow> rows = FXCollections.observableArrayList();
        for (State state : states) {
            // Pre každý stav vytvor TransitionRow s informáciou, či je začiatkový a/alebo koncový
            TransitionRow row = new TransitionRow(state.getName(), state.isStateBegin(), state.isStateEnd());
            // Pre každý symbol hľadaj prechod zo stavu pre daný symbol
            for (String symbol : alphabet) {
                String destination = "";
                for (Transition t : automata.getTransitions()) {
                    // Ak prechod pochádza zo stavu a vstupný symbol sa nachádza v prechodovom reťazci
                    if (t.getStateSource().getName().equals(state.getName())
                            && t.getCharacter().contains(symbol)) {
                        destination = t.getStateDestination().getName();
                        break;
                    }
                }
                row.setTransition(symbol, destination);
            }
            rows.add(row);
        }
        // Nastav ObservableList ako zdroj dát pre TableView
        transition_table.setItems(rows);

        double fixedRowHeight = 31;  // prispôsob podľa tvojich potrieb
        double headerHeight = 61;    // nastav hodnotu podľa tvojho vzhľadu
        int numRows = rows.size();
//        System.out.println(numRows);
//        System.out.println(headerHeight + numRows * fixedRowHeight);
        transition_table.setPrefHeight(headerHeight + numRows * fixedRowHeight);

        // Dynamické nastavenie preferovanej šírky – sumár všetkých stĺpcov
        double totalWidth = 15;
        for (TableColumn<TransitionRow, ?> col : transition_table.getColumns()) {
            totalWidth += col.getPrefWidth();
//            System.out.println(col.getPrefWidth());
        }
//        System.out.println(totalWidth);
//        transition_table.setPrefWidth(totalWidth);
    }

    // Pomocná vnútorná trieda, ktorá reprezentuje jeden riadok prechodovej matice
    public static class TransitionRow {
        private final SimpleStringProperty stateName;
        private final Map<String, SimpleStringProperty> transitions;
        private final boolean isBegin;
        private final boolean isEnd;

        public TransitionRow(String stateName, boolean isBegin, boolean isEnd) {
            this.stateName = new SimpleStringProperty(stateName);
            this.transitions = new HashMap<>();
            this.isBegin = isBegin;
            this.isEnd = isEnd;
        }

        public StringProperty stateNameProperty() {
            return stateName;
        }

        public boolean isBegin() {
            return isBegin;
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setTransition(String symbol, String destination) {
            transitions.put(symbol, new SimpleStringProperty(destination));
        }

        public SimpleStringProperty getTransition(String symbol) {
            if (!transitions.containsKey(symbol)) {
                transitions.put(symbol, new SimpleStringProperty(""));
            }
            return transitions.get(symbol);
        }
    }
}