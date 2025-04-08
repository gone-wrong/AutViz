package sk.ukf.autviz.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StateTreeNode {
    private final State state;             // Pôvodný stav, ktorý tento uzol reprezentuje.
    private final boolean isPrimary;       // Flag označujúci, či je tento uzol primárny (má výstupné prechody).
    private final List<StateTreeNode> children;
    private final String transitionLabel;  // Označenie prechodu z rodiča na tento uzol.

    public StateTreeNode(State state, boolean isPrimary, String transitionLabel) {
        this.state = state;
        this.isPrimary = isPrimary;
        this.transitionLabel = transitionLabel;
        this.children = new ArrayList<>();
    }

    // Pre koreň stromu nemusíme mať prechodové označenie – môžeme použiť prázdny reťazec.
    public StateTreeNode(State state, boolean isPrimary) {
        this(state, isPrimary, "");
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

    public String toIndentedString(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(state.getName());
        if (transitionLabel != null && !transitionLabel.isEmpty()) {
            sb.append(" [").append(transitionLabel).append("]");
        }
        if (isPrimary) {
            sb.append(" [Primary]");
        } else {
            sb.append(" [Dup]");
        }
        sb.append("\n");
        for (StateTreeNode child : children) {
            sb.append(child.toIndentedString(indent + "  "));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return state.getName();
    }
}