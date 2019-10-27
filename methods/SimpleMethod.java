package methods;

import java.util.ArrayList;
import java.util.Collections;

public class SimpleMethod {
    private double result;
    private ArrayList<Double> particleRadius;

    public SimpleMethod(ArrayList<Double> particleRadius) {
        this.particleRadius = particleRadius;
    }

    public double getResult() {
        return result;
    }

    public void doMean() {
        double avgVolume;
        double wholeVol = 0;

        for (double r : particleRadius) wholeVol += r;
        avgVolume = wholeVol / particleRadius.size();
//        System.out.println(avgVolume);

        countResult(avgVolume);
    }

    public void doMedian() {
        double median;
        Collections.sort(particleRadius);
        if ((particleRadius.size()/2.0)%1==0){
            median = particleRadius.get(particleRadius.size()/2);
        }else{
            median = (particleRadius.get(particleRadius.size()/2)+particleRadius.get(particleRadius.size()/2+1))/2.0;
        }
//        System.out.println(median);
        countResult(median);
    }

    public void doMidRange() {
        double mid;
        Collections.sort(particleRadius);
        mid = (particleRadius.get(0)+particleRadius.get(particleRadius.size()-1))/2;
//        System.out.println(mid);
        countResult(mid);
    }

    private void countResult(double divisor) {
        ArrayList<Double> multiplicity = new ArrayList<>();
        for (double r : particleRadius) multiplicity.add(r / divisor);
//        System.out.println(multiplicity);

        for (int i = 0; i < particleRadius.size(); i++) {
            if (particleRadius.get(i) <= divisor) {
                result += 1;
            } else {
                result += multiplicity.get(i);
            }
        }
//        System.out.println(result);
    }
}
