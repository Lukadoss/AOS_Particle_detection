package methods;

import controller.ImageController;
import controller.ParticleDetectionController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Stack;

public class DistanceField {
    private final BufferedImage img;
    private double result = 0;
    private ParticleDetectionController pdc;
    private int[][] field;
    private String extension = "png";
    private String dfImageFilePath = "img/distanceField." + extension;
    private double threshold;

    public DistanceField(BufferedImage img, ParticleDetectionController pdc, double threshold) {
        this.pdc = pdc;
        this.img = img;
        this.threshold = threshold;
    }

    public void calculateParticles(boolean dir) {
        boolean changed = true;
        field = new int[img.getWidth()][img.getHeight()];
        int level = 0;
        int maximum = 0;

        while (changed) {
            changed = false;
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    int val = img.getRGB(j, i);
                    if (val == Color.WHITE.getRGB() && field[i][j] == 0) {
                        field[i][j] = 1;
                        changed = true;
                    } else if (val == Color.WHITE.getRGB() && field[i][j] > 0) {
                        int tmp;

                        if (dir) {
                            tmp = check8d(i, j);
                        } else {
                            tmp = check4d(i, j);
                        }

                        if (tmp == level) {
                            field[i][j] += 1;
                            if (field[i][j]>maximum) maximum = field[i][j];
                            changed = true;
                        }
                    }
                }
                double prog = i / (double) img.getWidth();
                pdc.updProg(prog);
            }
//            if (level==0) level++;
//            else level*=2;
            level++;
        }

        BufferedImage image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        drawImage(image, maximum);
        if(threshold==0){
            pdc.getGc().tfd1.setText(""+maximum);
            threshold = maximum;
        }

        seedAlgorithm(image, maximum);
        pdc.updProg(-1);
        ImageController.writeImage(image, dfImageFilePath, extension);
        pdc.updProg(1);

//        printArray(field);
//        System.out.println("-----------------");
    }

    private void seedAlgorithm(BufferedImage img, int maximum) {
        Stack<Point> s = new Stack<>();
        int localMax = 0;
        ArrayList<Integer> dMaxLocal = new ArrayList<>();
        ArrayList<Point> dMax = new ArrayList<>();

        int[][] fcopy = new int[img.getWidth()][img.getHeight()];
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                fcopy[i][j] = field[i][j];
            }
        }

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int val = fcopy[j][i];
                if (val > 0) {
                    localMax = fcopy[j][i];
                    fcopy[j][i] = 0;

                    // 8okoli do zasobniku
                    seed8d(s, j, i);

                    while (!s.empty()) {
                        Point p = s.pop();
                        if (fcopy[p.x][p.y] == 0) continue;
                        if (fcopy[p.x][p.y]>localMax){
                            localMax = fcopy[p.x][p.y];
                        }
                        fcopy[p.x][p.y] = 0;
                        seed8d(s, p.x, p.y);
                    }
                    dMaxLocal.add(localMax);
                }
            }

            double prog = i / (double) img.getWidth();
            pdc.updProg(prog);
        }

        int x = 0;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int val = field[j][i];
                if (val > 0) {
                    field[j][i] = 0;

                    // 8okoli do zasobniku
                    seed8d(s, j, i);

                    while (!s.empty()) {
                        Point p = s.pop();
                        if (field[p.x][p.y] == 0) continue;
                        if (field[p.x][p.y] >= dMaxLocal.get(x)) field[p.x][p.y] = maximum;
                        if (threshold<=field[p.x][p.y]){
                            boolean hovno = true;
                            for (Point tmp: dMax){
                                double kek = Math.sqrt(Math.pow(p.x-tmp.x,2)+Math.pow(p.y-tmp.y,2));
                                if (kek<threshold){
                                    hovno = false;
                                    break;
                                }
                            }
                            if(hovno) {
                                dMax.add(p);
                            }
                        }
                        field[p.x][p.y] = 0;
                        seed8d(s, p.x, p.y);
                    }
                    x++;
                }
            }

            double prog = i / (double) img.getWidth();
            pdc.updProg(prog);
        }

        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setPaint(Color.RED);
        for (Point max : dMax) {
            g.drawOval((int) (max.y - threshold / 2), (int) (max.x - threshold / 2), (int) threshold, (int) threshold);
        }
        result = dMax.size();
    }

    private void seed8d(Stack<Point> s, int x, int y) {
        for (int i = -1; i <= 1; i++) {
            if ((x + i) < 0 || (x + i) > img.getHeight() - 1) continue;
            for (int j = -1; j <= 1; j++) {
                if ((y + j) < 0 || (y + j) > img.getWidth() - 1) continue;

                int val = field[x+i][y+j];
                if (!(i == 0 && j == 0) && val > 0) {
                    s.push(new Point(x + i, y + j));
                }
            }
        }
    }

    private void drawImage(BufferedImage image, int max) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        double pako = 255/(double)max;

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int val = (int)pako*field[i][j];
                image.setRGB(j, i, new Color(val, val, val).getRGB());
            }
        }
    }

    private int check4d(int x, int y) {
        int min = Integer.MAX_VALUE;
        if ((x - 1) > 0){
            if (field[x-1][y]<min) min=field[x-1][y];
        }
        if ((x + 1) < img.getWidth() - 1) {
            if (field[x+1][y]<min) min=field[x+1][y];
        }
        if ((y - 1) > 0){
            if (field[x][y-1]<min) min=field[x][y-1];
        }
        if ((y + 1) < img.getHeight() - 1) {
            if (field[x][y+1]<min) min=field[x][y+1];
        }

        return min;
    }

    private int check8d(int x, int y) {
        int min = Integer.MAX_VALUE;

        for (int i = -1; i <= 1; i++) {
            if ((x + i) < 0 || (x + i) > img.getWidth() - 1) continue;
            for (int j = -1; j <= 1; j++) {
                if ((y + j) < 0 || (y + j) > img.getHeight() - 1) continue;

                int val = field[x+i][y+j];
                if (!(i == 0 && j == 0) && val < min) {
                    min = val;
                }
            }
        }

        return min;
    }

    private void printArray(int[][] array) {
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }

    public double getResult() {
        return result;
    }

    public String getDFImage() {
        return dfImageFilePath;
    }
}
