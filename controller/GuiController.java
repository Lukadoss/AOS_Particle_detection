package controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    public RadioButton rbm1, rbm2, rbm3, rbm4, rbp1, rbp2, rbp3, rbh1, rbh2, rbd1, rbd2;
    public TextField tf1, tf2, tf3, tf4;
    public Label wi1, wi2, wi3, wi4, progT, resultLabel, picWarning;
    public BarChart<Number, String> histogram;
    public GridPane grid;
    public VBox simpleVB, histVB, ringVB, distanceVB;
    public Slider histSlider;

    public boolean debugMode = true;
    public CheckBox debugCheckBox;
    private ParticleDetectionController pdc;
    private String ext, filePath;
    private double aspectRatio = 1; // pouze ctvercove obrazky

    @FXML
    public void initialize() {
        if (ivIN != null) {
            ivIN.fitWidthProperty().bind(pnIN.widthProperty());
            ivIN.fitHeightProperty().bind(pnIN.heightProperty());
            listenResize();

            initRadioButtons();
            initHistogram();
        }
    }

    private void initHistogram() {
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        xAxis.setLabel("Četnost obsahů (N)");
        yAxis.setLabel("Obsahy (S)");

        histVB.setDisable(true);
        histogram = new BarChart<>(xAxis, yAxis);
        histogram.setPrefSize(200, Region.USE_COMPUTED_SIZE);
        histogram.setLayoutY(60);
        histogram.setLegendVisible(false);
        histogram.setTitle("Četnost víceexpozic");
        histogram.setStyle("-fx-font-family: 'Noto Sans'");
        histogram.setAnimated(false);
        histVB.getChildren().add(1, histogram);

        histSlider.valueProperty().addListener((obs, oldval, newVal) ->
        {
            if (!histSlider.isValueChanging()) {
                if (newVal.intValue() == 0) histSlider.setValue(oldval.doubleValue());
                pdc.executeMethod();
            }
        });

        final double SCALE_DELTA = 1.1;
        histogram.setOnScroll(event -> {
            event.consume();

            if (event.getDeltaY() == 0) {
                return;
            }

            double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(xAxis.getUpperBound() * scaleFactor);
            xAxis.setTickUnit(xAxis.getUpperBound() / 20);
//                histogramChart.setScaleY(histogramChart.getScaleY() * scaleFactor);

        });

        histogram.setOnMousePressed(event -> {
            if (event.getClickCount() == 2) {
                xAxis.setAutoRanging(true);
//                    histogramChart.setScaleX(1.0);
            }
        });

        histogram.setOnKeyPressed(event->{
            System.out.println(event.getCode());
        });
    }

    private void initRadioButtons() {
        final ToggleGroup mgr = new ToggleGroup(); // mask group
        rbm1.setToggleGroup(mgr);
        rbm2.setToggleGroup(mgr);
        rbm3.setToggleGroup(mgr);
        rbm4.setToggleGroup(mgr);

        listenSelectedMethod(mgr);

        final ToggleGroup pgr = new ToggleGroup(); // simple method group
        rbp1.setToggleGroup(pgr);
        rbp2.setToggleGroup(pgr);
        rbp3.setToggleGroup(pgr);

        listenSelectedSettings(pgr);

        final ToggleGroup pgh = new ToggleGroup(); // hist method group
        rbh1.setToggleGroup(pgh);
        rbh2.setToggleGroup(pgh);

        listenSelectedSettings(pgh);

        final ToggleGroup pgd = new ToggleGroup(); // hist method group
        rbd1.setToggleGroup(pgd);
        rbd2.setToggleGroup(pgd);

        listenSelectedSettings(pgd);
    }

    public void solve(ActionEvent actionEvent) {
        pdc.executeMethod();
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
        } else {
            if (Integer.parseInt(tf1.getText()) <= 0) {
                wi1.setVisible(true);
                inputs = false;
            }
        }

        if (!isParsable(tf2.getText())) {
            wi2.setVisible(true);
            inputs = false;
        } else {
            if (Integer.parseInt(tf2.getText()) <= 0) {
                wi2.setVisible(true);
                inputs = false;
            }
        }

        if (!isParsable(tf3.getText())) {
            wi3.setVisible(true);
            inputs = false;
        } else {
            if (Integer.parseInt(tf3.getText()) <= 0) {
                wi3.setVisible(true);
                inputs = false;
            }
        }

        if (!isParsable(tf4.getText())) {
            wi4.setText("Špatný vstup");
            wi4.setVisible(true);
            inputs = false;
        } else {
            if (Integer.parseInt(tf4.getText()) <= 0) {
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
            Image img = new Image(file.toURI().toString());
            filePath = file.getPath();

            ivIN.setImage(img);
            progB.setProgress(0);
            progT.setText("");

            aspectRatio = img.getWidth() / img.getHeight();
            fitImage(ivIN, pnIN, filePath);

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

            if (rbh2.isSelected() && rbm2.isSelected()) {
                histogram.setPrefWidth(mainStage.getWidth() * 0.5);
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

    public void updateResult(String result) {
        Platform.runLater(() -> resultLabel.setText(result));
    }

    public void updateHist(XYChart.Series<Number, String> data) {
        Platform.runLater(() -> {
            boolean flag = true;
            histVB.setDisable(false);
            histogram.setData(FXCollections.observableArrayList(data)) ;

            for (XYChart.Series<Number, String> serie : histogram.getData()) {
                for (XYChart.Data<Number, String> item : serie.getData()) {
                    if (item.getXValue().intValue()==pdc.getSH().getRefValue() && flag){
                        item.getNode().setStyle("-fx-bar-fill: green");
                        flag = false;
                    }
                    item.getNode().setOnMousePressed((MouseEvent event) -> {
                        for (XYChart.Series<Number, String> s : histogram.getData()) {
                            for (XYChart.Data<Number, String> i : serie.getData()) {
                                i.getNode().setStyle("-fx-bar-fill: red;");
                            }
                        }
                        item.getNode().setStyle("-fx-bar-fill: green");
                        pdc.getSH().setRefValue(item);
                        pdc.getSH().calculateExpo();
                        updateHist(pdc.getSH().getHist());
                        updateResult("Počet nalezených stop: "+pdc.getSH().getResult());
                    });
                }
            }
        });
    }

    private void listenSelectedMethod(ToggleGroup tg) {
        tg.selectedToggleProperty().addListener(observable -> {
            simpleVB.setVisible(false);
            histVB.setVisible(false);
            ringVB.setVisible(false);
            distanceVB.setVisible(false);
            animateHist(200);
            rbh1.setSelected(true);

            if (ivIN.getImage() != null) pdc.executeMethod();

            switch (((RadioButton) tg.getSelectedToggle()).getId()) {
                case "rbm1":
                    simpleVB.setVisible(true);
                    break;
                case "rbm2":
                    histVB.setVisible(true);
                    break;
                case "rbm3":
                    ringVB.setVisible(true);
                    break;
                case "rbm4":
                    distanceVB.setVisible(true);
            }
        });
    }

    private void listenSelectedSettings(ToggleGroup tg) {
        tg.selectedToggleProperty().addListener(observable -> {
            switch (((RadioButton) tg.getSelectedToggle()).getId()) {
                case "rbp1":
                case "rbp2":
                case "rbp3":
                case "rbd1":
                case "rbd2":
                    if (ivIN.getImage() != null) pdc.executeMethod();
                    break;
                case "rbh1":
                    animateHist(200);
                    break;
                case "rbh2":
                    animateHist(mainStage.getWidth() * 0.5);
                    break;
            }
        });
    }

    private void animateHist(double width) {
        final Duration cycleDuration = Duration.millis(500);
        final Timeline timeline;
        timeline = new Timeline(
                new KeyFrame(cycleDuration,
                        new KeyValue(histogram.prefWidthProperty(), width, Interpolator.EASE_BOTH))
        );

        timeline.play();
    }

    public void generate(ActionEvent actionEvent) {
        if (pdc != null) {
            pdc.stop();
        }

        if (actionEvent.getSource().getClass().equals(Button.class) && checkTFinput()) {
            aspectRatio = 1;
            pdc = new ParticleDetectionController(this, Integer.parseInt(tf1.getText()), Integer.parseInt(tf2.getText()),
                    Integer.parseInt(tf3.getText()), Integer.parseInt(tf4.getText()));
            pdc.start();
        } else {
            pdc = new ParticleDetectionController(this, filePath, ext);
            pdc.start();
        }
        solve.setDisable(false);
    }

    public void setDebug(ActionEvent actionEvent) {
        if (debugCheckBox.isSelected()){
            debugMode = true;
        }else{
            debugMode = false;
        }
    }
}
