package sk.ukf.autviz.Controllers;

import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import sk.ukf.autviz.Models.Model;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public BorderPane client_parent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ClientController Initialize");
        client_parent.setCenter(Model.getInstance().getViewFactory().getView1()); // zo zaciatku bude nastaveny View1
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().addListener(
                (observable, oldValue, newValue) -> {
                    switch (newValue) {
                        case "View2": client_parent.setCenter(Model.getInstance().getViewFactory().getView2()); break;
                        case "View3": client_parent.setCenter(Model.getInstance().getViewFactory().getView3()); break;
                        default: client_parent.setCenter(Model.getInstance().getViewFactory().getView1());
                    }
                });
    }
}
