package controller;

import methods.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

/**
 * Created by Lukado on 23. 11. 2016.
 */
public class ParticleDetectionController extends Thread {
    private String extension = "png";
    private GuiController gc;
    private int size, partNum, min, max;
    private ArrayList<Double> particleRadius;
    private BufferedImage image;

    ParticleDetectionController(GuiController gc, String fileLoc, String ext) {
        this(gc, 0, 0, 0, 0);
        this.extension = ext;
        this.image = ImageController.readImage(fileLoc);
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
    public void run() {
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
        gc.updateResult(particleRadius.size());

        executeMethod();
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
        Stack<Point> s = new Stack<>();
        double particleContent = 0;

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int val = img.getRGB(i, j);
                if (val == Color.WHITE.getRGB()) {
                    particleContent++;
                    img.setRGB(i, j, Color.RED.getRGB());
                    // 4okoli-8okoli do zasobniku
                    check8d(s, img, i, j);

                    while (!s.empty()) {
                        Point p = s.pop();
                        if (img.getRGB(p.x, p.y) == Color.RED.getRGB()) continue;
                        particleContent++;
                        img.setRGB(p.x, p.y, Color.RED.getRGB());
                        check8d(s, img, p.x, p.y);
                    }

                    Graphics2D g = (Graphics2D) img.getGraphics();
                    g.setFont(new Font("Serif", Font.PLAIN, 10));
                    g.setColor(Color.CYAN);
                    g.drawString(particleContent + "", i, j);
                    g.dispose();

                    particleRadius.add(particleContent);
                    particleContent=0;
                }
            }

            double prog = i / (double) img.getWidth();
            updProg(prog);
        }
    }

    private void check8d(Stack<Point> s, BufferedImage img, int x, int y) {
        for (int i = -1; i <= 1; i++) {
            if ((x+i)<0 || (x+i)>img.getWidth()-1) continue;
            for (int j = -1; j <= 1; j++) {
                if((y+j)<0 || (y+j)>img.getHeight()-1) continue;

                int val = img.getRGB(x + i, y + j);
                if (!(i == 0 && j == 0) && val == Color.WHITE.getRGB()) {
                    s.push(new Point(x + i, y + j));
                }
            }
        }
    }

    public void executeMethod() {
        if (gc.rbm1.isSelected()) {
            SimpleMethod sm = new SimpleMethod(particleRadius);
            if (gc.rbp1.isSelected()) sm.doMean();
            else if (gc.rbp2.isSelected()) sm.doMedian();
            else if (gc.rbp3.isSelected()) sm.doMidRange();
            gc.updateResult(sm.getResult());
        }
        else if (gc.rbm2.isSelected()){
            SurfaceHistogram sh = new SurfaceHistogram(particleRadius, gc.histSlider.getValue());
            gc.updateHist(sh.getHist(), particleRadius.size());
        }
        else if (gc.rbm3.isSelected()) new RingDetection();
        else if (gc.rbm4.isSelected()) new DistanceField();
    }

    private BufferedImage generateParticles(BufferedImage img) {
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        g.setPaint(Color.WHITE);
        for (int i = 0; i < partNum; i++) {
            Random r = new Random();
            int radius = r.nextInt(max - min + 1) + min;
            g.fillOval(r.nextInt(size) - radius / 2, r.nextInt(size) - radius / 2, radius, radius);
        }
        g.dispose();
        return img;
    }

    public void updProg(double value) {
        gc.updateProgress(value);
    }

}
