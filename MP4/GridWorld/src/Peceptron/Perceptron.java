package Peceptron;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by manshu on 5/1/15.
 */

class Image {
    int[] image_pixel;
    public Image(int[] image_pixel) {
        this.image_pixel = image_pixel;
    }
}

public class Perceptron {
    private final int ROW = 28;
    private final int COLUMN = 28;
    private final int TRAINIMAGES = 5000; // set low to debug
    private final int TESTIMAGES = 1000; // set low to debug
    private final boolean BIAS = true;
    private final boolean CYCLE_DATA = true;

    private final int CLASS_SIZE = 10;

    private Image[] images;
    private int[] training_labels;

    private Image[] test_images;
    private int[] test_labels;

    private double[][] weight_per_class;

    private void initWeights(boolean byRandom) {
        for (int i = 0; i < CLASS_SIZE; i++) {
            if (BIAS)
                for (int r = 0; r < ROW * COLUMN + 1; r++) {
                    if (byRandom) {
                        weight_per_class[i][r] = Math.random();
                    }
                    else {weight_per_class[i][r] = 0.0;}
                }
            else
                for (int r = 0; r < ROW * COLUMN; r++) {
                    if (byRandom) {
                        weight_per_class[i][r] = Math.random();
                    }
                    else {weight_per_class[i][r] = 0.0;}
                }
        }
    }

    private void readTrainingFile(String imagesFile, String labelsFile) throws IOException {
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(imagesFile));
        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(labelsFile));

        training_labels = new int[TRAINIMAGES];
        images = new Image[TRAINIMAGES];

        for (int i = 0; i < TRAINIMAGES; i++) {
            training_labels[i] = Integer.parseInt(bufferedReader2.readLine());

            int[] pixel_data;
            if (BIAS) {
                pixel_data = new int[ROW * COLUMN + 1];
                pixel_data[0] = 1;
            } else {
                pixel_data = new int[ROW * COLUMN];
            }
            pixel_data[0] = 1;
            for (int r = 0; r < ROW; r++) {
                String row = bufferedReader1.readLine();
                for (int c = 0; c < row.length(); c++) {
                    if (row.charAt(c) != ' ') {
                        if (BIAS)
                            pixel_data[r * ROW + c + 1] = -1;
                        else
                            pixel_data[r * ROW + c] = -1;
                    }
                    else {
                        if (BIAS)
                            pixel_data[r * ROW + c + 1] = 1;
                        else
                            pixel_data[r * ROW + c] = 1;
                    }
                }
            }
            images[i] = new Image(pixel_data);
        }
        if (BIAS)
            weight_per_class = new double[CLASS_SIZE][ROW * COLUMN + 1];
        else
            weight_per_class = new double[CLASS_SIZE][ROW * COLUMN];
        initWeights(true);
    }

    private void readTestFile(String imagesFile, String labelsFile) throws IOException {
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(imagesFile));
        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(labelsFile));

        test_labels = new int[TESTIMAGES];
        test_images = new Image[TESTIMAGES];

        for (int i = 0; i < TESTIMAGES; i++) {
            test_labels[i] = Integer.parseInt(bufferedReader2.readLine());

            int[] pixel_data;
            if (BIAS) {
                pixel_data = new int[ROW * COLUMN + 1];
                pixel_data[0] = 1;
            } else {
                pixel_data = new int[ROW * COLUMN];
            }
            for (int r = 0; r < ROW; r++) {
                String row = bufferedReader1.readLine();
                for (int c = 0; c < row.length(); c++) {
                    if (row.charAt(c) != ' ') {
                        if (BIAS)
                            pixel_data[r * ROW + c + 1] = -1;
                        else
                            pixel_data[r * ROW + c] = -1;
                    }
                    else {
                        if (BIAS)
                            pixel_data[r * ROW + c + 1] = 1;
                        else
                            pixel_data[r * ROW + c] = 1;
                    }
                }
            }
            test_images[i] = new Image(pixel_data);
        }
    }

    private int getMaxClass(Image image) {
        int maxClass = 0;
        int maxCValue = Integer.MIN_VALUE;
        for (int c = 0; c < CLASS_SIZE; c++) {
            int dotProduct = 0;
            if (BIAS)
                for (int r = 0; r < ROW * COLUMN + 1; r++) {
                    dotProduct += weight_per_class[c][r] * image.image_pixel[r];
                }
            else {
                for (int r = 0; r < ROW * COLUMN; r++) {
                    dotProduct += weight_per_class[c][r] * image.image_pixel[r];
                }
            }

            if (dotProduct > maxCValue) {
                maxClass = c;
                maxCValue = dotProduct;
            }
        }
        return maxClass;
    }

    private double getAlpha(int timeStep) {
        return 1000.0 / (1000.0 + timeStep);
    }

    public void runPerceptron() {
        int timeStep = 0;
        int numMisMatched;
        do {
            numMisMatched = 0;
            int[] train_index = new int[TRAINIMAGES];
            for (int i = 0; i < TRAINIMAGES; i++) {
                train_index[i] = i;
            }
            if (CYCLE_DATA) {
                Random rand = new Random();
                for (int i = TRAINIMAGES - 1; i > 0; i--) {
                    int n = rand.nextInt(i + 1);
                    train_index[i] = train_index[i] ^ train_index[n];
                    train_index[n] = train_index[i] ^ train_index[n];
                    train_index[i] = train_index[n] ^ train_index[i];
                }
            }
            for (int x = 0; x < TRAINIMAGES; x++) {
                int t = train_index[x];
                int actual_label = training_labels[t];
                int predicted_label = getMaxClass(images[t]);

                if (predicted_label != actual_label) {
                    if (BIAS)
                        for (int r = 0; r < ROW * COLUMN + 1; r++) {
                            weight_per_class[actual_label][r] += getAlpha(timeStep) * images[t].image_pixel[r];
                            weight_per_class[predicted_label][r] -= getAlpha(timeStep) * images[t].image_pixel[r];
                        }
                    else
                        for (int r = 0; r < ROW * COLUMN; r++) {
                            weight_per_class[actual_label][r] += getAlpha(timeStep) * images[t].image_pixel[r];
                            weight_per_class[predicted_label][r] -= getAlpha(timeStep) * images[t].image_pixel[r];
                        }
                    numMisMatched++;
                }
            }
            timeStep++;
            System.out.println("At timestep "+ timeStep + " num mismatched = " + numMisMatched);
        } while (numMisMatched != 0);
        System.out.println(timeStep);
    }

    public void testPerceptron() {
        int numMisMatched = 0;
        for (int t = 0; t < TESTIMAGES; t++) {
            int actual_label = test_labels[t];
            int predicted_label = getMaxClass(test_images[t]);

            if (predicted_label != actual_label) {
                numMisMatched++;
            }
        }
        System.out.println("At test num mismatched " + numMisMatched);
        System.out.println("perceptron accuracy " + Double.toString(100.0 - (numMisMatched * 100.0/TESTIMAGES)));
    }

    public static void main(String[] args) throws IOException {
        String trainImages = "/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/digitdata/trainingimages";
        String trainLabels = "/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/digitdata/traininglabels";
        String testImages = "/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/digitdata/testimages";
        String testLabels = "/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/digitdata/testlabels";

        Perceptron perceptron = new Perceptron();
        perceptron.readTrainingFile(trainImages, trainLabels);
        perceptron.readTestFile(testImages, testLabels);
        perceptron.runPerceptron();
        perceptron.testPerceptron();

    }
}
