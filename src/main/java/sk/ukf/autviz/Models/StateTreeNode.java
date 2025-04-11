package sk.ukf.autviz.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StateTreeNode {
    private final State state;
    private final boolean isPrimary;
    private final List<StateTreeNode> children;
    private final Transition transition;

    // transition - prechod od rodiča
    public StateTreeNode(State state, boolean isPrimary, Transition transition) {
        this.state = state;
        this.isPrimary = isPrimary;
        this.transition = transition;
        this.children = new ArrayList<>();
    }

    // root node, bez prichádzajúceho prechodu
    public StateTreeNode(State state, boolean isPrimary) {
        this(state, isPrimary, null);
    }

    public void addChild(StateTreeNode child) {
        children.add(child);
    }

    public State getState() {
        return state;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public List<StateTreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Transition getTransition() {
        return transition;
    }

    public String getTransitionLabel() {
        return transition != null ? transition.getCharacter() : "";
    }

    @Override
    public String toString() {
        return state.getName();
    }
}
