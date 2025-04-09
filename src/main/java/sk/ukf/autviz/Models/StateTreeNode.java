package sk.ukf.autviz.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StateTreeNode {
    private final State state;             // The original state this node represents.
    private final boolean isPrimary;       // True if this node is a primary instance (with outgoing transitions)
    private final List<StateTreeNode> children;
    private final String transitionLabel;  // The transition label that led from the parent to this node.
    private final Transition transition;   // The actual Transition object (null for the root)

    // For non-root nodes – includes the transition that led from the parent.
    public StateTreeNode(State state, boolean isPrimary, String transitionLabel, Transition transition) {
        this.state = state;
        this.isPrimary = isPrimary;
        this.transitionLabel = transitionLabel;
        this.transition = transition;
        this.children = new ArrayList<>();
    }

    // For root node, no incoming transition.
    public StateTreeNode(State state, boolean isPrimary) {
        this(state, isPrimary, "", null);
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

    public String getTransitionLabel() {
        return transitionLabel;
    }

    public Transition getTransition() {
        return transition;
    }

    // Other utility methods…
    @Override
    public String toString() {
        return state.getName();
    }
}
