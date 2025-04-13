package sk.ukf.autviz.Controllers;

import sk.ukf.autviz.Controllers.View1Controller;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import sk.ukf.autviz.Models.Model;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientMenuController implements Initializable {
    public Button view1_btn;
    public Button view2_btn;
    public Button view3_btn;
    public Button alg1_btn;
    public Button alg2_btn;
    public VBox client_menu;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ClientMenuController Initialize");
        addListeners();
    }

    private void addListeners() {
        view1_btn.setOnAction(event -> onView1());
        view2_btn.setOnAction(event -> onView2());
        view3_btn.setOnAction(event -> onView3());
    }

    private void onView1() {
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().set("View1");
    }

    private void onView2() {
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().set("View2");
    }

    private void onView3() {
        Model.getInstance().getViewFactory().getClientSelectedViewProperty().set("View3");
    }

}
