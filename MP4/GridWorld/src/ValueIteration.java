import java.io.*;
import java.util.ArrayList;

import static java.lang.String.format;

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
//        Arrays.fill(utility, START_UTIL);
        setStartUtility(utility);
        findStatesAndCalcRewards();
    }

    private void setStartUtility(double[] utility){
        for (int s=0; s<utility.length; s++){
            if (inputMap[s].equals("1")) {
                utility[s] = 1;
            } else if (inputMap[s].equals("-1")){
                utility[s] = -1;
            } else if ( inputMap[s].equals("w") || inputMap[s].equals("0")){
                utility[s] = 0;
            }
        }
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
                System.out.print(inputMap[COL*i+j]+"         ");
            }
            System.out.println();
        }
    }

    private void printUtil(double[] utility){
        for(int i=0; i< ROW ;i++){
            for(int j=0; j< COL;j++){
                System.out.format("%10.4f", utility[COL * i + j]);
                System.out.print(" ");
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


    private boolean checkMove(int index, char moveDirection){
        // moveDirection : U D L R [ up down left right ]
        switch (moveDirection){
            case 'U':
                if ( (index - COL) < 0){
                    return false;
                }
                break;
            case 'D':
                if ( (index + COL) > (ROW*COL-1)){
                    return false;
                }
                break;
            case 'R':
                if ( (index + 1) % COL == 0 ){
                    return false;
                }
                break;
            case 'L':
                if ( index % COL == 0 ){
                    return false;
                }
                break;
        }
        return true;
    }

    private int setMove(int index, char moveDirection ) {
        if (checkMove(index, moveDirection)) {
            switch (moveDirection) {
                case 'U':
                    return index - COL;
                case 'D':
                    return index + COL;
                case 'R':
                    return index + 1;
                case 'L':
                    return index - 1;
            }
        }
        return index;
    }

    private double actionUtility(int index, int action){
        // action 0 1 2 3 : up left down right
        int temp1 = -1 , temp2 = -1 , temp3 = -1 ;
        double result;
        switch (action){
            case 0: // up : left & right
                temp3 = setMove(index, 'U');
                temp1 = setMove(index, 'L');
                temp2 = setMove(index, 'R');
                break;

            case 1: // left : down & up
                temp3 = setMove(index, 'L');
                temp1 = setMove(index, 'D');
                temp2 = setMove(index, 'U');
                break;

            case 2: // down : right & left
                temp3 = setMove(index, 'D');
                temp1 = setMove(index, 'R');
                temp2 = setMove(index, 'L');
                break;
            case 3:
                // right : up &down
                temp3 = setMove(index, 'R');
                temp1 = setMove(index, 'U');
                temp2 = setMove(index, 'D');
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
        double aptActionValue = -1 * Double.MIN_VALUE;
        int aptAction = -1;
        for(int action=0; action < 4 ; action++){
            double temp = actionUtility(stateIndex, action);
            if ( Double.compare(temp, aptActionValue) > 0 )  {
                aptActionValue =  temp;
                aptAction = action;
            }
        }
        return aptActionValue;
    }

    // write to file
    private void writeOp(){


    }

    private double[] calcValueIteration() throws IOException {
        String opFile = "mapChart.csv";
        File file1 = new File(opFile);
        file1.createNewFile();
        FileWriter writer1 = new FileWriter(file1);

        double[] preUtility;
        int maxIteration = 0;
        double temp = EPSILON * ( (1 - DISCOUNT_FACTOR) / DISCOUNT_FACTOR ) ;
        while (true){
            writer1.write(Integer.toString(maxIteration));
            writer1.write(",");

            preUtility = utility.clone();
            System.out.println("************** ITERATION MAX :" + maxIteration  + "********");
            printUtil(preUtility);
            System.out.println("=========================================================================");
            delta = 0;
            for(int s: states){
//                System.out.println("STATE :"+s);
//                if ( inputMap[s].equals("1") || inputMap[s].equals("-1") ) {
//                    continue;
//                }
                utility[s] = reward[s] + DISCOUNT_FACTOR * findMax(s);
                delta = Double.max(delta, Math.abs(utility[s] - preUtility[s]));
//                if ( Double.compare(utility[s] - preUtility[s], delta) > 0) {
//                    delta = utility[s] - preUtility[s];
//                }
            }
            for (double s: utility){
                writer1.write(Double.toString(s));
                writer1.write(",");
            }
            writer1.write("\n");
            if (Double.compare(delta, temp) < 0 ) {
                writer1.flush();
                writer1.close();
                return preUtility;
            }
            maxIteration++;
            if (maxIteration > 500){
                writer1.flush();
                writer1.close();
                return preUtility;
            }
        }

    }

    public static void main(String[] args) throws IOException {
        String fileName = "/Users/sam/AI_MP/MP4/GridWorld/files/map";
        ValueIteration vl = new ValueIteration(fileName);
        vl.printMap();
        System.out.println(" ** ");
        vl.printUtil(vl.calcValueIteration());
    }
}