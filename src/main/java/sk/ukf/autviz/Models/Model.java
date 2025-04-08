package sk.ukf.autviz.Models;

import sk.ukf.autviz.Views.ViewFactory;

public class Model {
    private static Model model;
    private final ViewFactory viewFactory;
    private Automata currentAutomata;

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
}
