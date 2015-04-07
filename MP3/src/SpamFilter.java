
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * Created by sam on 4/7/15.
 */
public class SpamFilter {
    private int spam_messages = 0;
    private int non_spam_message = 0;
    private ArrayList<String[]> spam_words;
    private HashMap<String, Integer> spam_hash = new HashMap<String, Integer>();
    private HashMap<String, Integer> non_spam_hash = new HashMap<String, Integer>();
    private HashMap<String, Double> spamCondProb = new HashMap<String, Double>();
    private HashMap<String, Double> nonSpamCondProb = new HashMap<String, Double>();
    private Set<String> totalUniqueWords = new HashSet<String>();
    private int uniqSpamWords;
    private int uniqNonSpamWords;
    private int totalSpamWords;
    private int totalNonSpamWords;
    private double spamProb ;


    public SpamFilter(String fileName) throws IOException {
        readFile(fileName);
        printMaps(spam_hash);
        spamProb = calcSpamProb(spam_messages, non_spam_message);
        spamCondProb = calcCondProb(spam_hash);
        nonSpamCondProb = calcCondProb(non_spam_hash);
        System.out.println("SPAMCONDITIONAL PROB");
        printMapsD(spamCondProb);
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
            System.out.println("ENTRY CALU FOR" + entry.getKey() + "VALUE IS "+ entry.getValue() + "| TOTAL is "+totalWords);
            tempWordProb = (double ) (entry.getValue()+1)  / totalWords;
            res.put(entry.getKey(), tempWordProb);
        }
        return res;
    }


    private double calcMessProb(String[] message, Boolean type){
        int totalWords;
        String[] keyValuePair = new String[2];
        int val;
        double totalProduct ;
        int numerator = 0;
        double wordProb = 0.0;
        String key;
        if ( type ) {
            totalWords = calcTotalWords(spam_hash);
            totalProduct = spamProb;
        } else {
            totalWords = calcTotalWords(non_spam_hash);
            totalProduct = 1 - spamProb;
        }


        for (String st : message){
            keyValuePair= st.split(":");
            key = keyValuePair[0];
            val = Integer.parseInt(keyValuePair[1]);
            if (type){
                // indicates spam message has been read. Now check what your classification predicts
                if (!spamCondProb.containsKey(key)){
                    wordProb = 1.0 / ( totalWords + totalUniqueWords.size());
                } else {
                    wordProb = spamCondProb.get(key);
                }

            } else {
                if (!nonSpamCondProb.containsKey(key)){
                    wordProb = 1.0 / ( totalWords + totalUniqueWords.size());
                } else {
                    wordProb = nonSpamCondProb.get(key);
                }
            }
            wordProb = Math.pow(wordProb,(double) val);
            totalProduct*=(wordProb);
            ;
        }
        return totalProduct;
    }

    private void predictClassification(String fileName) throws IOException {
        BufferedReader  bufferedReader = new BufferedReader(new FileReader(fileName));
        String in_line;
        String[] temp ;
        double messageSpam ;
        double messageNonSpam;
        double res;
        while((in_line = bufferedReader.readLine()) != null){
            if (in_line.equals("")) continue;
            temp = in_line.split(" ");
            messageSpam = calcMessProb(Arrays.copyOfRange(temp, 1, temp.length), Boolean.TRUE);
            messageNonSpam = calcMessProb(Arrays.copyOfRange(temp, 1, temp.length), Boolean.FALSE);
            if (messageSpam > messageNonSpam ) {
                System.out.println("GIVEN : "+temp[0]+" | PREDICTION :SPAM\n" + messageNonSpam +" "+messageSpam);
            } else {
                System.out.println("GIVEN : "+temp[0]+" | PREDICTION :NONSPAM\n" + messageNonSpam+" "+messageSpam);
            }

        }

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
//        ArrayList<String[]> line_words = new ArrayList<String[]>();
        String[] temp ;
        while((in_line = bufferedReader.readLine()) != null){
            if (in_line.equals("")) continue;
            temp = in_line.split(" ");
            System.out.println("LINE IS "+in_line);
            if ( temp[0].equals("0") ) {
                non_spam_message++;
                populateCounts(temp, Boolean.FALSE);
            }else{
                spam_messages++;
                populateCounts(temp, Boolean.TRUE);
            }
//            line_words.add(temp);
        }

    }

    public static  void main(String[] args) throws IOException {
//        SpamFilter sf = new SpamFilter("/Users/Sam/AI_MP/MP3/spam_detection/train_email.txt");
        SpamFilter sf = new SpamFilter("/Users/Sam/AI_MP/MP3/spam_detection/sample.txt");
        String testFile = "/Users/Sam/AI_MP/MP3/spam_detection/test_email_sample.txt";
        sf.predictClassification(testFile);

    }
}
