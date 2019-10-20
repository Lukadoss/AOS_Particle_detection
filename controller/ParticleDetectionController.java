package controller;

import methods.Masks;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by Lukado on 23. 11. 2016.
 */
public class ParticleDetectionController extends Thread {
    private String extension = "png";
    private GuiController gc;
    int size, partNum, min, max;

    ParticleDetectionController(GuiController gc, int size, int partNumber, int minPart, int maxPart) {
        this.gc = gc;
        this.size = size;
        this.partNum = partNumber;
        this.min = minPart;
        this.max = maxPart;
    }

    @Override
    public void run(){
        String outputImageFilePath = "img/gray."+extension;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);

        image = generateParticles(image);

        ImageController.writeImage(image, outputImageFilePath, extension);
        gc.updateImg(outputImageFilePath);

        outputImageFilePath = "img/result."+extension;
        image = new Masks(image, 1, -1,this).getImg();


        updProg(-1);
        ImageController.writeImage(image, outputImageFilePath, extension);
        updProg(1);
        gc.updateImg(outputImageFilePath);
    }

    private BufferedImage executeMethod(BufferedImage image) {
        int filter = 0, dir = -1;
                if (gc.rbm1.isSelected()) filter = 1;
                else if (gc.rbm2.isSelected()) filter = 2;
                else if (gc.rbm3.isSelected()) filter = 3;
                else if (gc.rbm4.isSelected()) filter = 4;

                image = new Masks(image, filter, dir,this).getImg();
                return image;
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
            int particleSize = r.nextInt(max-min+1)+min;
            g.fillOval(r.nextInt(size)-particleSize/2, r.nextInt(size)-particleSize/2, particleSize, particleSize);
        }
        g.dispose();
        return img;
    }

    public void updProg(double value) {
        gc.updateProgress(value);
    }
}
