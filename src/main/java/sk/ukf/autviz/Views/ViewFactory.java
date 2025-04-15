package sk.ukf.autviz.Views;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sk.ukf.autviz.Controllers.ClientController;

public class ViewFactory {

    private final StringProperty clientSelectedView;
    private AnchorPane View1;
    private AnchorPane View2;
    private AnchorPane View3;

    public ViewFactory() {
        this.clientSelectedView = new SimpleStringProperty("View1");
    }

    public StringProperty getClientSelectedViewProperty() {
        return this.clientSelectedView;
    }

    public AnchorPane getView1() {
        if (View1 == null) {
            try {
                View1 = new FXMLLoader(getClass().getResource("/Fxml/View1.fxml")).load();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return View1;
    }

    public AnchorPane getView2() {
        if (View2 == null) {
            try {
                View2 = new FXMLLoader(getClass().getResource("/Fxml/View2.fxml")).load();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return View2;
    }

    public AnchorPane getView3() {
        if (View3 == null) {
            try {
                View3 = new FXMLLoader(getClass().getResource("/Fxml/View3.fxml")).load();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return View3;
    }

    public void showClientWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client.fxml"));
        ClientController clientController = new ClientController();
        loader.setController(clientController);
        createStage(loader);
    }

    private void createStage(FXMLLoader loader) {
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Stage stage = new Stage();

        stage.setScene(scene);
        stage.setTitle("AutViz");
        stage.show();
    }

    public void closeStage(Stage stage) {
        stage.close();
    }
}
