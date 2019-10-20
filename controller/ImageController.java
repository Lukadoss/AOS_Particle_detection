package controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Lukado on 23. 11. 2016.
 */
class ImageController {
    static BufferedImage readImage(String fileLocation) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(fileLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    static void writeImage(BufferedImage img, String fileLocation, String extension) {
        try {
            File outfile = new File(fileLocation);
            if (!outfile.exists()) outfile.mkdirs();
            ImageIO.write(img, extension, outfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
