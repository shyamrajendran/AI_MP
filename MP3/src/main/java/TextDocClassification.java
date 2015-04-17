import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by sam on 4/8/15.
 */
public class TextDocClassification {
    public int LABEL_COUNT;
    private  double LAPLACE;
    private Integer totalTrainingDoc = 0;
    private Set<String> totalUniqueWords = new HashSet<String>();
    private double[][] confusionMatrix ;
    private Integer totalTestDoc = 0 ;
    private  boolean EXTRA = false;
    private HashMap<Integer, Integer> labelRawCount = new HashMap<Integer, Integer>(); // to store total words
    private HashMap<Integer, Integer> labelOccuranceCount = new HashMap<Integer, Integer>(); // to store total words
    private HashMap<Integer, Integer> labelTestOccuranceCount = new HashMap<Integer, Integer>(); // to store total words
    private HashMap<Integer, Double> labelOccuranceProb = new HashMap<Integer, Double>(); // to store total words
    private HashMap<Integer, HashMap<String, Integer>> labelWordCount = new HashMap<Integer, HashMap<String , Integer>>();
    private HashMap<Integer, HashMap<String, Double>> labelProbCount = new HashMap<Integer, HashMap<String , Double>>();
    private HashMap<Integer, String[]> perClassTopLikelihoodWords = new HashMap<Integer, String[]>();
    private HashMap<Integer, HashMap<String, Double>> wordsOddPerClass = new HashMap<Integer, HashMap<String, Double>>();
    private int[] classIndex ;

    public TextDocClassification(String fileName, int labelCount, int laplace) throws IOException {
        LABEL_COUNT = labelCount;
        LAPLACE = (double) laplace;
        classIndex = new int[LABEL_COUNT];
        confusionMatrix = new double[LABEL_COUNT][LABEL_COUNT];
        readFile(fileName);
        for(int labelType = 0 ;labelType < LABEL_COUNT; labelType++){
            if (!labelWordCount.containsKey(labelType)) continue;
            calcTotalWords(labelType);
            labelProbCount.put(labelType,getProbCounts(labelType));
        }
    }

    public TextDocClassification(String dataFile, String labelFile, String mapFile,int laplace) throws IOException {
        EXTRA = true;
        LAPLACE = (double) laplace;
        classIndex = new int[21];
        confusionMatrix = new double[21][21];
        readFile(readFileExtra(dataFile, labelFile, mapFile,"trainParsed"));
        for(int labelType = 0 ;labelType < LABEL_COUNT; labelType++){
            if (!labelWordCount.containsKey(labelType)) continue;
            calcTotalWords(labelType);
            labelProbCount.put(labelType,getProbCounts(labelType));
        }

    }


    private String readFileExtra(String dataFile, String labelFile, String mapFile, String opFile) throws  IOException {
        File file1 = new File(opFile);
        file1.createNewFile();
        FileWriter writer1 = new FileWriter(file1);

        BufferedReader  dataReader = new BufferedReader(new FileReader(dataFile));
        BufferedReader  labelReader = new BufferedReader(new FileReader(labelFile));
        BufferedReader  mapReader = new BufferedReader(new FileReader(mapFile));
        String in_line;

        int localLabel=0;
        // find label count :
        while((in_line = mapReader.readLine()) != null){
            localLabel++;
        }
        LABEL_COUNT = localLabel+1;

        int docId;
        String str;
        List<String> strList ;
        String[] str2;
        HashMap<Integer, String> perLine = new HashMap<Integer, String>();
        while((in_line = dataReader.readLine()) != null){
            String tmp;
            str2 = in_line.split(" ");
            docId = Integer.parseInt(str2[0]);
            tmp = str2[1]+":"+str2[2]+" ";
            if (!perLine.containsKey(docId)){
                perLine.put(docId,tmp);
            } else {
                String tmp2;
                tmp2 = perLine.get(docId);
                tmp2 = tmp2+tmp;
                perLine.put(docId,tmp2);
            }
        }

        int index=1;

        while((in_line = labelReader.readLine()) != null){
            int labelId;
            String finalOp;
            labelId = Integer.parseInt(in_line.split(" ")[0]);
            finalOp = labelId+" "+perLine.get(index);
            writer1.write(finalOp);
            writer1.write("\n");
            index++;
        }
        writer1.flush();
        writer1.close();
        return opFile;
    }
    private void highestLikelihood() throws IOException {
        HashMap<String, Double> ha = new HashMap<String, Double>();
        for(Map.Entry<Integer, HashMap<String, Double>> entry : labelProbCount.entrySet()) {
            File file1 = new File("/tmp/"+entry.getKey()+".csv");
            file1.createNewFile();
            FileWriter writer1 = new FileWriter(file1);
            System.out.println("CLASS :"+entry.getKey());
            ha = entry.getValue();
            for(Map.Entry<String, Double> entry2 : ha.entrySet()) {
                writer1.write(entry2.getKey());
                writer1.write(",");
                writer1.write(String.format( "%.10f", entry2.getValue() ));
                writer1.write("\n");
            }
        }
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
            int labelType = Integer.parseInt(temp[0]);
            if (!labelOccuranceCount.containsKey(labelType)){
                labelOccuranceCount.put(labelType,1);
            } else {
                int t = labelOccuranceCount.get(labelType);
                labelOccuranceCount.put(labelType,t + 1);
            }
            labelWordCount.put(labelType,getWordCounts(Arrays.copyOfRange(temp, 1, temp.length),labelType));
        }
    }

    private void calcLabelProb(){
        for (int i = 0 ;i < LABEL_COUNT ; i++ ){
            if (!labelWordCount.containsKey(i)) continue;
            double t = (double) labelOccuranceCount.get(i) / totalTrainingDoc;
            labelOccuranceProb.put(i,t);
        }
    }

    private void findOdds() throws IOException {
        //odds(Fij=1, c1, c2) = P(Fij=1 | c1) / P(Fij=1 | c2).
        int c1;
        int c2;
        double oddsProb ;
        double p1;
        double p2;
        Double vl;

        HashMap<String, Double> wordsProbC1 = new HashMap<String, Double>();
        HashMap<String, Double> wordsProbC2 = new HashMap<String, Double>();
//        for(int index = 0;index< classIndex.length;index++){

        for(int index = 0;index< classIndex.length;index++){
            HashMap<String, Double> wordOdds = new HashMap<String, Double>();
            c1=index;
            c2=classIndex[index];
            wordsProbC1 =  labelProbCount.get(c1);
            wordsProbC2 =  labelProbCount.get(c2);

//            printClassForeGroundProb(pixelProbC1, "numerator", index);
//            printClassForeGroundProb(pixelProbC2, "denominator", index);
            for(Map.Entry<String, Double> entry: wordsProbC1.entrySet()) {

                if(!wordsProbC2.containsKey(entry.getKey()))  continue;

                // getting only words which are common among the two classes and finding max odd op
                // 20 words with the highest log-odds ratio for that pair of classes.
                p1=wordsProbC1.get(entry.getKey());
                p2=wordsProbC2.get(entry.getKey());
                oddsProb = p1/p2;
                vl=Math.log(oddsProb);
                wordOdds.put(entry.getKey(), vl);
            }
            wordsOddPerClass.put(c1,wordOdds);
        }
    }

    private void printWordOddsPerClass() throws  IOException{

        HashMap<String, Double> ha = new HashMap<String, Double>();
        for(int i=0; i<LABEL_COUNT;i++){
            System.out.println("CLASS :"+i);
            File file1 = new File("/tmp/"+i+".csv");
            file1.createNewFile();
            FileWriter writer1 = new FileWriter(file1);
            ha = wordsOddPerClass.get(i);
            for(Map.Entry<String, Double> entry2 : ha.entrySet()) {
                writer1.write(entry2.getKey());
                writer1.write(",");
                writer1.write(String.format( "%.10f", entry2.getValue() ));
                writer1.write("\n");
            }
        }
    }

    private double max(double a, double b){
        if (Double.compare(a,b) > 1){
            return a;
        } else {
            return b;
        }
    }
    private void findMaxOddPairs(){
        double[] res= new double[LABEL_COUNT];
        int index=0;
        for(int i: classIndex){
            res[index] = confusionMatrix[index][classIndex[index]];
            index++;
        }

        Arrays.sort(res);
        System.out.println(Arrays.toString(res));
//        System.out.println(Arrays.toString(Arrays.copyOfRange(res, res.length,res.length-1)));
//        ArrayUtils.reverse(int[] array)
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
    private void predictClassificationExtra(String dataFile, String labelFile, String mapFile) throws IOException {
        predictClassification(readFileExtra(dataFile, labelFile, mapFile, "testParsed"));
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
            int index;
            int i;
            if ( EXTRA ){
                index = 1;
                i=1;
            } else {
                index = 0;
                i=0;
            }


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

    private  void printConfusionMatrix(boolean debug, int K){
        Double accuracy = 0.0;
        Double t ;
        if (debug){
            System.out.println("TOTAL TEST DOCUMENTS READ   " + totalTestDoc);
            System.out.println("\n\n*** CONFUSION MATRIX ***\n");
        }
//        for(int i=0; i<LABEL_COUNT; i++){
//            System.out.println("TOTAL TEST DOCUMENTS OF TYPE "+i+" READ : "+labelTestOccuranceCount.get(i));
//        }
        for (int i = 0 ;i < LABEL_COUNT; i++){
            if (!labelWordCount.containsKey(i)) continue;
            for (int j = 0 ; j < LABEL_COUNT ; j++){
                if (!labelWordCount.containsKey(j)) continue;
                t = confusionMatrix[i][j]/labelTestOccuranceCount.get(i)*100;
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
        double a = accuracy/LABEL_COUNT;
        if (debug){
            System.out.println("--------------------------");
            System.out.print("OVERALL ACCURACY :");
            System.out.format("%5.3f", a);
            System.out.println("%");
            System.out.println("---------------------------");
        } else {
            System.out.print(K+" ,");
            System.out.format("%5.3f", a);
            System.out.println();
        }

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
    private void findHighConfusion(){
//        int[] classIndex = new int[10];
        for(int i=0; i<confusionMatrix.length; i++){
            classIndex[i]=max(confusionMatrix[i],i);
        }
        System.out.println(Arrays.toString(classIndex));
//        return classIndex
    }


    public static  void main(String[] args) throws IOException {
        TextDocClassification nf;
        String testFile;
        System.out.println();
        System.out.println();

        for (int i=1; i<=1; i++){
            //////////                  EMAIL                            ///////////////////////////////
//            nf = new TextDocClassification("/Users/Sam/AI_MP/MP3/spam_detection/train_email_stemmed.txt",2, i);
//            testFile = "/Users/Sam/AI_MP/MP3/spam_detection/test_email_stemmed.txt";
//            System.out.println();
//            System.out.println();
//            System.out.println("**********************************************");
//            System.out.println("        EMAIL CLASSIFICATION                   ");
//            System.out.println("**********************************************");



            ////////////////////////     NEWS                                   ////////////
            nf = new TextDocClassification("/Users/Sam/AI_MP/MP3/8category/8category_training_stemmed.txt",8,i);
            testFile = "/Users/Sam/AI_MP/MP3/8category/8category_testing_stemmed.txt";
            System.out.println();
            System.out.println();
            System.out.println("**********************************************");
            System.out.println("        NEWS CLASSIFICATION                   ");
            System.out.println("**********************************************");
//
//
            nf.calcLabelProb();
            nf.predictClassification(testFile);
            nf.printConfusionMatrix(true, i);
//            nf.findHighConfusion();
//            nf.findOdds();
//            nf.findMaxOddPairs();
//            nf.printWordOddsPerClass();
//            nf.highestLikelihood();
//            cation(String dataFile, String labelFile, String mapFile,int laplace) throws IOException {
//            String testFile="newsParsedTrainFile.txt";
//            System.out.println();
//            System.out.println();
//            System.out.println("**********************************************");
//            System.out.println("        20 NEWS CLASSIFICATION                ");
//            System.out.println("**********************************************");
//
//            nf = new TextDocClassification("/Users/Sam/AI_MP/MP3/20news/train.data",
//                    "/Users/Sam/AI_MP/MP3/20news/train.label","/Users/Sam/AI_MP/MP3/20news/train.map",i);
//
//            nf.calcLabelProb();
//            nf.predictClassificationExtra("/Users/Sam/AI_MP/MP3/20news/test.data",
//                    "/Users/Sam/AI_MP/MP3/20news/test.label",
//                    "/Users/Sam/AI_MP/MP3/20news/test.map"
//            );
////            nf.predictClassification(testFile);
//            nf.printConfusionMatrix(true, i);
//            nf.findHighConfusion();
//            nf.findOdds();
//            nf.findMaxOddPairs();
//            nf.printWordOddsPerClass();
//            nf.highestLikelihood();

        }

    }
}
