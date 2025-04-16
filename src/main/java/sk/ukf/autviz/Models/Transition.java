package sk.ukf.autviz.Models;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Transition {
    private final State stateSource;
    private final Set<String> symbols = new LinkedHashSet<>();
    private final State stateDestination;

    private final ReadOnlyStringWrapper characterWrapper = new ReadOnlyStringWrapper();

    private final BooleanProperty hasOpposite = new SimpleBooleanProperty(false);

    public Transition(State stateSource, String symbol, State stateDestination) {
        this.stateSource = stateSource;
        this.stateDestination = stateDestination;
        if (symbol.contains(",")) {
            setSymbols(symbol);
        } else {
            String processedSymbol = processSymbol(symbol);
            if (!processedSymbol.isEmpty()) {
                symbols.add(processedSymbol);
            }
            updateCharacterWrapper();
        }
    }

    private String processSymbol(String sym) {
        String trimmed = sym.trim();
        if (trimmed.isEmpty()) {
            return "ε";
        }
        if (trimmed.length() > 1) {
            return "";
        }
        return trimmed;
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

    public void setSymbols(String symbolsStr) {
        String[] tokens = symbolsStr.split(",", -1);
        List<String> processedTokens = new ArrayList<>();

        for (String token : tokens) {
            String processed = processSymbol(token);
            if (!processed.isEmpty()) {
                processedTokens.add(processed);
            }
        }

        if (!processedTokens.isEmpty()) {
            symbols.clear();
            symbols.addAll(processedTokens);
        }

        updateCharacterWrapper();
    }

    private void updateCharacterWrapper() {
        if (symbols.size() == 1 && symbols.contains("ε")) {
            characterWrapper.set("ε");
        } else {
            characterWrapper.set(String.join(",", symbols));
        }
    }

    public BooleanProperty hasOppositeProperty() {
        return hasOpposite;
    }

    public void setHasOpposite(boolean value) {
        hasOpposite.set(value);
    }

    public void updateOppositeStatus(Set<Transition> allTransitions) {
        boolean foundOpposite = allTransitions.stream().anyMatch(t ->
                t != this &&
                        t.getStateSource().getName().equals(this.getStateDestination().getName()) &&
                        t.getStateDestination().getName().equals(this.getStateSource().getName())
        );
        setHasOpposite(foundOpposite);
    }
}