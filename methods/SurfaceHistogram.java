package methods;

import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.Collections;

public class SurfaceHistogram {
    private XYChart.Series<Number, String> data;
    private XYChart.Data<Number, String> refData;
    private double result;

    public SurfaceHistogram(ArrayList<Double> particleRadius, double sliderVal) {
        data = new XYChart.Series<>();
        Collections.sort(particleRadius);
        double step = particleRadius.get(particleRadius.size()-1)/sliderVal;
        double divider = step;
        int count=0;
        refData = new XYChart.Data<>(0,"");

        for (int i = 0; i < particleRadius.size(); i++) {
            if (particleRadius.get(i)<=divider){
//                System.out.println(particleRadius.get(i)+" : "+divider);
                count++;
            }else{
//                System.out.println(particleRadius.get(i)+" : "+divider);
                data.getData().add(new XYChart.Data<>(count, ""+(int)(divider-step)));
                if (refData.getXValue().doubleValue()<count){ refData = data.getData().get(data.getData().size()-1);}
                count = 1;
                divider+=step;
                while(divider<particleRadius.get(i)){
                    data.getData().add(new XYChart.Data<>(0, ""+(int)(divider-step)));
                    divider+=step;
                }
            }
            if (i+1==particleRadius.size()) data.getData().add(new XYChart.Data<>(count, ""+(int)(divider-step)));
        }
        if (refData.getXValue().doubleValue()<count){ refData = data.getData().get(data.getData().size()-1);}

        calculateExpo();
    }

    public void calculateExpo() {
        result = refData.getXValue().doubleValue();

        for (int i = data.getData().indexOf(refData)+1; i < data.getData().size(); i++) {
            result+=(Integer.parseInt(data.getData().get(i).getYValue())/(double)Integer.parseInt(refData.getYValue()))*data.getData().get(i).getXValue().intValue();
        }
    }

    public void setRefValue(XYChart.Data<Number, String> data) {
        refData = data;
    }

    public XYChart.Series<Number, String> getHist() {
        return data;
    }

    public double getRefValue() {
        return refData.getXValue().doubleValue();
    }

    public double getResult(){
        return result;
    }
}