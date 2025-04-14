module sk.ukf.autviz {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires javafx.graphics;
    requires org.abego.treelayout.core;
    requires fxgraph;
    requires org.json;
    requires java.sql;

    opens sk.ukf.autviz to javafx.fxml;
    exports sk.ukf.autviz;
    exports sk.ukf.autviz.Controllers;
    exports sk.ukf.autviz.Models;
    exports sk.ukf.autviz.Views;
}