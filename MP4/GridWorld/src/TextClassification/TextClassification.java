package TextClassification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by sam on 5/1/15.
 */
public class TextClassification {
    private HashMap<Integer, HashMap<String, Integer>> labelWordCount = new HashMap<Integer, HashMap<String , Integer>>();


    private Set<String> totalUniqueWords = new HashSet<String>();

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
        return res;
    }



    private void readFile(String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        String in_line;
        String[] temp ;
        while((in_line = bufferedReader.readLine()) != null){
            if (in_line.equals("")) continue;

            temp = in_line.split(" ");
            int labelType = Integer.parseInt(temp[0]);

            labelWordCount.put(labelType,getWordCounts(Arrays.copyOfRange(temp, 1, temp.length),labelType));
        }
    }

}
