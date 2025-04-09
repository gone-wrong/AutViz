package sk.ukf.autviz.Utils;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import sk.ukf.autviz.Models.State;
import sk.ukf.autviz.Models.Model;

import java.util.Optional;

public class CircleCell extends AbstractCell {

    private Region view;

    // state Objekt typu State, ktorý obsahuje názov a príznaky.
    public CircleCell(State state) {
        // Vytvor základný kruh pre stav.
        Circle circle = new Circle(30);
        circle.setFill(Color.LIGHTBLUE);
        circle.setStroke(Color.DARKBLUE);
        circle.setStrokeWidth(1);

        // Vytvor textový prvok so štítkom stavu.
        Text label = new Text(state.getName());

        // Zoskup základný kruh a text do StackPane – text sa automaticky centrová.
        StackPane circlePane = new StackPane(circle, label);
        circlePane.setPrefSize(60, 60);
        // Uistíme sa, že stred circlePane je v strede (štandardné chovanie StackPane)

        // Vytvor základný container, ktorý bude vždy mať stred rovnaký ako circlePane.
        // Použijeme StackPane, pretože on zachováva centrovanie.
        StackPane basePane = new StackPane();
        basePane.setPrefSize(60, 60);

        // Ak je stav počiatočný (stateBegin), pridaj trojuholníkový indikátor.
        if (state.isStateBegin()) {
            // Vytvor trojuholník, ktorý bude slúžiť ako indikátor počiatočného stavu.
            Polygon triangle = new Polygon();
            // Definujeme body trojuholníka tak, aby jeho "hrot" bol mimo kruhu,
            // ale celý indikátor sa nepresunul – použijeme fixný offset.
            triangle.getPoints().addAll(
                    20.0, 25.0,   // bod, ktorý bude umiestnený tesne vľavo od kruhu
                    0.0, 0.0,
                    0.0, 50.0
            );
            triangle.setFill(Color.TRANSPARENT); // nevyplnený
            triangle.setStroke(Color.BLACK);
            triangle.setStrokeWidth(2);
            // Nastavíme translateX, aby trojuholník "visel" vedľa kruhu, bez toho aby ovplyvnil centrovanie basePane.
            triangle.setTranslateX(-30); // posuňme trojuholník doľava (v závislosti od požadovaného vzhľadu)

            // Vložime trojuholník do rovnakej StackPane, aby sa prekrýval s circlePane,
            // ale keďže má vlastný translateX, celkový stred basePane zostane stred kruhu.
            basePane.getChildren().add(triangle);
        }
        // Pridávame CirclePane po sipke aby sipka bola na spodku
        basePane.getChildren().add(circlePane);

        // Ak je stav koncový (stateEnd), pridaj vonkajší kruh (outer border)
        if (state.isStateEnd()) {
            Circle outer = new Circle(32); // mierne väčší ako základný kruh
            outer.setFill(Color.TRANSPARENT);
            outer.setStroke(Color.DARKBLUE);
            outer.setStrokeWidth(1);
            // Vytvoríme ďalší StackPane, do ktorého vložíme outer a basePane.
            StackPane outerPane = new StackPane();
            outerPane.setPrefSize(64, 64);
            outerPane.getChildren().addAll(outer, basePane);
            view = outerPane;
        } else {
            view = basePane;
        }

        view.setOnMouseClicked(event -> {
            if (Model.getInstance().isEditMode()) {
                // Zobraz TextInputDialog pre úpravu názvu stavu.
                TextInputDialog dialog = new TextInputDialog(label.getText());
                dialog.setTitle("Edit State");
                dialog.setHeaderText("Uprav názov stavu");
                dialog.setContentText("Nový názov:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newName -> {
                    label.setText(newName);
                    state.setStateName(newName);
                });
            }
        });
    }

    /**
     * Pohodlný konštruktor pre obyčajný stav (bez špeciálnych označení).
     */
    public CircleCell(String label) {
        // Predpokladáme, že ak nie sú explicitne nastavené príznaky, ide o obyčajný stav.
        this(new State(label));
    }

    @Override
    public Region getGraphic(Graph graph) {
        return view;
    }
}