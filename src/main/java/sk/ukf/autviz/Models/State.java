package sk.ukf.autviz.Models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

public class State implements Comparable<State> {
    private final StringProperty stateName = new SimpleStringProperty();
    private final BooleanProperty stateEnd = new SimpleBooleanProperty(false);
    private final BooleanProperty stateBegin = new SimpleBooleanProperty(false);
    private final BooleanProperty isActive = new SimpleBooleanProperty(false);

    public State(String name) {
        this.stateName.set(name);
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void setActive(boolean active) {
        isActive.set(active);
    }

    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    public String getName() {
        return stateName.get();
    }

    public StringProperty nameProperty() {
        return stateName;
    }

    public void setStateName(String name) {
        stateName.set(name);
    }

    public boolean isStateEnd() {
        return stateEnd.get();
    }

    public void setStateEnd(boolean end) {
        stateEnd.set(end);
    }

    public BooleanProperty stateEndProperty() {
        return stateEnd;
    }

    public boolean isStateBegin() {
        return stateBegin.get();
    }

    public void setStateBegin(boolean begin) {
        stateBegin.set(begin);
    }

    public BooleanProperty stateBeginProperty() {
        return stateBegin;
    }

    public int compareTo(State other)
    {
        return getName().compareTo(other.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof State other) {
            return other.getName().equals(this.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stateName);
    }

    public boolean isDuplicateIn(Iterable<State> existingStates) {
        for (State s : existingStates) {
            if (s.getName().equals(this.getName())) {
                return true;
            }
        }
        return false;
    }
}