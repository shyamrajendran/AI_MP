package Peceptron;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by manshu on 5/1/15.
 */

class Feature {
    int[] word_freq;

    public Feature(int[] word_freq) {
        this.word_freq = word_freq;
    }
}

public class TextClassification {
    private final int TRAINING_SIZE = 1900;
    private final int TEST_SIZE = 263;
    private final boolean BIAS = false;
    private final boolean CYCLE_DATA = true;
    private final boolean RANDOM_INITIALIZATION = true;
    private final int MAX_EPOCH = 100;
    private double[][] confusionMatrix;
    private final int CLASS_SIZE = 8;

    private double[][] weight_per_class;
    private Map<String, Integer> reverseWordMap;
    int[] training_labels;
    int[] test_labels;
    Feature[] featureVector;
    Feature[] testFeatureVector;

    private HashMap<Integer, Integer> perClassTotal = new HashMap<Integer, Integer>();

    String[] dictionary;

    TextClassification() {
        confusionMatrix = new double[CLASS_SIZE][CLASS_SIZE];
    }

    private void initWeights(boolean byRandom) {
        for (int i = 0; i < CLASS_SIZE; i++) {
            if (BIAS) {
                for (int r = 0; r < dictionary.length + 1; r++) {
                    if (byRandom) {
                        weight_per_class[i][r] = Math.random();
                    }
                    else {weight_per_class[i][r] = 0.0;}
                }
            } else {
                for (int r = 0; r < dictionary.length; r++) {
                    if (byRandom) {
                        weight_per_class[i][r] = Math.random();
                    }
                    else {weight_per_class[i][r] = 0.0;}
                }
            }
        }
    }

    public void readTrainingFile(String file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        training_labels = new int[TRAINING_SIZE];
        featureVector = new Feature[TRAINING_SIZE];

        String line;
        int line_num = 0;
        List<String> lines = new ArrayList<>();
        Set<String> wordDictionary = new HashSet<>();
        while ((line = bufferedReader.readLine()) != null) {
            int index = 0;
            while (line.charAt(index++) != ' ');

            training_labels[line_num] = Integer.parseInt(line.substring(0, index - 1));
            line = line.substring(index);
            lines.add(line);
            String[] word_nums = line.split(" ");
            for (String word_num : word_nums) {
                String word = word_num.split(":")[0];
                wordDictionary.add(word);
            }
            line_num++;
        }

        reverseWordMap = new HashMap<>();
        dictionary = new String[wordDictionary.size()];
        int word_num = 0;
        for (String word : wordDictionary) {
            reverseWordMap.put(word, word_num);
            dictionary[word_num++] = word;
        }

        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
            int[] word_freq;
            if (BIAS) {
                word_freq = new int[dictionary.length + 1];
                word_freq[0] = 1;
            } else {
                word_freq = new int[dictionary.length];
            }
            String[] word_nums = line.split(" ");
            for (String word_map : word_nums) {
                String word = word_map.split(":")[0];
                int freq = Integer.parseInt(word_map.split(":")[1]);
                if (BIAS) {
                    word_freq[reverseWordMap.get(word) + 1] = freq;
                } else {
                    word_freq[reverseWordMap.get(word)] = freq;
                }
            }
            featureVector[i] = new Feature(word_freq);
        }

        if (BIAS) {
            weight_per_class = new double[CLASS_SIZE][dictionary.length + 1];
        } else {
            weight_per_class = new double[CLASS_SIZE][dictionary.length];
        }
        initWeights(RANDOM_INITIALIZATION);
    }

    public void readTestFile(String file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        test_labels = new int[TEST_SIZE];
        testFeatureVector = new Feature[TEST_SIZE];

        String line;
        int line_num = 0;
        while ((line = bufferedReader.readLine()) != null) {
            int index = 0;
            while (line.charAt(index++) != ' ');

            test_labels[line_num] = Integer.parseInt(line.substring(0, index - 1));

            Integer key = test_labels[line_num];
            if (!perClassTotal.containsKey(key)) {
                perClassTotal.put(key, 1);
            } else {
                Integer val = perClassTotal.get(key);
                val++;
                perClassTotal.put(key, val);
            }

            line = line.substring(index);
            String[] word_nums = line.split(" ");
            int[] word_freq;
            if (BIAS) {
                word_freq = new int[dictionary.length + 1];
                word_freq[0] = 1;
            } else {
                word_freq = new int[dictionary.length];
            }
            for (String word_num : word_nums) {
                String word = word_num.split(":")[0];
                int freq = Integer.parseInt(word_num.split(":")[1]);
                if (!reverseWordMap.containsKey(word)) continue;
                if (BIAS) {
                    word_freq[reverseWordMap.get(word) + 1] = freq;
                } else {
                    word_freq[reverseWordMap.get(word)] = freq;
                }
            }
            testFeatureVector[line_num] = new Feature(word_freq);
            line_num++;
        }
    }

    private int getMaxClass(Feature feature) {
        int maxClass = 0;
        int maxCValue = Integer.MIN_VALUE;
        for (int c = 0; c < CLASS_SIZE; c++) {
            int dotProduct = 0;
            if (BIAS) {
                for (int r = 0; r < dictionary.length + 1; r++) {
                    dotProduct += weight_per_class[c][r] * feature.word_freq[r];
                }
            } else {
                for (int r = 0; r < dictionary.length; r++) {
                    dotProduct += weight_per_class[c][r] * feature.word_freq[r];
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
            int[] train_index = new int[TRAINING_SIZE];
            for (int i = 0; i < TRAINING_SIZE; i++) {
                train_index[i] = i;
            }
            if (CYCLE_DATA) {
                Random rand = new Random();
                for (int i = TRAINING_SIZE - 1; i > 0; i--) {
                    int n = rand.nextInt(i + 1);
                    train_index[i] = train_index[i] ^ train_index[n];
                    train_index[n] = train_index[i] ^ train_index[n];
                    train_index[i] = train_index[n] ^ train_index[i];
                }
            }
            for (int x = 0; x < TRAINING_SIZE; x++) {
                int t = train_index[x];
                int actual_label = training_labels[t];
                int predicted_label = getMaxClass(featureVector[t]);

                if (predicted_label != actual_label) {
                    if (BIAS) {
                        for (int r = 0; r < dictionary.length + 1; r++) {
                            weight_per_class[actual_label][r] += getAlpha(timeStep) * featureVector[t].word_freq[r];
                            weight_per_class[predicted_label][r] -= getAlpha(timeStep) * featureVector[t].word_freq[r];
                        }
                    } else {
                        for (int r = 0; r < dictionary.length; r++) {
                            weight_per_class[actual_label][r] += getAlpha(timeStep) * featureVector[t].word_freq[r];
                            weight_per_class[predicted_label][r] -= getAlpha(timeStep) * featureVector[t].word_freq[r];
                        }
                    }
                    numMisMatched++;
                }
            }
            timeStep++;
            System.out.println("At timestep "+ timeStep + " num mismatched = " + numMisMatched);
        } while (timeStep < MAX_EPOCH && numMisMatched != 0);
        System.out.println(timeStep);
    }

    public void testPerceptron(boolean confusion) {
        int numMisMatched = 0;
        for (int t = 0; t < TEST_SIZE; t++) {
            int actual_label = test_labels[t];
            int predicted_label = getMaxClass(testFeatureVector[t]);

            if (confusion) {
                confusionMatrix[actual_label][predicted_label]++;
            }
            if (predicted_label != actual_label) {
                numMisMatched++;
            }
        }
        System.out.println("At test num mismatched " + numMisMatched);
        System.out.println("perceptron accuracy " + Double.toString(100.0 - (numMisMatched * 100.0/TEST_SIZE)));
    }

    private double getDistance(Feature feature1, Feature feature2) {
        double dot_product = 0.0;
        double ai = 0.0;
        double bi = 0.0;
        for (int i = 0; i < dictionary.length; i++) {
            int p1 = feature1.word_freq[i];
            int p2 = feature2.word_freq[i];
            dot_product += p1 * p2;
            ai += p1 * p1;
            bi += p2 * p2;
        }
        return 1.0 - ((dot_product) / (Math.sqrt(ai) * Math.sqrt(bi)));
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
        for (int i = 0; i < TEST_SIZE; i++) {
            int original_label = test_labels[i];
            for (int t = 0; t < TRAINING_SIZE; t++) {
                double distance = getDistance(testFeatureVector[i], featureVector[t]);
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
        double percentage_accuracy = 100.0 - (100.0 * num_mismatch) / TEST_SIZE;
        System.out.println(k + ", " + percentage_accuracy);
        //System.out.println("KNN Accuracy is " + percentage_accuracy);
    }
//    public void runPerceptronGradientDescent() {
//        int timeStep = 0;
//        int numMisMatched = 0;
//        do {
//            numMisMatched = 0;
//            for (int t = 0; t < TRAINING_SIZE; t++) {
//                int actual_label = training_labels[t];
//                int predicted_label = getMaxClass(featureVector[t]);
//                //w ← w +α(y － f (x))σ (w ・x)(1－σ (w ・x))x
//                if (actual_label == predicted_label) continue;
//                numMisMatched++;
//
//                double dotproduct = 0.0;
//                for (int r = 0; r < ROW * COLUMN; r++) {
//                    dotproduct += weight_per_class[actual_label][r] * images[t].image_pixel[r];
//                }
//                for (int r = 0; r < ROW * COLUMN; r++) {
//                    weight_per_class[actual_label][r] +=
//                            getAlpha(timeStep) * images[t].image_pixel[r] * sigmoid(dotproduct) *
//                                    (1 - sigmoid(dotproduct));
//                }
//                System.out.println("Dot product = " + dotproduct + " sigmoid = " + sigmoid(dotproduct));
//                dotproduct = 0.0;
//                for (int r = 0; r < ROW * COLUMN; r++) {
//                    dotproduct += weight_per_class[predicted_label][r] * images[t].image_pixel[r];
//                }
//                System.out.println("Dot product = " + dotproduct + " sigmoid = " + sigmoid(dotproduct));
//                for (int r = 0; r < ROW * COLUMN; r++) {
//                    weight_per_class[predicted_label][r] -=
//                            getAlpha(timeStep) * images[t].image_pixel[r] * sigmoid(dotproduct) *
//                                    (1 - sigmoid(dotproduct));
//                }
//            }
//            timeStep++;
//            System.out.println("TimeStep = " + timeStep + " Mismatch = " + numMisMatched);
//        } while (numMisMatched != 0);
//    }

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
        TextClassification textClassification = new TextClassification();
        textClassification.readTrainingFile("/Users/saikat/Documents/UIUC/spring_2015/cs440/mp/AI_MP/MP4/GridWorld/8category/8category_training.txt");
        textClassification.readTestFile("/Users/saikat/Documents/UIUC/spring_2015/cs440/mp/AI_MP/MP4/GridWorld/8category/8category_testing.txt");
        textClassification.runPerceptron();
        textClassification.testPerceptron(true);
        textClassification.printConfusionMatrix(true);
//        for (int runs = 1; runs < 100; runs++)
//            textClassification.runKNN(runs);
    }
}
