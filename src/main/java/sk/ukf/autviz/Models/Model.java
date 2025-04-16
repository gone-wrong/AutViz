package sk.ukf.autviz.Models;

import sk.ukf.autviz.Views.ViewFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;
import java.util.Map;

public class Model {
    private static Model model;
    private final ViewFactory viewFactory;
    private Automata currentAutomata;
    private Map<State, StateCellData> stateMapping = new HashMap<>();

    private final BooleanProperty vizMode = new SimpleBooleanProperty(false);
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);
    private final BooleanProperty deleteStateMode = new SimpleBooleanProperty(false);
    private final BooleanProperty deleteEdgeMode = new SimpleBooleanProperty(false);
    private final BooleanProperty addEdgeMode = new SimpleBooleanProperty(false);

    private final BooleanProperty updateView1 = new SimpleBooleanProperty(false);
    private final BooleanProperty updateView2 = new SimpleBooleanProperty(false);
    private final BooleanProperty updateView3 = new SimpleBooleanProperty(false);
    private final BooleanProperty updateStateMapping = new SimpleBooleanProperty(false);

    private Model() {
        this.viewFactory = new ViewFactory();
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    public Automata getCurrentAutomata() {
        if (currentAutomata == null) {
            currentAutomata = createSampleAutomaton();
        }
        return currentAutomata;
    }

    public void setCurrentAutomata(Automata automata) {
        this.currentAutomata = automata;
        automata.updateAlphabet();
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public BooleanProperty vizModeProperty() {
        return vizMode;
    }

    public boolean isVizMode() {
        return vizMode.get();
    }

    public void setVizMode(boolean vizMode) {
        if(vizMode) {
            setEditMode(false);
            setDeleteStateMode(false);
            setDeleteEdgeMode(false);
            setAddEdgeMode(false);
        }
        this.vizMode.set(vizMode);
    }

    public BooleanProperty editModeProperty() {
        return editMode;
    }

    public boolean isEditMode() {
        return editMode.get();
    }

    public void setEditMode(boolean value) {
        editMode.set(value);
        if(value) {
            setDeleteStateMode(false);
            setDeleteEdgeMode(false);
            setAddEdgeMode(false);
        }
    }

    public BooleanProperty deleteStateModeProperty() {
        return deleteStateMode;
    }

    public boolean isDeleteStateMode() {
        return deleteStateMode.get();
    }

    public void setDeleteStateMode(boolean value) {
        deleteStateMode.set(value);
        if(value) {
            setEditMode(false);
            setDeleteEdgeMode(false);
            setAddEdgeMode(false);
        }
    }

    public boolean isDeleteEdgeMode() {
        return deleteEdgeMode.get();
    }

    public BooleanProperty deleteEdgeModeProperty() {
        return deleteEdgeMode;
    }

    public void setDeleteEdgeMode(boolean value) {
        deleteEdgeMode.set(value);
        if(value) {
            setEditMode(false);
            setDeleteStateMode(false);
            setAddEdgeMode(false);
        }
    }

    public boolean isAddEdgeMode() {
        return addEdgeMode.get();
    }

    public BooleanProperty addEdgeModeProperty() {
        return addEdgeMode;
    }

    public void setAddEdgeMode(boolean value) {
        addEdgeMode.set(value);
        if(value) {
            setEditMode(false);
            setDeleteStateMode(false);
            setDeleteEdgeMode(false);
        }
    }

    public void disableModes() {
        editMode.set(false);
        deleteStateMode.set(false);
        deleteEdgeMode.set(false);
        addEdgeMode.set(false);
    }

    public BooleanProperty updateView1Property() {
        return updateView1;
    }

    public boolean isUpdateView1() {
        return updateView1.get();
    }

    public void setUpdateView1(boolean value) {
        updateView1.set(value);
    }

    public BooleanProperty updateView2Property() {
        return updateView2;
    }

    public boolean isUpdateView2() {
        return updateView2.get();
    }

    public void setUpdateView2(boolean value) {
        updateView2.set(value);
    }

    public BooleanProperty updateView3Property() {
        return updateView3;
    }

    public boolean isUpdateView3() {
        return updateView3.get();
    }

    public void setUpdateView3(boolean value) {
        updateView3.set(value);
    }

    public BooleanProperty updateStateMappingProperty() {
        return updateStateMapping;
    }

    public boolean isUpdateStateMapping() {
        return updateStateMapping.get();
    }

    public void setUpdateStateMapping(boolean value) {
        updateStateMapping.set(value);
    }

    private Automata createSampleAutomaton() {
        Automata a = new Automata();

//        State q0 = new State("q0");
//        State q1 = new State("q1");
//        State q2 = new State("qqqqq2"); // Begin a End
//        State q3 = new State("q3");
//        State q4 = new State("q4"); // End
//
//        // nastavenie príznakov:
//        q2.setStateBegin(true);
//        q2.setStateEnd(true);
//        q4.setStateEnd(true);
//
//        a.addState(q0);
//        a.addState(q1);
//        a.addState(q4);
//        a.addState(q3);
//        a.addState(q2);
//
//        a.addTransition(new Transition(q1, "b", q0));
//        a.addTransition(new Transition(q2, "a", q0));
//        a.addTransition(new Transition(q0, "b", q1));
//        a.addTransition(new Transition(q0, "b", q3));
//        a.addTransition(new Transition(q0, "a", q3));
//        a.addTransition(new Transition(q1, "a", q3));
//        a.addTransition(new Transition(q1, "b", q1));
//        a.addTransition(new Transition(q3, "b", q4));
//        a.addTransition(new Transition(q4, "a", q2));

        State s0 = new State("s0"), s1 = new State("s1"), s2 = new State("s2"),
                s3 = new State("s3"), s4 = new State("s4");
        s0.setStateBegin(true);
        s0.setStateEnd(true);
        s2.setStateEnd(true);
        s4.setStateEnd(true);
        a.addState(s0);
        a.addState(s1);
        a.addState(s2);
        a.addState(s3);
        a.addState(s4);
        a.addTransition(new Transition(s0, "a", s0));
        a.addTransition(new Transition(s0, "b", s1));
        a.addTransition(new Transition(s1, "b", s3));
        a.addTransition(new Transition(s1, "a", s2));
        a.addTransition(new Transition(s2, "a", s2));
        a.addTransition(new Transition(s2, "b", s4));
        a.addTransition(new Transition(s3, "a", s2));
        a.addTransition(new Transition(s3, "b", s3));
        a.addTransition(new Transition(s4, "a,b", s4));

        return a;
    }

    public Map<State, StateCellData> getStateMapping() {
        return stateMapping;
    }

    public void setStateMapping(Map<State, StateCellData> mapping) {
        this.stateMapping = mapping;
    }

    public void clearStateMapping() {
        stateMapping.clear();
    }
}
