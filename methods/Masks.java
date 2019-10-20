package methods;

import controller.ParticleDetectionController;

import java.awt.image.*;

public class Masks extends EdgeMath {
    private BufferedImage output_img;
    private int[][] conv1, conv2;

    public Masks(BufferedImage img, int filter, int dir, ParticleDetectionController edc) {
        int width = img.getWidth();
        int height = img.getHeight();
        output_img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        switch (filter){
            case 1:
                prewitt(dir);
                break;
            case 2:
                sobel(dir);
                break;
            case 3:
                robinson(dir);
                break;
            case 4:
                kirsch(dir);
        }

        for (int x = 1; x < width-1; x++) {
            for (int y = 1; y < height-1; y++) {

                // ziskej okoli
                int[][] gray = new int[3][3];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        gray[i][j] = img.getRGB(x-1+i,y-1+j) & 0xFF;
                    }
                }

                // aplikuj filtr
                int gray1 = 0, gray2 = 0;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (dir != -1) gray1 += gray[i][j] * conv1[i][j];
                        else {
                            gray1 += gray[i][j] * conv1[i][j];
                            gray2 += gray[i][j] * conv2[i][j];
                        }
                    }
                }
                int magnitude;
                if (dir!=-1) magnitude = truncate(Math.abs(gray1));
                else magnitude = truncate((int) Math.sqrt(gray1*gray1 + gray2*gray2));
                int gs = (magnitude<<16)+(magnitude<<8)+magnitude;
                output_img.setRGB(x, y, gs);
            }
            double progress = x/(double)width;
            edc.updProg(progress);
        }
    }
    public BufferedImage getImg(){
        return output_img;
    }

    private void prewitt(int dir){
        if (dir!=-1) {
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-1, 0, 1},
                    {-1, 0, 1}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else{
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-1, 0, 1},
                    {-1, 0, 1}
            };
            conv2 = new int[][]{
                    {1, 1, 1},
                    {0, 0, 0},
                    {-1, -1, -1}
            };
        }
    }

    private void sobel(int dir){
        if (dir!=-1){
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-2, 0, 2},
                    {-1, 0, 1}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else {
            conv1 = new int[][]{
                    {-1, 0, 1},
                    {-2, 0, 2},
                    {-1, 0, 1}
            };
            conv2 = new int[][]{
                    {1, 2, 1},
                    {0, 0, 0},
                    {-1, -2, -1}
            };
        }
    }

    private void robinson(int dir){
        if (dir!=-1){
            conv1 = new int[][]{
                    {-1, 1, 1},
                    {-1, -2, 1},
                    {-1, 1, 1}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else{
            conv1 = new int[][]{
                    {-1, 1, 1},
                    {-1, -2, 1},
                    {-1, 1, 1}
            };
            conv2 = new int[][]{
                    {1, 1, 1},
                    {1, -2, 1},
                    {-1, -1, -1}
            };
        }
    }

    private void kirsch(int dir){
        if (dir!=-1){
            conv1 = new int[][]{
                    {-5, 3, 3},
                    {-5, 0, 3},
                    {-5, 3, 3}
            };
            conv1 = rotateConv(conv1, dir);
        }
        else {
            conv1 = new int[][]{
                    {-5, 3, 3},
                    {-5, 0, 3},
                    {-5, 3, 3}
            };
            conv2 = new int[][]{
                    {3, 3, 3},
                    {3, 0, 3},
                    {-5, -5, -5}
            };
        }
    }


}
