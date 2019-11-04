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
    private String outputImageFilePath = "img/result." + extension;
    private String inputImageFilePath = "img/input." + extension;

    private GuiController gc;
    private int size, partNum;
    private double min, max;
    private ArrayList<Double> particleVolume;
    private ArrayList<Double> particlePeriphery;
    private BufferedImage image;
    private SurfaceHistogram sh;

    ParticleDetectionController(GuiController gc, String fileLoc, String ext) {
        this(gc, 0, 0, 0, 0);
        this.extension = ext;
        this.image = ImageController.readImage(fileLoc);
    }

    ParticleDetectionController(GuiController gc, int size, int partNumber, double minPart, double maxPart) {
        this.gc = gc;
        this.size = size;
        this.partNum = partNumber;
        this.min = minPart;
        this.max = maxPart;
        particleVolume = new ArrayList<>();
        particlePeriphery = new ArrayList<>();
    }

    @Override
    public void run() {
        if (this.size != 0) {
            image = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);
            generateParticles(image);
        }

        ImageController.writeImage(image, inputImageFilePath, extension);
        seedAlgorithm(image);
        updProg(-1);
        ImageController.writeImage(image, outputImageFilePath, extension);
        updProg(1);
        if (gc.debugMode) gc.updateImg(outputImageFilePath);
        else gc.updateImg(inputImageFilePath);

        gc.updateResult("Počet nalezených stop: " + particleVolume.size());
        executeMethod();
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

                    // obvod
                    countPeriphery(img, i, j);
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

                    particleVolume.add(particleContent);
                    particleContent = 0;
                }
            }

            double prog = i / (double) img.getWidth();
            updProg(prog);
        }
    }

    private void countPeriphery(BufferedImage img, int x, int y) {
        double periphery = 0;
        Point p = new Point(x, y);
        Point tmp;
        int loopChecker = 0;
        int counter = 0;
        boolean flag = true;

        ArrayList<Point> surrounding = new ArrayList<>();
        surrounding.add(new Point(0, -1));
        surrounding.add(new Point(+1, -1));

        surrounding.add(new Point(+1, 0));
        surrounding.add(new Point(+1, +1));

        surrounding.add(new Point(0, +1));
        surrounding.add(new Point(-1, +1));

        surrounding.add(new Point(-1, 0));
        surrounding.add(new Point(-1, -1));

        do {
            tmp = surrounding.get(counter);
            while ((p.x+tmp.x) < 0 || (p.x+tmp.x) > img.getWidth()-1 || (p.y + tmp.y) < 0 || (p.y + tmp.y) > img.getHeight() - 1){
                counter=(counter+1)%8;
                tmp = surrounding.get(counter);
            }

            int val = img.getRGB(p.x + tmp.x, p.y + tmp.y);
            if (val == Color.WHITE.getRGB() || val == Color.RED.getRGB()) {
                periphery++;
                counter-=2;
                if (counter<0) counter+=8;
                p.setLocation(p.x+tmp.x, p.y+tmp.y);
                flag = false;
                loopChecker = 0;
            }else{
                if (loopChecker>8) {
                    gc.updateError("");
                    break;
                }
                loopChecker++;
                counter=(counter+1)%8;
            }
        } while (flag || img.getRGB(p.x, p.y) != Color.RED.getRGB());
        particlePeriphery.add(periphery);
    }

    private void check8d(Stack<Point> s, BufferedImage img, int x, int y) {
        for (int i = -1; i <= 1; i++) {
            if ((x + i) < 0 || (x + i) > img.getWidth() - 1) continue;
            for (int j = -1; j <= 1; j++) {
                if ((y + j) < 0 || (y + j) > img.getHeight() - 1) continue;

                int val = img.getRGB(x + i, y + j);
                if (!(i == 0 && j == 0) && val == Color.WHITE.getRGB()) {
                    s.push(new Point(x + i, y + j));
                }
            }
        }
    }

    public void executeMethod() {
        gc.updateImg(outputImageFilePath);

        if (particleVolume.size() == 0) {
            gc.histVB.setDisable(true);
            gc.updateResult("Nenalezeny žádné stopy!");
            return;
        }

        if (gc.rbm1.isSelected()) {
            SimpleMethod sm = new SimpleMethod(particleVolume);
            if (gc.rbp1.isSelected()) sm.doMean();
            else if (gc.rbp2.isSelected()) sm.doMedian();
            else if (gc.rbp3.isSelected()) sm.doMidRange();
            gc.updateResult("Počet nalezených stop: " + sm.getResult());
        } else if (gc.rbm2.isSelected()) {
            if (gc.histSlider.getMax() != particleVolume.size()) {
                gc.histSlider.setMax(particleVolume.size());
                gc.histSlider.setValue(particleVolume.size() * 0.1);
                gc.histSlider.setBlockIncrement(particleVolume.size() / 100.0);
                gc.histSlider.setMajorTickUnit(particleVolume.size() / 10.0);
                gc.histSlider.setMinorTickCount((int) (particleVolume.size() / 10.0) / 2);
            } else {
                sh = new SurfaceHistogram(particleVolume, gc.histSlider.getValue());
                gc.updateHist(sh.getHist());
                gc.updateResult("Počet nalezených stop: " + sh.getResult());
            }
        } else if (gc.rbm3.isSelected()) {
            RingDetection rd = new RingDetection(particleVolume, particlePeriphery, Double.parseDouble(gc.tfr1.getText()), Double.parseDouble(gc.tfr2.getText()));
            gc.updateResult("Počet nalezených stop: "+rd.getResult());
        }
        else if (gc.rbm4.isSelected()) {
            DistanceField df = new DistanceField(ImageController.readImage(inputImageFilePath), this, Double.parseDouble(gc.tfd1.getText()));
            if (gc.rbd1.isSelected()) df.calculateParticles(false);
            else if (gc.rbd2.isSelected()) df.calculateParticles(true);
            gc.updateImg(df.getDFImage());
            gc.updateResult("Počet nalezených stop: "+df.getResult());
        }
    }

    private BufferedImage generateParticles(BufferedImage img) {
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        g.setPaint(Color.WHITE);
        for (int i = 0; i < partNum; i++) {
            Random r = new Random();
            double radius = r.nextDouble() * (max - min) + min;
            g.fillOval((int) (r.nextInt(size) - radius / 2), (int) (r.nextInt(size) - radius / 2), (int) radius, (int) radius);
        }
        g.dispose();
        return img;
    }

    public void updProg(double value) {
        gc.updateProgress(value);
    }

    public SurfaceHistogram getSH() {
        return sh;
    }

    public GuiController getGc(){
        return gc;
    }

}
