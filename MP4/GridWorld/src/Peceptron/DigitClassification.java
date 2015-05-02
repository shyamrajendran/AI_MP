package Peceptron;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
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

public class DigitClassification {
    private final int ROW = 28;
    private final int COLUMN = 28;
    private final int TRAINIMAGES = 5000; // set low to debug
    private final int TESTIMAGES = 1000; // set low to debug
    private final boolean BIAS = false;
    private final boolean CYCLE_DATA = false;
    private final boolean RANDOM_INITIALIZATION = true;
    private static final int MAX_EPOCH = 500;
    private final int CLASS_SIZE = 10;
    private static double[][] confusionMatrix;
    private boolean confusion = false;
    private HashMap<Integer, Integer> perClassTotal = new HashMap<Integer, Integer>();

    private Image[] images;
    private int[] training_labels;

    private Image[] test_images;
    private int[] test_labels;

    private double[][] weight_per_class;

    DigitClassification() {
        confusionMatrix = new double[CLASS_SIZE][CLASS_SIZE];
        int a = 0;
    }

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
        initWeights(RANDOM_INITIALIZATION);
    }

    private void readTestFile(String imagesFile, String labelsFile) throws IOException {
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(imagesFile));
        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(labelsFile));

        test_labels = new int[TESTIMAGES];
        test_images = new Image[TESTIMAGES];

        for (int i = 0; i < TESTIMAGES; i++) {
            test_labels[i] = Integer.parseInt(bufferedReader2.readLine());

            Integer key = test_labels[i];
            if (!perClassTotal.containsKey(key)) {
                perClassTotal.put(key, 1);
            } else {
                Integer val = perClassTotal.get(key);
                val++;
                perClassTotal.put(key, val);
            }

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

    public int runPerceptron(int epochs) {
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
            //System.out.println("At timestep "+ timeStep + " num mismatched = " + numMisMatched);
        } while (timeStep < epochs && numMisMatched != 0);
        System.out.println(timeStep);
        return timeStep;
    }

    public double testPerceptron(boolean confusion) {
        int numMisMatched = 0;
        for (int t = 0; t < TESTIMAGES; t++) {
            int actual_label = test_labels[t];
            int predicted_label = getMaxClass(test_images[t]);

            if (confusion) {
                confusionMatrix[actual_label][predicted_label]++;
            }
            if (predicted_label != actual_label) {
                numMisMatched++;
            }
        }
        System.out.println("At test num mismatched " + numMisMatched);
        double accuracy = 100.0 - (numMisMatched * 100.0/TESTIMAGES);
        System.out.println("perceptron accuracy " + Double.toString(accuracy));
        return accuracy;
    }

    private double getDistance(Image image1, Image image2) {
        double dist = 0.0;
        double normal = 0.0;
        for (int i = 0; i < ROW * COLUMN; i++) {
            int p1 = image1.image_pixel[i];
            int p2 = image2.image_pixel[i];
            dist += (p1 - p2) * (p1 - p2);
            normal += p2 * p2;
        }
        return Math.sqrt(dist) / (Math.sqrt(normal));
    }

    class QueueItem {
        Double distance;
        Integer label;
        QueueItem(double distance, int label) {
            this.distance = distance;
            this.label = label;
        }
    }

    public void runKNN(int k) {
        PriorityQueue<QueueItem> priorityQueue = new PriorityQueue<>(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                QueueItem q1 = (QueueItem) o1;
                QueueItem q2 = (QueueItem) o2;
                if (q1.distance > q2.distance) return -1;
                else if (q1.distance < q2.distance) return 1;
                return 0;
            }
        });

        int num_mismatch = 0;
        for (int i = 0; i < TESTIMAGES; i++) {
            int original_label = test_labels[i];
            for (int t = 0; t < TRAINIMAGES; t++) {
                double distance = getDistance(test_images[i], images[t]);
                int class_label = training_labels[t];
                if (priorityQueue.size() < k) {
                    priorityQueue.add(new QueueItem(distance, class_label));
                } else {
                    if (priorityQueue.peek().distance > distance) {
                        priorityQueue.poll();
                        priorityQueue.add(new QueueItem(distance, class_label));
                    }
                }
            }
            int[] label_count = new int[CLASS_SIZE];
            int max_label = 0;
            int max_count = 0;
            int queue_size = priorityQueue.size();
            for (int p = 0; p < queue_size; p++) {
                QueueItem queueItem = priorityQueue.poll();
                label_count[queueItem.label]++;
            }

            for (int l = 0; l < CLASS_SIZE; l++) {
                if (label_count[l] > max_count) {
                    max_label = l;
                    max_count = label_count[l];
                }
            }
            if (original_label != max_label) num_mismatch++;
        }
        double percentage_accuracy = 100.0 - (100.0 * num_mismatch) / TESTIMAGES;
        System.out.println(k + ", " + percentage_accuracy);
    }

    double sigmoid(double x) {
        return 1.0 / (1.0 + Math.pow(Math.E, -1 * x));
    }

    public void runPerceptronGradientDescent() {
        int timeStep = 0;
        int numMisMatched = 0;
        do {
            numMisMatched = 0;
            for (int t = 0; t < TRAINIMAGES; t++) {
                int actual_label = training_labels[t];
                int predicted_label = getMaxClass(images[t]);
                //w ← w +α(y － f (x))σ (w ・x)(1－σ (w ・x))x
                if (actual_label == predicted_label) continue;
                numMisMatched++;

                double dotproduct = 0.0;
                for (int r = 0; r < ROW * COLUMN; r++) {
                    dotproduct += weight_per_class[actual_label][r] * images[t].image_pixel[r];
                }
                for (int r = 0; r < ROW * COLUMN; r++) {
                    weight_per_class[actual_label][r] +=
                            getAlpha(timeStep) * images[t].image_pixel[r] * sigmoid(dotproduct) *
                                    (1 - sigmoid(dotproduct));
                }
                System.out.println("Dot product = " + dotproduct + " sigmoid = " + sigmoid(dotproduct));
                dotproduct = 0.0;
                for (int r = 0; r < ROW * COLUMN; r++) {
                    dotproduct += weight_per_class[predicted_label][r] * images[t].image_pixel[r];
                }
                System.out.println("Dot product = " + dotproduct + " sigmoid = " + sigmoid(dotproduct));
                for (int r = 0; r < ROW * COLUMN; r++) {
                    weight_per_class[predicted_label][r] -=
                            getAlpha(timeStep) * images[t].image_pixel[r] * sigmoid(dotproduct) *
                                    (1 - sigmoid(dotproduct));
                }
            }
            timeStep++;
            System.out.println("TimeStep = " + timeStep + " Mismatch = " + numMisMatched);
        } while (numMisMatched != 0);
    }

    public void generateEpochAccuracyData() {
        PrintWriter bw = null;
        try {
            bw = new PrintWriter(new FileWriter("train_epoch_accuracy"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < 100; i++) {
            initWeights(false);
            int iter = runPerceptron(i);
            double accuracy = testPerceptron(false);
            bw.write(Integer.toString(iter));
            bw.write(",");
            bw.write(Double.toString(accuracy));
            bw.write("\n");
        }
        bw.close();

    }

    private void printConfusionMatrix(Boolean debug) throws IOException {
        Double accuracy;
        Double t ;
        accuracy=0.0;
        if (debug) {
            System.out.println("\n\n*** CONFUSION MATRIX ***\n");
        }
        for (int i = 0 ;i < CLASS_SIZE; i++){
            for (int j = 0 ; j < CLASS_SIZE ; j++){
                t = confusionMatrix[i][j]/perClassTotal.get(i)*100;
                if (debug){
                    System.out.format("%10.3f", t);
                    System.out.print("%");
                }
                if ( i == j) {
                    accuracy+=t;
                }
            }
            if (debug){
                System.out.println();
            }
        }
        double a = accuracy/CLASS_SIZE;

        if (debug) {
            System.out.println("--------------------------");
            System.out.print("OVERALL ACCURACY :");
            System.out.format("%5.3f", a);
            System.out.println("%");
            System.out.println("---------------------------");
        }


    }
    public static void main(String[] args) throws IOException {
        String trainImages = "/Users/saikat/Documents/UIUC/spring_2015/cs440/mp/AI_MP/MP4/GridWorld/digitdata/trainingimages";
        String trainLabels = "/Users/saikat/Documents/UIUC/spring_2015/cs440/mp/AI_MP/MP4/GridWorld/digitdata/traininglabels";
        String testImages = "/Users/saikat/Documents/UIUC/spring_2015/cs440/mp/AI_MP/MP4/GridWorld/digitdata/testimages";
        String testLabels = "/Users/saikat/Documents/UIUC/spring_2015/cs440/mp/AI_MP/MP4/GridWorld/digitdata/testlabels";

        DigitClassification digitClassification = new DigitClassification();
        digitClassification.readTrainingFile(trainImages, trainLabels);
        digitClassification.readTestFile(testImages, testLabels);
        //digitClassification.runPerceptronGradientDescent();
        //digitClassification.testPerceptron();

        //digitClassification.generateEpochAccuracyData();
        digitClassification.runPerceptron(500);
        digitClassification.testPerceptron(true);
        digitClassification.printConfusionMatrix(true);
//        digitClassification.runPerceptron(MAX_EPOCH);
//        for (int runs = 1; runs < 100; runs++)
//            digitClassification.runKNN(runs);


    }
}
