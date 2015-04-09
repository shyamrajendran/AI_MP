import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by sam on 4/8/15.
 */
public class newsClassification {
    private final int LABEL_COUNT = 8;
    private final double LAPLACE = 1.0;
    private Set<String> totalUniqueWords = new HashSet<String>();
    private HashMap<Integer, Integer> labelRawCount = new HashMap<Integer, Integer>();
    // to hold per label details
    // total number of words
    private HashMap<Integer, HashMap<String, Integer>> labelWordCount = new HashMap<Integer, HashMap<String , Integer>>();
    private HashMap<Integer, HashMap<String, Double>> labelProbCount = new HashMap<Integer, HashMap<String , Double>>();

    public newsClassification(String fileName) throws IOException {
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
            temp = in_line.split(" ");
//            System.out.println("LINE IS "+in_line);
            int labelType = Integer.parseInt(temp[0]);
//            if (!labelRawCount.containsKey(labelType)) {
//                labelRawCount.put(labelType,1);
//            }else{
//                int tempc = labelRawCount.get(labelType);
//                labelRawCount.put(labelType,tempc++);
//            }
            labelWordCount.put(labelType,getWordCounts(Arrays.copyOfRange(temp, 1, temp.length),labelType));

        }
    }



    private void predictClassification(String fileName){


    }
    public static  void main(String[] args) throws IOException {
        newsClassification nf = new newsClassification("/Users/Sam/AI_MP/MP3/8category/8category.training_sample.txt");
//        SpamFilter sf = new SpamFilter("/Users/Sam/AI_MP/MP3/spam_detection/sample.txt");
//        String testFile = "/Users/Sam/AI_MP/MP3/spam_detection/test_email.txt";
//        String testFile = "/Users/Sam/AI_MP/MP3/spam_detection/test_email_sample.txt";
//        nf.predictClassification(testFile);

    }
}
