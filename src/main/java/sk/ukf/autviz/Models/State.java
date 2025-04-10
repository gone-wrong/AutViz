package sk.ukf.autviz.Models;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

public class State implements Comparable<State> {
    private final StringProperty stateName = new SimpleStringProperty();
    private Boolean stateEnd;
    private Boolean stateBegin;

    public State(String name) {
        this.stateName.set(name);
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

    public Boolean isStateEnd() {
        return (stateEnd == Boolean.TRUE);
    }

    public Boolean isStateBegin() {
        return (stateBegin == Boolean.TRUE);
    }

    public void setStateEnd(Boolean end) {
        this.stateEnd = end;
    }

    public void setStateBegin(Boolean begin) {
        this.stateBegin = begin;
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
}