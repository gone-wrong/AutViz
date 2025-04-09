package sk.ukf.autviz.Models;

import sk.ukf.autviz.Views.ViewFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Model {
    private static Model model;
    private final ViewFactory viewFactory;
    private Automata currentAutomata;
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);

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
    }
}
