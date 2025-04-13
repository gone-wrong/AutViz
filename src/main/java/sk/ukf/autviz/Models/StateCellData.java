package sk.ukf.autviz.Models;

import com.fxgraph.graph.ICell;
import javafx.scene.layout.Region;

public class StateCellData {
    private final ICell cell;
    private final Region graphicNode;
    private double layoutX;
    private double layoutY;

    public StateCellData(ICell cell, Region graphicNode) {
        this.cell = cell;
        this.graphicNode = graphicNode;
        // Capture the initial layout positions (likely 0,0)
        this.layoutX = graphicNode.getLayoutX();
        this.layoutY = graphicNode.getLayoutY();
    }

    public ICell getCell() {
        return cell;
    }

    public Region getGraphicNode() {
        return graphicNode;
    }

    public double getLayoutX() {
        return layoutX;
    }

    public double getLayoutY() {
        return layoutY;
    }

    public void setLayoutX(double x) {
        this.layoutX = x;
    }

    public void setLayoutY(double y) {
        this.layoutY = y;
    }
}

