import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by sam on 4/8/15.
 */
public class TextDocClassification {
    public int LABEL_COUNT;
    private final double LAPLACE = 1.0;
    private Integer totalTrainingDoc = 0;
    private Set<String> totalUniqueWords = new HashSet<String>();
    private double[][] confusionMatrix ;
    private Integer totalTestDoc = 0 ;

    private HashMap<Integer, Integer> labelRawCount = new HashMap<Integer, Integer>(); // to store total words
    private HashMap<Integer, Integer> labelOccuranceCount = new HashMap<Integer, Integer>(); // to store total words
    private HashMap<Integer, Integer> labelTestOccuranceCount = new HashMap<Integer, Integer>(); // to store total words
    private HashMap<Integer, Double> labelOccuranceProb = new HashMap<Integer, Double>(); // to store total words
    private HashMap<Integer, HashMap<String, Integer>> labelWordCount = new HashMap<Integer, HashMap<String , Integer>>();
    private HashMap<Integer, HashMap<String, Double>> labelProbCount = new HashMap<Integer, HashMap<String , Double>>();

    public TextDocClassification(String fileName, int labelCount) throws IOException {
        LABEL_COUNT = labelCount;
        confusionMatrix = new double[LABEL_COUNT][LABEL_COUNT];

        readFile(fileName);
//        System.out.println(labelWordCount.get(3));
        for(int labelType = 0 ;labelType < LABEL_COUNT; labelType++){
            if (!labelWordCount.containsKey(labelType)) continue;
            calcTotalWords(labelType);
            labelProbCount.put(labelType,getProbCounts(labelType));
        }
//        System.out.println(labelProbCount.get(0));

    }

    private HashMap<String, Integer> getWordCounts(String[] str, Integer labelType){
        HashMap<String , Integer> res  = new HashMap<String, Integer>();
        String[] temp = new String[2];
        ArrayList<String > lineWords = new ArrayList<String>();
        if (! labelWordCount.containsKey(labelType)) {
            labelWordCount.put(labelType,new HashMap<String, Integer>());
        }
        res = labelWordCount.get(labelType);
        for (String st : str) {
            temp = st.split(":");
            String word = temp[0];
            totalUniqueWords.add(word);
            if (res.containsKey(word)) {
                int t = res.get(word);
                res.put(word, t + Integer.parseInt(temp[1]));
            } else {
                res.put(word, Integer.parseInt(temp[1]));
            }

        }
//        System.out.println(res);
        return res;
    }

    private void calcTotalWords(Integer labelType){
        int totalWords=0;
        HashMap<String , Integer> ha = labelWordCount.get(labelType);
        for(Map.Entry<String, Integer> entry : ha.entrySet()) {
            totalWords+=entry.getValue();
        }
        labelRawCount.put(labelType,totalWords);
    }
    private HashMap<String, Double> getProbCounts(Integer labelType) {
        double tempWordProb = 0.0;
        int totalWords = labelRawCount.get(labelType);
        HashMap<String, Integer> wordCountHash;
        HashMap<String, Double> res = new HashMap<String, Double>();
        wordCountHash = labelWordCount.get(labelType);
        totalWords += totalUniqueWords.size();
        for (Map.Entry<String, Integer> entry : wordCountHash.entrySet()) {
            tempWordProb = (double) (entry.getValue() + LAPLACE) / totalWords;
            res.put(entry.getKey(), tempWordProb);
        }
        return res;
    }

    private void readFile(String fileName) throws  IOException {
        BufferedReader  bufferedReader = new BufferedReader(new FileReader(fileName));
        String in_line;
        String[] temp ;
        while((in_line = bufferedReader.readLine()) != null){
            if (in_line.equals("")) continue;
            totalTrainingDoc++;
            temp = in_line.split(" ");
//            System.out.println("LINE IS "+in_line);
            int labelType = Integer.parseInt(temp[0]);
            if (!labelOccuranceCount.containsKey(labelType)){
                labelOccuranceCount.put(labelType,1);
            } else {
                int t = labelOccuranceCount.get(labelType);
                labelOccuranceCount.put(labelType,t + 1);
            }

//            if (!labelRawCount.containsKey(labelType)) {
//                labelRawCount.put(labelType,1);
//            }else{
//                int tempc = labelRawCount.get(labelType);
//                labelRawCount.put(labelType,tempc++);
//            }
            labelWordCount.put(labelType,getWordCounts(Arrays.copyOfRange(temp, 1, temp.length),labelType));

        }
    }

    private void calcLabelProb(){
//        int total = 0;
//        for (int i = 0 ;i < LABEL_COUNT ; i++ ){
//            if (!labelWordCount.containsKey(i)) continue;
//            total+=labelOccuranceCount.get(i);
//        }
        for (int i = 0 ;i < LABEL_COUNT ; i++ ){
            if (!labelWordCount.containsKey(i)) continue;
            double t = (double) labelOccuranceCount.get(i) / totalTrainingDoc;
            labelOccuranceProb.put(i,t);
        }


    }

    private Double classificationProb(String[] st, Integer labelType){
        int totalWords = labelRawCount.get(labelType);
        String[] keyValuePair = new String[2];
        int val;
        double totalProduct ;
        totalProduct = Math.log(labelOccuranceProb.get(labelType));
        double wordProb = 0.0;
        String key;
        HashMap<String , Double> prob ;
        prob = labelProbCount.get(labelType);
        for (String str : st) {
            keyValuePair = str.split(":");
            key = keyValuePair[0];
            val = Integer.parseInt(keyValuePair[1]);
            if (!totalUniqueWords.contains(key)) continue;
            if (!prob.containsKey(key)) {
                wordProb = LAPLACE / ( totalWords + totalUniqueWords.size());
            } else {
                wordProb = prob.get(key);
            }
            totalProduct+= Math.log(wordProb)*val;
        }
        return  totalProduct;
    }
    private void predictClassification(String fileName) throws  IOException{


        BufferedReader  bufferedReader = new BufferedReader(new FileReader(fileName));
        String in_line;
        String[] temp ;
        while((in_line = bufferedReader.readLine()) != null){
            ArrayList<Double> classificationProbs = new ArrayList<Double>(LABEL_COUNT);
            if (in_line.equals("")) continue;
            totalTestDoc++;
            temp = in_line.split(" ");
            int testLabelType = Integer.parseInt(temp[0]);
            if (!labelTestOccuranceCount.containsKey(testLabelType)){
                labelTestOccuranceCount.put(testLabelType,1);
            } else {
                int t = labelTestOccuranceCount.get(testLabelType);
                labelTestOccuranceCount.put(testLabelType,t + 1);
            }

            for(int labelType = 0 ;labelType < LABEL_COUNT; labelType++){
                if (!labelWordCount.containsKey(labelType)) continue;
                Double t =  classificationProb(Arrays.copyOfRange(temp, 1, temp.length), labelType);
                classificationProbs.add(t);
            }
            Double max = classificationProbs.get(0);
            int index = 0;
            int i=0;

            for ( Double t : classificationProbs){
                if ( t > max ) {
                    index = i ;
                    max = t;
                }
                i++;
            }
            confusionMatrix[testLabelType][index]++;
//            System.out.println("TEST: "+testLabelType+" PREDICTION: "+index);
        }
    }

    private  void printConfusionMatrix(){
        DecimalFormat df = new DecimalFormat("#.00");
        Double accuracy = 0.0;
        Double t ;
        System.out.println("TOTAL TRAINING DOCUMENTS READ" + totalTestDoc);
        for(int i=0; i<LABEL_COUNT; i++){
            System.out.println("TOTAL TEST DOCUMENTS OF TYPE "+i+" READ : "+labelTestOccuranceCount.get(i));
        }
        for (int i = 0 ;i < LABEL_COUNT; i++){
            for (int j = 0 ; j < LABEL_COUNT ; j++){
                t = confusionMatrix[i][j]/labelTestOccuranceCount.get(i)*100;
                System.out.print(df.format(t)+"% ");
                if ( i == j) {
                    accuracy+=t;
                }
            }
            System.out.println();

        }
        System.out.println("--------------------------------------------------------");
        System.out.println("OVERALL ACCURACY :"+df.format(accuracy/LABEL_COUNT)+"%");
        System.out.println("--------------------------------------------------------");
    }

    public static  void main(String[] args) throws IOException {
        System.out.println(" ************** SPAM EMAIL CLASSIFICATION ********** ");
        TextDocClassification nf = new TextDocClassification("/Users/Sam/AI_MP/MP3/spam_detection/train_email.txt",2);
        String testFile = "/Users/Sam/AI_MP/MP3/spam_detection/test_email.txt";
        nf.calcLabelProb();
        nf.predictClassification(testFile);
        nf.printConfusionMatrix();

        System.out.println();
        System.out.println();
        System.out.println(" ************** NEWS CLASSIFICATION ********** ");
        nf = new TextDocClassification("/Users/Sam/AI_MP/MP3/8category/8category.training.txt",8);
        testFile = "/Users/Sam/AI_MP/MP3/8category/8category.testing.txt";
        nf.calcLabelProb();
        nf.predictClassification(testFile);
        nf.printConfusionMatrix();


    }
}
