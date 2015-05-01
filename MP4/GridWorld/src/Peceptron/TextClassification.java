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

    private final int CLASS_SIZE = 8;

    private double[][] weight_per_class;
    private Map<String, Integer> reverseWordMap;
    int[] training_labels;
    int[] test_labels;
    Feature[] featureVector;
    Feature[] testFeatureVector;

    String[] dictionary;

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
        initWeights(false);
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
            for (int t = 0; t < TRAINING_SIZE; t++) {
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
        } while (numMisMatched != 0);
        System.out.println(timeStep);
    }

    public void testPerceptron() {
        int numMisMatched = 0;
        for (int t = 0; t < TEST_SIZE; t++) {
            int actual_label = test_labels[t];
            int predicted_label = getMaxClass(testFeatureVector[t]);

            if (predicted_label != actual_label) {
                numMisMatched++;
            }
        }
        System.out.println("At test num mismatched " + numMisMatched);
        System.out.println("perceptron accuracy " + Double.toString(100.0 - (numMisMatched * 100.0/TEST_SIZE)));
    }

    public static void main(String[] args) throws IOException {
        TextClassification textClassification = new TextClassification();
        textClassification.readTrainingFile("/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/8category/8category_training.txt");
        textClassification.readTestFile("/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/8category/8category_testing.txt");
        textClassification.runPerceptron();
        textClassification.testPerceptron();
    }
}
