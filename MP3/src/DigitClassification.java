import org.omg.PortableInterceptor.INACTIVE;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by sam on 4/9/15.
 */
public class DigitClassification {
    private final int aSize = 2;

    private double LAPLACE;// = 4.0; // tune smoothing

    private final int ROW = 70;
    private final int COLUMN = 60;
    private final int TRAINIMAGES = 451; // set low to debug
    private final int CLASSLABELS = 2; // 0-9 values

    private double[][] confusionMatrix ; // final matrix to print and calculate accuracy of prediction
    private int totalTests; // total test labels read
    private int[] classIndex = new int[CLASSLABELS];

    private HashMap<Integer, HashMap<Integer, Double[]>> pixelOddsPerClass = new HashMap<Integer, HashMap<Integer, Double[]>>();



    // save occurance count of each class type in test data
    private HashMap<Integer, Integer> perClassTotal = new HashMap<Integer, Integer>();

    // per class count of Fij values Fij  = 28*row+col
    // CLASS,HASH<index,[foregroundCount,backgroundCount]>
    private HashMap<Integer, HashMap<Integer, int[]>> testImagePixels = new HashMap<Integer, HashMap<Integer, int[]>>();

    // stores per class number of times its seen in test data
    private HashMap<Integer, Integer> classCountsInTest  = new HashMap<Integer, Integer>();

    // stores prob of each class in testdata
    private HashMap<Integer, Double> classProbInTest  = new HashMap<Integer, Double>();

    // per class  count of each type of pixels
    private HashMap<Integer, int[]> classPixelCounts = new HashMap<Integer, int[]>();

    // stores per class, per pixel prob it being foreground or background
    private HashMap<Integer, HashMap<Integer, Double[]>> testImagePixelProb = new HashMap<Integer, HashMap<Integer, Double[]>>();

    // to store per class probabilty
    private HashMap<Integer, Double> classProb = new HashMap<Integer, Double>();

    public DigitClassification(String trainImages, String trainLabels, double laplace) throws IOException {

        LAPLACE = laplace;
        confusionMatrix = new double[CLASSLABELS][CLASSLABELS];
        readTrainingFile(trainImages, trainLabels);
        calcClassProb();
        calcPixelCounts();
        calcPixelProb();
    }

    private void calcPixelCounts(){
        int t0=0;
        int t1=0;
        int t2=0;

        int[] tt;
        HashMap<Integer, int[]> t;
        for (int i = 0 ;i < CLASSLABELS ; i++ ){
            if(!testImagePixels.containsKey(i)) continue;
            t = testImagePixels.get(i);
            for(Map.Entry<Integer, int[]> entry : t.entrySet()) {
                // for each : should run till 784
                tt = entry.getValue().clone();
                t0+=tt[0];
                t1+=tt[1];
                if (aSize > 2){
                    t2+=tt[2];
                }
            }
            int[] ttp = new int[aSize];
            ttp[0]=t0;
            ttp[1]=t1;
            if (aSize > 2){
                ttp[2]=t2;
            }

            t0=0;
            t1=0;
            t2=0;
            classPixelCounts.put(i,ttp);
        }
    }

    private void calcClassProb(){
        for (int i = 0 ;i < CLASSLABELS ; i++ ){
            if(!testImagePixels.containsKey(i)) continue;
            int t = classCountsInTest.get(i);
            double d = (double) t / TRAINIMAGES;
            classProbInTest.put(i,d);
        }
    }

    private void calcPixelProb(){
        HashMap<Integer, int[]> t ;
        int[] tt ;
        int[] tt2 ;

        double backProb;
        double plusProb;
        double hashProb;

        double plusCount;
        double backCount;
        double hashCount;

        double[] probData;
        for(int i=0; i<CLASSLABELS; i++){
            if (!testImagePixels.containsKey(i)) continue;
            t = testImagePixels.get(i);
            tt = classPixelCounts.get(i);
            backCount = tt[0];
            plusCount = tt[1];
            if (aSize>2)
                hashCount = tt[2];

            HashMap<Integer, Double[]> p = new HashMap<Integer, Double[]>();
            for(Map.Entry<Integer, int[]> entry : t.entrySet()) {
                int index = entry.getKey();
                tt2 = entry.getValue().clone();
                backProb = (double) (tt2[0] + LAPLACE) / (classCountsInTest.get(i) + LAPLACE * (aSize));
                plusProb = (double) (tt2[1] + LAPLACE) / (classCountsInTest.get(i) + LAPLACE * (aSize));
                if (aSize>2)
                    hashProb = (double) (tt2[2] + LAPLACE) / (classCountsInTest.get(i) + LAPLACE * (aSize));
                Double[] pixelProbs = new Double[aSize];
                pixelProbs[0]=backProb;
                pixelProbs[1]=plusProb;
                if (aSize>2)
                    pixelProbs[2]=hashProb;
                p.put(index, pixelProbs);

            }
            testImagePixelProb.put(i,p);
        }

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

    private void printPixelProb(HashMap<Integer, HashMap<Integer, Double[]>> ha){
        HashMap<Integer, Double[]> t = new HashMap<Integer, Double[]>();
        for(Map.Entry<Integer, HashMap<Integer, Double[]>> entry : ha.entrySet()) {
            t = entry.getValue();
            Double[] temp;
            for(Map.Entry<Integer, Double[]> entry2 : t.entrySet()){
                temp = entry2.getValue();

                if (aSize > 2)
                    System.out.println("CLASS: " + entry.getKey() + " PIXELPROB:(" + entry2.getKey() + ") :" + temp[0] +":"+temp[1]+":"+temp[2]);
                else
                    System.out.println("CLASS: " + entry.getKey() + " PIXELPROB:(" + entry2.getKey() + ") :" + temp[0] +":"+temp[1]);
            }
        }
    }

    private void readTrainingFile(String images, String labels) throws IOException {
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(images));
        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(labels));
        int temp;
        char pixel;
        for (int i = 0; i < TRAINIMAGES; i++) {

            int trainLabel = Integer.parseInt(bufferedReader2.readLine());
            if (!classCountsInTest.containsKey(trainLabel)){
                classCountsInTest.put(trainLabel,1);
            } else {
                int t = classCountsInTest.get(trainLabel);
                classCountsInTest.put(trainLabel,t+1);
            }

            if (!testImagePixels.containsKey(trainLabel)) {
                HashMap<Integer, int[]> t = new HashMap<Integer, int[]>();
                int[] p = new int[aSize];// making all counts 0
                for (int r = 0; r < ROW; r++) {
                    for (int c = 0; c < COLUMN; c++) {
                        t.put(COLUMN * r + c, p);
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
                        pixelArrayIndex = COLUMN * j + index;
//                    pixelCount++;
                        pixel = line.charAt(index);
                        int pixelValues[] = new int[aSize];
                        pixelValues = pixelDetails.get(pixelArrayIndex).clone();
                        if (aSize > 2) {
                            if (pixel == ' ') {
                                pixelValues[0] = pixelValues[0] + 1;
                            } else if (pixel == '+') {
                                pixelValues[1] = pixelValues[1] + 1;
                            } else {
                                pixelValues[2] = pixelValues[2] + 1;
                            }
                        } else {
                            if (pixel == ' ') {
                                pixelValues[0] = pixelValues[0] + 1;
                            } else {
                                pixelValues[1] = pixelValues[1] + 1;
                            }
                        }


                        pixelDetails.put(pixelArrayIndex, pixelValues);
                    }
                    int pixelValues[] = new int[aSize];
                    while ( index != COLUMN){
                        int t = COLUMN * j + index;
                        pixelValues = pixelDetails.get(t).clone();
                        pixelValues[0]+=1;
                        pixelDetails.put(t,pixelValues);
                        index++;
                    }
                    testImagePixels.put(trainLabel, pixelDetails);
                }
            }
        }

    }


    private void predictDigit(String testImages, String testLables) throws IOException{
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(testImages));
        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(testLables));
        String in_line;
        while((in_line = bufferedReader2.readLine()) != null){
            if (in_line.equals("")) continue;
            int[] testPixels = new int[ROW*COLUMN];
            totalTests++;
            // for every test label read; read 28 rows of test images ( 28 x28)
            int testLabel = Integer.parseInt(in_line);
            if (!perClassTotal.containsKey(testLabel)){
                perClassTotal.put(testLabel,1);
            } else {
                int t = perClassTotal.get(testLabel);
                perClassTotal.put(testLabel,t+1);
            }
            char pixel;
            int flag;
            int raw_index ;
            for(int i=0; i<ROW;i++){
                String image_line = bufferedReader1.readLine();
                for(int j=0; j<COLUMN; j++){
                    raw_index = COLUMN*i+j;
                    pixel = image_line.charAt(j);
                    if (aSize > 2){
                        if (pixel == ' '){
                            flag = 0;
                        } else if (pixel == '+') {
                            flag = 1;
                        } else {
                            flag = 2;
                        }
                    } else {
                        if (pixel == ' '){
                            flag = 0;
                        } else  {
                            flag = 1;
                        }
                    }
                    testPixels[raw_index] = flag;
                }
            }
            double[] decisionProbs = new double[CLASSLABELS];
            for(int i = 0; i<CLASSLABELS; i++){
                if (!testImagePixels.containsKey(i)) continue;
                decisionProbs[i]=calcDecisionProb(testPixels,i);
            }
            // find max and return the index
            int predictedLabel = max(decisionProbs);
            confusionMatrix[testLabel][predictedLabel]++;
        }
    }

    private int max(double[] probs){
        int maxIndex = 0;
        double maxValue = probs[0];
        for(int i=0; i<probs.length; i++){
            if(Double.compare(probs[i],maxValue) > 0) {
                maxIndex = i;
                maxValue = probs[i];
            }
        }
        return maxIndex;
    }

    private int max(double[] probs,int skipIndex){
        int maxIndex = -1;
        double maxValue = -1.0;
        for(int i=0; i<probs.length; i++){
            if ( i == skipIndex) continue;
            if(Double.compare(probs[i],maxValue) > 0){
                maxIndex = i;
                maxValue = probs[i];
            }
        }
        return maxIndex;
    }
    private double calcDecisionProb(int[] pixelValues, int classLabel){
        double resultProb=0.0 ;
        resultProb = Math.log(classProbInTest.get(classLabel));
        HashMap<Integer, Double[]> pixelProb = new HashMap<Integer, Double[]>();
        pixelProb =  testImagePixelProb.get(classLabel);
        int index =0;
        for(int pixel: pixelValues){
            Double[] d = pixelProb.get(index);
            resultProb += Math.log(d[pixel]);
            index++;
        }
        return resultProb;
    }
    private void findHighConfusion(){
//        int[] classIndex = new int[10];
        for(int i=0; i<confusionMatrix.length; i++){
                classIndex[i]=max(confusionMatrix[i],i);
        }
        System.out.println(Arrays.toString(classIndex));
//        return classIndex
    }

    private void printClassForeGroundProb(HashMap<Integer, Double[]> ha, String str, int classLabel) throws IOException {
        File file1 = new File("/tmp/oddsMatrix"+classLabel+str+".csv");
        file1.createNewFile();
        FileWriter writer1 = new FileWriter(file1);

        for(Map.Entry<Integer, Double[]> entry : ha.entrySet()){
            writer1.write(Double.toString(entry.getValue()[1]));
            writer1.write(" ");
            if (entry.getKey() == (ROW-1) ){
                writer1.write("\n");
            }
        }


    }
    private void findOdds() throws IOException {
        //odds(Fij=1, c1, c2) = P(Fij=1 | c1) / P(Fij=1 | c2).
        int c1;
        int c2;
        double oddsProb ;
        double p1;
        double p2;
        HashMap<Integer, Double[]> pixelProbC1 = new HashMap<Integer, Double[]>();
        HashMap<Integer, Double[]> pixelProbC2 = new HashMap<Integer, Double[]>();
//        for(int index = 0;index< classIndex.length;index++){

        for(int index = 0;index< classIndex.length;index++){
            HashMap<Integer, Double[]> pixelOdds = new HashMap<Integer, Double[]>();
            c1=index;
            c2=classIndex[index];
            pixelProbC1 =  testImagePixelProb.get(c1);
            pixelProbC2 =  testImagePixelProb.get(c2);

//            printClassForeGroundProb(pixelProbC1, "numerator", index);
//            printClassForeGroundProb(pixelProbC2, "denominator", index);
            for (int i=0;i<ROW*COLUMN; i++){
                Double[] vl = new Double[3];
                // for each row column index
                p1=pixelProbC1.get(i)[1];
                p2=pixelProbC2.get(i)[1];
                oddsProb = p1/p2;
                vl[0]=p1;
                vl[1]=p2;
                vl[2]=oddsProb;
                pixelOdds.put(i,vl);
            }
            pixelOddsPerClass.put(index,pixelOdds);
        }
    }

    private void printOdds(HashMap<Integer, Double>  ha){
        for(int i=0; i<ROW;i++){
            for(int j=0;j<COLUMN;j++){
                System.out.print(ha.get(i * ROW+j)+" ");
            }
            System.out.println();
        }
    }

    private void writeOddsMatrix(boolean flag) throws IOException{
        String fileName;
        for(int c=0;c<CLASSLABELS;c++){
                File file = new File("/tmp/oddsMatrix"+c+".csv");
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                HashMap<Integer, Double[]> classOdds = new HashMap<Integer, Double[]>();
                classOdds =  pixelOddsPerClass.get(c);

                for(int i=0; i<ROW;i++){
                    for(int j=0;j<COLUMN;j++){
                        Double t[] = new Double[3] ;
                        t = classOdds.get(i * ROW+j);
                        if ( flag ) {
                            writer.write(Double.toString(t[0]));
                            writer.write(",");
                            writer.write(Double.toString(t[1]));
                            writer.write(",");
                            writer.write(Double.toString(t[2]));
                            writer.write("\n");
                        } else {
//                            writer.write(Double.toString(t));
//                            writer.write(" ");
                        }
                    }
                    if ( !flag ) {
                        writer.write("\n");
                    }
                }
                writer.flush();
                writer.close();
            }
    }
    private void printConfusionMatrix(Boolean debug) throws IOException {
        Double accuracy;
        Double t ;
        accuracy=0.0;
        if (debug) {
            System.out.println("TOTAL TEST DOCUMENTS READ   :" + totalTests);
            System.out.println("\n\n*** CONFUSION MATRIX ***\n");
        }
        for (int i = 0 ;i < CLASSLABELS; i++){
            for (int j = 0 ; j < CLASSLABELS ; j++){
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
        double a = accuracy/CLASSLABELS;

        if (debug) {
            System.out.println("--------------------------");
            System.out.print("OVERALL ACCURACY :");
            System.out.format("%5.3f", a);
            System.out.println("%");
            System.out.println("---------------------------");
        } else {
            System.out.println(LAPLACE+","+a);
        }


    }


    public static void main(String[] args) throws IOException {

//        String trainImages = "/Users/Sam/AI_MP/MP3/digitdata/trainingimages";
//        String trainLabels = "/Users/Sam/AI_MP/MP3/digitdata/traininglabels";
//        String testImages = "/Users/Sam/AI_MP/MP3/digitdata/testimages";
//        String testLabels = "/Users/Sam/AI_MP/MP3/digitdata/testlabels";
//        System.out.println();
//        System.out.println();
//        System.out.println("**********************************************");
//        System.out.println("        DIGIT IMAGE CLASSIFICATION : TERNARY  ");
//        System.out.println("**********************************************");


        String trainImages = "/Users/Sam/AI_MP/MP3/facedata/facedatatrain";
        String trainLabels = "/Users/Sam/AI_MP/MP3/facedata/facedatatrainlabels";
        String testImages = "/Users/Sam/AI_MP/MP3/facedata/facedatatest";
        String testLabels = "/Users/Sam/AI_MP/MP3/facedata/facedatatestlabels";
        System.out.println();
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("        FACE IMAGE CLASSIFICATION             ");
        System.out.println("**********************************************");



        for(int i=1;i<=1;i++){
            DigitClassification dc = new DigitClassification(trainImages, trainLabels,(double)i);
            dc.predictDigit(testImages, testLabels);
            dc.printConfusionMatrix(true);
        }
//
    }
}
