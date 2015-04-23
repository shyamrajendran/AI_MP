
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
    private ArrayList<Integer> states = new ArrayList<Integer>();
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
        int index ;
        for(int i=0; i< ROW ;i++){
            for(int j=0; j< COL;j++){
                index = COL*i+j;
                if ( inputMap[index].equals("0")){
                    states.add(index);
                    reward[index] = REWARD;
                } else if (inputMap[index].equals("1")){
                    states.add(index);
                    reward[index] = 1;
                } else if (inputMap[index].equals("-1")) {
                    states.add(index);
                    reward[index] = -1;
                } else {
                    reward[index] = 0;
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

    private double actionUtility(int index, int action){
        // action 0 1 2 3 : up left down right
        int temp1 = -1 , temp2 = -1 , temp3 = -1 ;
        double result;
        switch (action){
            case 0:
                // see left and right
                temp1 = index - 1;
                temp2 = index + 1;
                temp3 = index - COL; // the main action.
                if ( temp1 % COL == 0 ) {
                    temp1 = index;
                }
                if ( (temp2+1) % COL == 0 ) {
                    temp2 = index;
                }
                if ( temp3 < 0 ) {
                    temp3 = index;
                }
                break;
            case 1:
                // see bottom and top
                temp3 = index - 1;
                temp1 = index + 6;
                temp2 = index - 6;
                if ( temp1 > (ROW*COL)-1 ) {
                    temp1 = index;
                }
                if ( temp2 < 0 ) {
                    temp2 = index;
                }
                if ( temp3 < 0 ) {
                    temp3 = index;
                }
                break;
            case 2:
                // see left and right
                temp3 = index - 6;
                temp1 = index - 1;
                temp2 = index + 1;

                if ( temp1 < 0 ) {
                    temp1 = index;
                }
                if ( (temp2+1) % COL ==  0 ) {
                    temp2 = index;
                }
                if ( temp3 < 0 ) {
                    temp3 = index;
                }
                break;
            case 3:
                // see top and bottom
                temp3 = index + 1;
                temp1 = index - 6;
                temp2 = index + 6;

                if ( temp1 < 0 ) {
                    temp1 = index;
                }
                if ( temp2 > (ROW*COL)+1 ) {
                    temp2 = index;
                }
                if ( (temp3 + 1) % COL == 0 ) {
                    temp3 = index;
                }
                break;

        }

        if ( inputMap[temp1].equals("W") ){
            temp1 = index;
        }
        if ( inputMap[temp2].equals("W")){
            temp2 = index;
        }
        if ( inputMap[temp3].equals("W")){
            temp3 = index;
        }

        result = utility[temp3] * INTENDED_PROB;
        result += utility[temp2] * OTHER_PROB;
        result += utility[temp1] * OTHER_PROB;
        return result;
    }

    private double findMax(int stateIndex){
        double aptActionValue = Double.MIN_VALUE;
        int aptAction = -1;
        for(int action=0; action < 4 ; action++){
            double temp = actionUtility(stateIndex, action);
            if ( Double.compare(temp, aptActionValue) > 1 )  {
                aptActionValue =  temp;
                aptAction = action;
            }
        }
        return aptActionValue;
    }
    private double[] calcValueIteration(){
        double[] preUtility;
        int maxIteration = 50;
        while (true){
            preUtility = utility.clone();
            delta = 0;
            for(int s: states){
                utility[s] = reward[s] + DISCOUNT_FACTOR * findMax(s);
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
        System.out.println(vl.calcValueIteration());
    }
}
