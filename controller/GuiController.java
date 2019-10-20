package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Lukado on 23. 11. 2016.
 */
public class GuiController {
    public BorderPane mainStage;
    public Button solve;
    public ImageView ivIN, ivOUT;
    public Pane pnIN, pnOUT;
    public ProgressBar progB;
    public RadioButton rbm1, rbm2, rbm3, rbm4;
    public TextField tf1, tf2, tf3, tf4;
    public Label wi1, wi2, wi3, wi4, progT;

    private double aspectRatio = 1; // pouze ctvercove obrazky

    public String filePath;

    @FXML
    public void initialize() {
        if(ivIN !=null && ivOUT != null) {
            ivIN.fitWidthProperty().bind(pnIN.widthProperty());
            ivIN.fitHeightProperty().bind(pnIN.heightProperty());
            ivOUT.fitWidthProperty().bind(pnOUT.widthProperty());
            ivOUT.fitHeightProperty().bind(pnOUT.heightProperty());
            listenResize();

            final ToggleGroup mgr = new ToggleGroup(); // mask group
            rbm1.setToggleGroup(mgr);
            rbm2.setToggleGroup(mgr);
            rbm3.setToggleGroup(mgr);
            rbm4.setToggleGroup(mgr);
        }
    }

    public void solve(ActionEvent actionEvent) {
        if (checkTFinput()){
            Thread edc = new ParticleDetectionController(this, Integer.parseInt(tf1.getText()), Integer.parseInt(tf2.getText()),
                    Integer.parseInt(tf3.getText()), Integer.parseInt(tf4.getText()));
            edc.start();
        }
    }

    private boolean checkTFinput() {
        boolean inputs = true;
        wi1.setVisible(false);
        wi2.setVisible(false);
        wi3.setVisible(false);
        wi4.setVisible(false);

        if (!isParsable(tf1.getText())){
            wi1.setVisible(true);
            inputs = false;
        }
        if(!isParsable(tf2.getText())){
            wi2.setVisible(true);
            inputs = false;
        }
        if(!isParsable(tf3.getText())){
            wi3.setVisible(true);
            inputs = false;
        }
        if(!isParsable(tf4.getText())){
            wi4.setVisible(true);
            inputs = false;
        }

        return inputs;
    }

    public void about(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../about.fxml"));
            Stage stage = new Stage();
            stage.setTitle("O programu");
            stage.setScene(new Scene(root, 350, 125));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenResize() {
        resizeHW(pnIN, ivIN);
        resizeHW(pnOUT, ivOUT);
    }

    void updateImg(String s) {
        Platform.runLater(() -> {
            if (s.contains("gray")) {
                fitImage(ivIN, pnIN, s);
            }else{
                fitImage(ivOUT, pnOUT, s);
            }
        });
    }

    void updateProgress(double val){
        Platform.runLater(() -> {
            if (val == -1) {
                progT.setText("Načítání");
            }else {
                progB.setProgress(val);
                progT.setText(Math.round(val * 100) + " %");
            }
        });
    }

    @SuppressWarnings("Duplicates")
    private void resizeHW(Pane tmpPN, ImageView tmpIV) {
        tmpPN.heightProperty().addListener(observable -> {
            double imgWidth = Math.min(tmpIV.getFitWidth(), tmpIV.getFitHeight() * aspectRatio);
            double imgHeight = Math.min(tmpIV.getFitHeight(), tmpIV.getFitWidth() / aspectRatio);

            if (tmpIV.getImage() != null) {
                tmpIV.setX(tmpPN.getWidth() / 2 - imgWidth / 2);
                tmpIV.setY(tmpPN.getHeight() / 2 - imgHeight / 2);
            }

        });

        tmpPN.widthProperty().addListener(observable -> {
            double imgWidth = Math.min(tmpIV.getFitWidth(), tmpIV.getFitHeight() * aspectRatio);
            double imgHeight = Math.min(tmpIV.getFitHeight(), tmpIV.getFitWidth() / aspectRatio);
            if (tmpIV.getImage() != null) {
                tmpIV.setX(tmpPN.getWidth() / 2 - imgWidth / 2);
                tmpIV.setY(tmpPN.getHeight() / 2 - imgHeight / 2);
            }

        });
    }

    private void fitImage(ImageView iv, Pane pn, String s){
        double imgHeight = Math.min(iv.getFitHeight(), iv.getFitWidth() / aspectRatio);
        double imgWidth = Math.min(iv.getFitWidth(), iv.getFitHeight() * aspectRatio);

        iv.setImage(new Image("file:" + s));
        iv.setX(pn.getWidth() / 2 - imgWidth / 2);
        iv.setY(pn.getHeight() / 2 - imgHeight / 2);
    }

    private static boolean isParsable(String input) {
        boolean parsable = true;
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            parsable = false;
        }
        return parsable;
    }
}
