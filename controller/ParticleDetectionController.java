package controller;

import methods.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Lukado on 23. 11. 2016.
 */
public class ParticleDetectionController extends Thread {
    private String extension = "png";
    private GuiController gc;
    private int size, partNum, min, max;
    private ArrayList<Double> particleRadius;

    ParticleDetectionController(GuiController gc, int size, int partNumber, int minPart, int maxPart) {
        this.gc = gc;
        this.size = size;
        this.partNum = partNumber;
        this.min = minPart;
        this.max = maxPart;
        particleRadius = new ArrayList<>();
    }

    @Override
    public void run(){
        String outputImageFilePath = "img/gray."+extension;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);

        image = generateParticles(image);

        updProg(-1);
        ImageController.writeImage(image, outputImageFilePath, extension);
        gc.updateImg(outputImageFilePath);


        executeMethod();

//        outputImageFilePath = "img/result."+extension;
//        image = new Masks(image, 1, -1,this).getImg();

//        updProg(-1);
//        ImageController.writeImage(image, outputImageFilePath, extension);
//        updProg(1);
//        gc.updateImg(outputImageFilePath);
    }

    private void executeMethod() {
                if (gc.rbm1.isSelected()) gc.updateResult(new SimpleMethod(particleRadius).getResult());
                else if (gc.rbm2.isSelected()) gc.updateHist(new SurfaceHistogram(particleRadius).getHist());
                else if (gc.rbm3.isSelected()) new RingDetection();
                else if (gc.rbm4.isSelected()) new DistanceField();

    }

    private BufferedImage generateParticles(BufferedImage img) {
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.BLACK);
        g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint (RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        g.setPaint(Color.WHITE);
        for (int i = 0; i<partNum; i++){
            Random r = new Random();
            int radius = r.nextInt(max-min+1)+min;
            particleRadius.add(radius*0.5);
            g.fillOval(r.nextInt(size)-radius/2, r.nextInt(size)-radius/2, radius, radius);
        }
        g.dispose();
        return img;
    }

    public void updProg(double value) {
        gc.updateProgress(value);
    }
}
