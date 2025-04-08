package sk.ukf.autviz;

import javafx.application.Application;
import javafx.stage.Stage;
import sk.ukf.autviz.Models.Model;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Model.getInstance().getViewFactory().showClientWindow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}