package methods;

import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.Collections;

public class SurfaceHistogram {
    private XYChart.Series<Number, String> data;

    public SurfaceHistogram(ArrayList<Double> particleRadius, double sliderVal) {
        data = new XYChart.Series<>();
        Collections.sort(particleRadius);
        double step = particleRadius.get(particleRadius.size()-1)/sliderVal;
        double divider = step;
        int count=0;

        for (int i = 0; i < particleRadius.size(); i++) {
            if (particleRadius.get(i)<=divider){
                System.out.println(particleRadius.get(i)+" : "+divider);
                count++;
            }else{
                System.out.println(particleRadius.get(i)+" : "+divider);
                data.getData().add(new XYChart.Data<>(count, "S="+(int)divider));
                count = 1;
                divider+=step;
                while(divider<particleRadius.get(i)){
                    data.getData().add(new XYChart.Data<>(0, "S="+(int)divider));
                    divider+=step;
                }
            }
            if (i+1==particleRadius.size()) data.getData().add(new XYChart.Data<>(count, "S="+(int)divider));
        }
    }

    public XYChart.Series<Number, String> getHist() {
        return data;
    }
}
