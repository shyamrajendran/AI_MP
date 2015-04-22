
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sam on 4/22/15.
 */
public class ValueIteration {

    private String[] inputMap = new String[100];
    private double[] utility ;
    private double[] reward;
    private ArrayList<Integer> states;
    private double delta ;
    private final int START_UTIL = 0;
    private int ROW = 0;
    private int COL = 0;
    private final double DISCOUNT_FACTOR = 0.99;
    private final double EPSILON = 0.001;
    private final double REWARD = -0.4;
    private final double INTENDED_PROB = 0.8;
    private final double OTHER_PROB = 0.1;

    public ValueIteration(String fileName) throws IOException {
        readFile(fileName);
        utility = new double[ROW*COL];
        reward = new double[ROW*COL];
        Arrays.fill(utility, START_UTIL);
        findStatesAndCalcRewards();

    }


    private void findStatesAndCalcRewards(){
        for(int i=0; i< ROW ;i++){
            for(int j=0; j< COL;j++){
                if ( inputMap[COL*i+j].equals("0")){
                    states.add(COL*i+j);
                    reward[COL*i+j] = REWARD;
                } else if (inputMap[COL*i+j].equals("1")){
                    reward[COL*i+j] = 1;
                } else if (inputMap[COL*i+j].equals("-1")) {
                    reward[COL*i+j] = -1;
                } else {
                    reward[COL*i+j] = 0;
                }
            }
        }
    }
    private void printMap(){
        for(int i=0; i< ROW ;i++){
            for(int j=0; j< COL;j++){
                System.out.print(inputMap[COL*i+j]+" ");
            }
            System.out.println();
        }
    }

    private void readFile(String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        String in_line;
        String[] perLine;
        int index = 0;
        while((in_line = bufferedReader.readLine()) != null) {
            perLine = in_line.split(" ");
            for(String s: perLine){
                inputMap[index] = s;
                index++;
            }
            if ( ROW == 0) {
                COL = index;
            }
            ROW++;
        }
    }

    private double findMax(){
        double result = 0;

        return result;
    }

    private double[] calcValueIteration(){
        double[] preUtility;
        int maxIteration = 50;
        while (true){
            preUtility = utility.clone();
            delta = 0;
            for(int s: states){
                utility[s] = reward[s] + DISCOUNT_FACTOR * findMax();
                if ( delta < EPSILON * (1 - DISCOUNT_FACTOR) / DISCOUNT_FACTOR){
                    return preUtility;
                }
            }
            maxIteration--;
            if (maxIteration < 0){
                return preUtility;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String fileName = "/Users/sam/AI_MP/MP4/GridWorld/files/map";
        ValueIteration vl = new ValueIteration(fileName);
        vl.printMap();
    }
}
