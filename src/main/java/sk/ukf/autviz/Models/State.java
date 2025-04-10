package sk.ukf.autviz.Models;

import java.util.Objects;

public class State implements Comparable<State> {
    private String stateName;
    private Boolean stateEnd;
    private Boolean stateBegin;

    public State(String name) {
        this.stateName = name;
    }

    public String getName() {
        return stateName;
    }

    public void setStateName(String name) {
        this.stateName = name;
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
        if (obj instanceof State) {
            State other = (State) obj;
            return other.getName().equals(this.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stateName);
    }
}