package controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
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
    private BufferedImage image;

    ParticleDetectionController(GuiController gc, Image img, String ext){
        this.gc = gc;
        this.extension = ext;
        this.image = SwingFXUtils.fromFXImage(img, this.image);
        particleRadius = new ArrayList<>();
    }

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
        String outputImageFilePath = "img/result." + extension;
        String test = "img/test." + extension;
        if (this.size != 0) {
            image = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);

            generateParticles(image);

            updProg(-1);
            ImageController.writeImage(image, outputImageFilePath, extension);
            updProg(1);
            gc.updateImg(outputImageFilePath);
        }

        seedAlgorithm(image);
        ImageController.writeImage(image, test, extension);
        gc.updateImg(test);

//        outputImageFilePath = "img/result."+extension;
//        image = new Masks(image, 1, -1,this).getImg();

//        updProg(-1);
//        ImageController.writeImage(image, outputImageFilePath, extension);
//        updProg(1);
//        gc.updateImg(outputImageFilePath);
    }

    /**
     * Algoritmus pro nalezení všech částic na snímku a výpočet jejich obsahu/obvodu.
     */
    private void seedAlgorithm(BufferedImage img) {
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int val = img.getRGB(i, j);
                if(val>new Color(128,128,128).getRGB()){
                    img.setRGB(i, j, new Color(255, 0,0).getRGB());
                }
            }
            double prog = i/(double)img.getWidth();
            updProg(prog);
        }
    }

    public void executeMethod() {
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
            g.fillOval(r.nextInt(size)-radius/2, r.nextInt(size)-radius/2, radius, radius);
        }
        g.dispose();
        return img;
    }

    public void updProg(double value) {
        gc.updateProgress(value);
    }
}
