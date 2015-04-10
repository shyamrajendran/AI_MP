import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by sam on 4/9/15.
 */
public class DigitClassification {
    private final int ROW = 28;
    private final int COLUMN = 28;
    private final int TRAINIMAGES = 1;


    // per class count of Fij values Fij  = 28*row+col
//    private HashMap<Integer, HashMap<Integer, Integer>> testImagePixels = new HashMap<Integer, HashMap<Integer, Integer>>();
    private HashMap<Integer, HashMap<Integer, int[]>> testImagePixels = new HashMap<Integer, HashMap<Integer, int[]>>();
    // CLASS,HASH<index,[foregroundCount,backgroundCount]>


    // to store per class probabilty
    private HashMap<Integer, Double> classProb = new HashMap<Integer, Double>();


    public DigitClassification(String trainImages, String trainLabels) throws IOException {
        readTrainingFile(trainImages, trainLabels);
        printMap(testImagePixels);
    }

    private void printMap(HashMap<Integer, HashMap<Integer, int[]>> ha){
        HashMap<Integer, int[]> t = new HashMap<Integer, int[]>();
        for(Map.Entry<Integer, HashMap<Integer, int[]>> entry : ha.entrySet()) {
            t = entry.getValue();
            int[] temp;
            for(Map.Entry<Integer, int[]> entry2 : t.entrySet()){
                temp = entry2.getValue();
                System.out.println("CLASS: " + entry.getKey() + " PIXEL:(" + entry2.getKey() + ") :" + temp[0] +":"+temp[1]);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        String trainImages = "/Users/Sam/AI_MP/MP3/digitdata/trainimages_sample";
        String trainLabels = "/Users/Sam/AI_MP/MP3/digitdata/trainlabels_sample";
        DigitClassification dc = new DigitClassification(trainImages, trainLabels);
    }

    private void readTrainingFile(String images, String labels) throws IOException {
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(images));
        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(labels));
        int temp;

        char pixel;
//        int pixelCount = 0 ;

        for (int i = 0; i < TRAINIMAGES; i++) {
            int trainLabel = Integer.parseInt(bufferedReader2.readLine());


            if (!testImagePixels.containsKey(trainLabel)) {
                HashMap<Integer, int[]> t = new HashMap<Integer, int[]>();
                int[] p = new int[2];// making all counts 0
                for (int r = 0; r < ROW; r++) {
                    for (int c = 0; c < COLUMN; c++) {
                        t.put(ROW * r + c, p);
                    }
                }
                testImagePixels.put(trainLabel, t);
            }
            HashMap<Integer, int[]> pixelDetails = testImagePixels.get(trainLabel);
            int j;
            int pixelArrayIndex;
            String line;
            for ( j = 0; j < ROW; j++) {
                if ((  line = bufferedReader1.readLine()) != null ){
                    int index = 0;

                    for (; index < line.length(); index++) {
                        pixelArrayIndex = ROW * j + index;
//                    pixelCount++;
                        pixel = line.charAt(index);
                        int pixelValues[];
                        pixelValues = pixelDetails.get(pixelArrayIndex).clone();
                        if (pixel == ' ') {
                            pixelValues[0] = pixelValues[0] + 1;
                        } else {
                            pixelValues[1] = pixelValues[1] + 1;
                        }
                        pixelDetails.put(pixelArrayIndex, pixelValues);
                    }
                    int pixelValues[];
                    while ( index != COLUMN){
                        int t = ROW * j + index;
                        pixelValues = pixelDetails.get(t).clone();
                        pixelValues[0]+=1;
                        pixelDetails.put(t,pixelValues);
                        index++;
                    }
//                for(Map.Entry<Integer, int[]> entry : pixelDetails.entrySet()) {
//                    pixelValues=entry.getValue();
//                    pixelValues[0]=pixelValues[0]+(COLUMN-pixelCount);
//                    pixelDetails.put(entry.getKey(), pixelValues);
//                }
//                pixelCount = 0;
                    testImagePixels.put(trainLabel, pixelDetails);
                }
            }
            printMap(testImagePixels);
        }

    }

}
