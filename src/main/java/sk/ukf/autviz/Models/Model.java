package sk.ukf.autviz.Models;

import sk.ukf.autviz.Views.ViewFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Model {
    private static Model model;
    private final ViewFactory viewFactory;
    private Automata currentAutomata;
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);
    private final BooleanProperty deleteStateMode = new SimpleBooleanProperty(false);
    private final BooleanProperty deleteEdgeMode = new SimpleBooleanProperty(false);
    private final BooleanProperty addEdgeMode = new SimpleBooleanProperty(false);

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
        return currentAutomata;
    }

    public void setCurrentAutomata(Automata automata) {
        this.currentAutomata = automata;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
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
}
