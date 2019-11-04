package methods;

import java.util.ArrayList;
import java.util.Collections;

public class RingDetection {
    private double result=0;

    public RingDetection(ArrayList<Double> particleVolume, ArrayList<Double> particlePeriphery, double k, double l) {
        //kruhovost stopy ff
        //odříznutí extrémů 20%
        Collections.sort(particleVolume);
        Collections.sort(particlePeriphery);
        double extreme = particleVolume.get(particleVolume.size()-1)*0.1;
        double avgFF = 0;
        int counter = 0;

        for (int i=0; i<particleVolume.size()-1; i++){
            if (particleVolume.get(i)>extreme || particleVolume.get(i)<particleVolume.get(particleVolume.size()-1)-extreme){
                counter++;
                avgFF += Math.pow(particlePeriphery.get(i),2)/particleVolume.get(i);
            }
        }
        avgFF = avgFF/counter;
        counter = 0;

        ArrayList<Double> volumes = new ArrayList<>();
        //výpočet vol_i
        for(int i=0; i<particleVolume.size()-1;i++){
            volumes.add(Math.pow(particlePeriphery.get(i),2)/(particleVolume.get(i)*avgFF));
        }

        avgFF=0;
        //odstranění víceexpozic extrémů
        for (int i = 0; i< volumes.size()-1; i++){
            if (Math.round(volumes.get(i))==1){
                counter++;
                avgFF += particleVolume.get(i);
            }
        }
        avgFF = avgFF/counter;

        //výpočet vah
        ArrayList<Double> weight = new ArrayList<>();

        for (double d : particleVolume){
            weight.add(d/avgFF);
        }

        //výpočet výsledku
        for (int i = 0; i< particleVolume.size()-1; i++){
            result += (k*volumes.get(i)+l*weight.get(i))/(k+l);
        }
    }

    public double getResult(){
        return result;
    }
}
