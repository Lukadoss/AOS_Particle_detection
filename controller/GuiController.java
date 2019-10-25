package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Created by Lukado on 23. 11. 2016.
 */
public class GuiController {
    public BorderPane mainStage;
    public Button generate, solve, picBut;
    public ImageView ivIN;
    public Pane pnIN;
    public ProgressBar progB;
    public RadioButton rbm1, rbm2, rbm3, rbm4;
    public TextField tf1, tf2, tf3, tf4;
    public Label wi1, wi2, wi3, wi4, progT, resultLabel, picWarning;
    public BarChart hist;
    public GridPane grid;

    private Image img;
    private ParticleDetectionController edc;
    private String ext;
    private double aspectRatio = 1; // pouze ctvercove obrazky

    @FXML
    public void initialize() {
        if (ivIN != null) {
            ivIN.fitWidthProperty().bind(pnIN.widthProperty());
            ivIN.fitHeightProperty().bind(pnIN.heightProperty());
            listenResize();

            final ToggleGroup mgr = new ToggleGroup(); // mask group
            rbm1.setToggleGroup(mgr);
            rbm2.setToggleGroup(mgr);
            rbm3.setToggleGroup(mgr);
            rbm4.setToggleGroup(mgr);

            listenSelectedRadioItem(mgr);
        }
    }

    public void solve(ActionEvent actionEvent) {
        edc.executeMethod();
    }

    private boolean checkTFinput() {
        boolean inputs = true;
        wi1.setVisible(false);
        wi2.setVisible(false);
        wi3.setVisible(false);
        wi4.setVisible(false);

        if (!isParsable(tf1.getText())) {
            wi1.setVisible(true);
            inputs = false;
        } else{
            if (Integer.parseInt(tf1.getText())<=0){
                wi1.setVisible(true);
                inputs = false;
            }
        }

        if (!isParsable(tf2.getText())) {
            wi2.setVisible(true);
            inputs = false;
        } else{
            if (Integer.parseInt(tf2.getText())<=0){
                wi2.setVisible(true);
                inputs = false;
            }
        }

        if (!isParsable(tf3.getText())) {
            wi3.setVisible(true);
            inputs = false;
        } else{
            if (Integer.parseInt(tf3.getText())<=0){
                wi3.setVisible(true);
                inputs = false;
            }
        }

        if (!isParsable(tf4.getText())) {
            wi4.setText("Špatný vstup");
            wi4.setVisible(true);
            inputs = false;
        } else{
            if (Integer.parseInt(tf4.getText())<=0){
                wi4.setVisible(true);
                inputs = false;
            }
        }

        if (inputs && Integer.parseInt(tf3.getText()) > Integer.parseInt(tf4.getText())) {
            wi4.setText("Max > Min!");
            wi4.setVisible(true);
            inputs = false;
        }

        return inputs;
    }

    public void openFile(ActionEvent actionEvent) {
        Stage stage = (Stage) mainStage.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Picture loader");
        fileChooser.setInitialDirectory(
//                new File(System.getProperty("user.home"))
                new File(".")
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG, PNG, BMP", "*.jpg", "*.png", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            picWarning.setVisible(false);
            img = new Image(file.toURI().toString());

            ivIN.setImage(img);
            progB.setProgress(0);
            progT.setText("");

            aspectRatio = img.getWidth() / img.getHeight();
            fitImage(ivIN, pnIN, file.getPath());

            picBut.setText(file.getName());
            ext = picBut.getText().substring(picBut.getText().lastIndexOf(".") + 1);

            generate(new ActionEvent());
        }
    }

    public void about(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../about.fxml"));
            Stage stage = new Stage();
            stage.setTitle("O programu");
            stage.setScene(new Scene(root, 600, 200));
            stage.setMinWidth(600);
            stage.setMinHeight(150);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenResize() {
        resizeHW(pnIN, ivIN);
//        resizeHist(pnOUT, hist);
    }

    void updateImg(String s) {
        Platform.runLater(() -> {
            fitImage(ivIN, pnIN, s);
        });
    }

    void updateProgress(double val) {
        Platform.runLater(() -> {
            if (val == -1) {
                progT.setText("Načítání");
            } else {
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

    private void fitImage(ImageView iv, Pane pn, String s) {
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

    public void updateResult(double result) {
        Platform.runLater(() -> resultLabel.setText("Počet nalezených stop: " + result));
    }

    public void updateHist(XYChart.Series<String, Number> data) {
        Platform.runLater(() -> {
                    System.out.println(data.getData());
                    hist.getData().add(data);
                }
        );

    }

    private void listenSelectedRadioItem(ToggleGroup pgr) {
        pgr.selectedToggleProperty().addListener(observable -> {
            if (rbm2.isSelected()) {
                hist.setVisible(true);
                hist.setData(null);
            } else {
                hist.setVisible(false);
            }
        });
    }

    public void generate(ActionEvent actionEvent) {
        if (edc != null) {
            edc.stop();
        }

        if (actionEvent.getSource().getClass().equals(Button.class) && checkTFinput()) {
            aspectRatio = 1;
            edc = new ParticleDetectionController(this, Integer.parseInt(tf1.getText()), Integer.parseInt(tf2.getText()),
                    Integer.parseInt(tf3.getText()), Integer.parseInt(tf4.getText()));
            edc.start();
        } else {
            edc = new ParticleDetectionController(this, img, ext);
            edc.start();
        }
        solve.setDisable(false);
    }
}
