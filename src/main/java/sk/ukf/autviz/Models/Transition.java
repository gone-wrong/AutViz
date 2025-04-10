package sk.ukf.autviz.Models;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Transition {
    private State stateSource;
    private final Set<String> symbols = new LinkedHashSet<>();
    private State stateDestination;

    private final ReadOnlyStringWrapper characterWrapper = new ReadOnlyStringWrapper();

    public Transition(State stateSource, String symbol, State stateDestination) {
        this.stateSource = stateSource;
        this.stateDestination = stateDestination;
        String processedSymbol = processSymbol(symbol);
        symbols.add(processedSymbol);
        updateCharacterWrapper();
    }

    private String processSymbol(String sym) {
        String trimmed = sym.trim();
        return trimmed.isEmpty() ? "ε" : trimmed;
    }

    public State getStateDestination() {
        return stateDestination;
    }

    public State getStateSource() {
        return stateSource;
    }

    public String getCharacter() {
        return characterWrapper.get();
    }

    public StringProperty characterProperty() {
        return characterWrapper;
    }

    public Set<String> getSymbols() {
        return symbols;
    }

    public void addSymbol(String symbol) {
        symbols.add(symbol);
        updateCharacterWrapper();
    }

    public void removeSymbol(String symbol) {
        String processedSymbol = processSymbol(symbol);
        symbols.remove(processedSymbol);
        updateCharacterWrapper();
    }

    public void setSymbols(String symbolsStr) {
        symbols.clear();
        String[] tokens = symbolsStr.split(",", -1);
        for (String token : tokens) {
            symbols.add(processSymbol(token));
        }
        updateCharacterWrapper();
    }

    private void updateCharacterWrapper() {
        if (symbols.isEmpty()) {
            characterWrapper.set("ε");
        } else {
            characterWrapper.set(String.join(",", symbols));
        }
    }
}