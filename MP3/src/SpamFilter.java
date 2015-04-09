
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;


/**
 * Created by sam on 4/7/15.
 */
public class SpamFilter {
    //
    // DO NOT USE THIS CLASS. NEWSCLASSIFICATION ITSELF WILL DO THE JOB NOW
    //
    final private double LAPLACE = 1.0;
    private int spam_messages = 0;
    private int non_spam_message = 0;
    private ArrayList<String[]> spam_words;
    private HashMap<String, Integer> spam_hash = new HashMap<String, Integer>();
    private HashMap<String, Integer> non_spam_hash = new HashMap<String, Integer>();
    private HashMap<String, Double> spamCondProb = new HashMap<String, Double>();
    private HashMap<String, Double> nonSpamCondProb = new HashMap<String, Double>();
    private Set<String> totalUniqueWords = new HashSet<String>();
    private HashMap<String , ArrayList<Double>> confusionMatrix = new HashMap<String , ArrayList<Double>>(2);

    private int uniqSpamWords;
    private int uniqNonSpamWords;
    private int totalSpamWords;
    private int totalNonSpamWords;
    private int testDocumentSpamCount = 0 ;
    private int testDocumentNonSpamCount = 0;
    private double spamProb ;


    public SpamFilter(String fileName) throws IOException {
        readFile(fileName);
        spamProb = calcSpamProb(spam_messages, non_spam_message);
        spamCondProb = calcCondProb(spam_hash);
        nonSpamCondProb = calcCondProb(non_spam_hash);
    }


    private int calcTotalWords(HashMap<String, Integer> ha){
        int totalWords = 0 ;
        for(Map.Entry<String, Integer> entry : ha.entrySet()) {
            totalWords+=entry.getValue();
        }
        return totalWords;
    }
    private HashMap<String, Double> calcCondProb(HashMap<String, Integer> ha) {
        HashMap<String, Double> res = new HashMap<String, Double>();
        double tempWordProb = 0.0;
        int totalWords = calcTotalWords(ha);
        // get total number of unique words to add here.
        totalWords+=totalUniqueWords.size();
        for(Map.Entry<String, Integer> entry : ha.entrySet()) {
            tempWordProb = (double ) (entry.getValue()+ LAPLACE)  / totalWords;
            res.put(entry.getKey(), tempWordProb);
        }
        return res;
    }


    private double calcMessProb(String[] message, Boolean type){
        int totalWords;
        String[] keyValuePair = new String[2];
        int val;
        double totalProduct ;
        double wordProb = 0.0;
        String key;
        if ( type ) {
            totalWords = calcTotalWords(spam_hash);
            totalProduct = Math.log(spamProb);
        } else {
            totalWords = calcTotalWords(non_spam_hash);
            totalProduct = Math.log(1 - spamProb);
        }


        for (String st : message){
            keyValuePair= st.split(":");
            key = keyValuePair[0];
            val = Integer.parseInt(keyValuePair[1]);
            if (! totalUniqueWords.contains(key)) continue;
            // this is as per post https://piazza.com/class/i56vzaj3akl4wf?cid=398
            // “create dictionaries consisting of all unique words occurring in the training documents”
            // Apply Laplace smoothing to the words in the dictionary (some words may not appear in certain classes).
            // For words in the test documents, which doesn't appear in the dictionary, ignore them.

            if (type){
                // indicates spam message has been read. Now check what your classification predicts
                if (!spamCondProb.containsKey(key)){
                    wordProb = LAPLACE / ( totalWords + totalUniqueWords.size());
                } else {
                    wordProb = spamCondProb.get(key);
                }

            } else {
                if (!nonSpamCondProb.containsKey(key)){
                    wordProb = LAPLACE / ( totalWords + totalUniqueWords.size());
                } else {
                    wordProb = nonSpamCondProb.get(key);
                }
            }
            totalProduct += Math.log(wordProb) * val;

        }
        return totalProduct;
    }

    private void printConfusionMatrix(){
        DecimalFormat df = new DecimalFormat("#.00");

        System.out.println("TEST SPAM DOCUMENTS READ     :" +testDocumentSpamCount);
        System.out.println("TEST NON SPAM DOCUMENTS READ :" +testDocumentNonSpamCount);
        System.out.println("CONFUSION MATRIX");
        System.out.println("***** PREDICTION SCORES ********* ");
        System.out.println("    SPAM    NONSPAM");
        System.out.print("SPAM  ");

        for (Double d : confusionMatrix.get("spam")){
            System.out.print(df.format(d/testDocumentSpamCount*100)+"%" + " ");
        }
        System.out.println();
        System.out.print("NONSPAM ");
        for (Double d : confusionMatrix.get("nonSpam")){
            System.out.print(df.format(d/testDocumentNonSpamCount*100)+"%" + " ");
        }
    }
    private void predictClassification(String fileName) throws IOException {
        BufferedReader  bufferedReader = new BufferedReader(new FileReader(fileName));
        String in_line;
        String[] temp ;
        double messageSpam ;
        String test;
        String prediction;
        double messageNonSpam;
        double res;

        while((in_line = bufferedReader.readLine()) != null){
            ArrayList<Double> matrixDetails = new ArrayList<Double>(2);
            if (in_line.equals("")) continue;
            temp = in_line.split(" ");
            if ( temp[0].equals("1") ) {
                test = "spam";
                testDocumentSpamCount++;
            } else {
                test = "nonSpam";
                testDocumentNonSpamCount++;
            }


            messageSpam = calcMessProb(Arrays.copyOfRange(temp, 1, temp.length), Boolean.TRUE);
            messageNonSpam = calcMessProb(Arrays.copyOfRange(temp, 1, temp.length), Boolean.FALSE);

            if (messageSpam > messageNonSpam) {
                prediction = "spam";
            } else {
                prediction = "nonSpam";
            }

            double t0;
            double t1;
            if (test.equals(prediction)){
                if (test.equals("spam")){
                    // spam spam
                    if (! confusionMatrix.containsKey("spam")) {
                        matrixDetails.add(1.0);
                        matrixDetails.add(0.0);
                        confusionMatrix.put("spam",matrixDetails);
                    } else {
                        matrixDetails = confusionMatrix.get("spam");
                        t0 = matrixDetails.get(0);
                        matrixDetails.set(0, t0 + 1);
                        confusionMatrix.put("spam",matrixDetails);
                    }
                } else{
                    //not spam not spam
                    if (! confusionMatrix.containsKey("nonSpam")) {
                        matrixDetails.add(0.0);
                        matrixDetails.add(1.0);
                        confusionMatrix.put("nonSpam",matrixDetails);
                    } else {
                        matrixDetails = confusionMatrix.get("nonSpam");
                        t1 = matrixDetails.get(1);
                        matrixDetails.set(1,t1+1);
                        confusionMatrix.put("nonSpam",matrixDetails);
                    }

                }
            } else {
                if (test.equals("spam")){
                    // spam spam
                    if (! confusionMatrix.containsKey("spam")) {
                        matrixDetails.add(0.0);
                        matrixDetails.add(1.0);
                        confusionMatrix.put("spam",matrixDetails);
                    } else {
                        matrixDetails = confusionMatrix.get("spam");
                        t1 = matrixDetails.get(1);
                        matrixDetails.set(1, t1 + 1);
                        confusionMatrix.put("spam",matrixDetails);
                    }
                } else{
                    //not spam not spam
                    if (! confusionMatrix.containsKey("nonSpam")) {
                        matrixDetails.add(1.0);
                        matrixDetails.add(0.0);
                        confusionMatrix.put("nonSpam",matrixDetails);
                    } else {
                        matrixDetails = confusionMatrix.get("nonSpam");
                        t0 = matrixDetails.get(0);
                        matrixDetails.set(0,t0 + 1);
                        confusionMatrix.put("nonSpam",matrixDetails);
                    }

                }
            }
        }
        printConfusionMatrix();

    }
    private double calcSpamProb(int spam_m, int non_spam_m){
        return  ( double) spam_m / (spam_m + non_spam_m);

    }
    private void printMaps(HashMap<String, Integer> ha){
        for(Map.Entry<String, Integer> entry : ha.entrySet()){
            System.out.printf("Key : %s :: %s %n", entry.getKey(), entry.getValue());
        }
    }
    private void printMapsD(HashMap<String, Double> ha){
        for(Map.Entry<String, Double> entry : ha.entrySet()){
            System.out.printf("Key : %s :: %s %n", entry.getKey(), entry.getValue());
        }
    }
    private void populateCounts(String[] str, Boolean docType){
        String[] temp = new String[2];

        int val = 0;
        Boolean cFlag = Boolean.TRUE;
        for (String st : str){
            if (cFlag) {
                cFlag = Boolean.FALSE;
                continue;

            }
            temp = st.split(":");
            totalUniqueWords.add(temp[0]);
            if (docType){

                if (  !spam_hash.containsKey(temp[0]) ) {
                    spam_hash.put(temp[0],Integer.parseInt(temp[1]));
                }else {
                    val = spam_hash.get(temp[0]);
                    spam_hash.put(temp[0], val+Integer.parseInt(temp[1]));
                }
            } else {
                if (  !non_spam_hash.containsKey(temp[0])) {
                    non_spam_hash.put(temp[0], Integer.parseInt(temp[1]));
                }else {
                    val = non_spam_hash.get(temp[0]);
                    non_spam_hash.put(temp[0], val+Integer.parseInt(temp[1]));
                }
            }
        }
    }

    private void readFile(String fileName) throws IOException {
        BufferedReader  bufferedReader = new BufferedReader(new FileReader(fileName));
        String in_line;
        String[] temp ;
        while((in_line = bufferedReader.readLine()) != null){
            if (in_line.equals("")) continue;
            temp = in_line.split(" ");
//            System.out.println("LINE IS "+in_line);
            if ( temp[0].equals("0") ) {
                non_spam_message++;
                populateCounts(temp, Boolean.FALSE);
            }else{
                spam_messages++;
                populateCounts(temp, Boolean.TRUE);
            }
        }

    }

    public static  void main(String[] args) throws IOException {
        SpamFilter sf = new SpamFilter("/Users/Sam/AI_MP/MP3/spam_detection/train_email.txt");
//        SpamFilter sf = new SpamFilter("/Users/Sam/AI_MP/MP3/spam_detection/sample.txt");
        String testFile = "/Users/Sam/AI_MP/MP3/spam_detection/test_email.txt";
//        String testFile = "/Users/Sam/AI_MP/MP3/spam_detection/test_email_sample.txt";
        sf.predictClassification(testFile);

    }
}
