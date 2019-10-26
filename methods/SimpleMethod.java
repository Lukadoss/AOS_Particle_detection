package methods;

import java.util.ArrayList;

public class SimpleMethod {
    private double result;

    public SimpleMethod(ArrayList<Double> particleRadius) {
        double avgVolume;
        double wholeVol = 0;
        for (double r:particleRadius) wholeVol+=r;
        avgVolume = wholeVol/particleRadius.size();
//        System.out.println(avgVolume);

        ArrayList<Double> multiplicity = new ArrayList<>();
        for (double r:particleRadius) multiplicity.add(r/avgVolume);
//        System.out.println(multiplicity);

        for (int i =0; i < particleRadius.size(); i++){
            if (particleRadius.get(i)<=avgVolume){
                result+=1;
            }else{
                result+=multiplicity.get(i);
            }
        }
//        System.out.println(result);
    }

    public double getResult(){
        return result;
    }
}
