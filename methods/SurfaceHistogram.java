package methods;

import javafx.scene.chart.XYChart;

import java.util.ArrayList;

public class SurfaceHistogram {
    private XYChart.Series data;

    public SurfaceHistogram(ArrayList<Double> particleRadius) {
        data = new XYChart.Series();
        data.setName("2014");

        data.getData().add(new XYChart.Data("Sj", 500));
    }

    public XYChart.Series getHist() {
        return data;
    }
}
