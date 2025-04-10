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
        symbols.add(symbol);
        updateCharacterWrapper();
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
        symbols.remove(symbol);
        updateCharacterWrapper();
    }

    public void setSymbols(Collection<String> newSymbols) {
        symbols.clear();
        symbols.addAll(newSymbols);
        updateCharacterWrapper();
    }

    private void updateCharacterWrapper() {
        characterWrapper.set(String.join(",", symbols));
    }
}