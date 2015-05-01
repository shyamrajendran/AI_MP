import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by manshu on 4/30/15.
 */

class State {
    int x;
    int y;
    public State(int y, int x) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return 31 * y + x;
    }

    @Override
    public boolean equals(Object obj) {
        State state = (State) obj;
        if (state.x == this.x && state.y == this.y)
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "State{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

class GridLocation {
    int x;
    int y;
    double reward;
    boolean isWall;
    public GridLocation(int y, int x, double reward, boolean isWall) {
        this.x = x; this.y = y; this.reward = reward; this.isWall = isWall;
    }

    @Override
    public int hashCode() {
        return 31 * y + x;
    }
}

enum Action {
    NORTH,
    SOUTH,
    WEST,
    EAST
}

public class TDQLearning {
    public static final int NE = 10;
    public static final double DISCOUNT_FACTOR = 0.99;
    public static final double RPLUS = 1.0;
    public static final double LEARNING_THRESHOLD = 0.01;

    int time_step = 0;
    GridLocation[][] grid = null;
    State start_state;
    Map<State, Map<Action, Double>> qsa;
    Map<State, Map<Action, Integer>> nsa;


    private State move(State state, Action action) {
        State new_state = null;
        switch (action) {
            case NORTH:
                if ((state.y - 1) >= 0) {
                    new_state = new State(state.y - 1, state.x);
                }
                break;
            case SOUTH:
                if ((state.y + 1) < grid.length) {
                    new_state = new State(state.y + 1, state.x);
                }
                break;
            case WEST:
                if ((state.x - 1) >= 0) {
                    new_state = new State(state.y, state.x - 1);
                }
                break;
            case EAST:
                if ((state.x + 1) < grid[0].length) {
                    new_state = new State(state.y, state.x + 1);
                }
                break;
        }
        if (new_state != null) {
            if (grid[new_state.y][new_state.x].isWall)
                return state;
            return new_state;
        }
        return state;
    }

    private Action perpendicular(Action action, boolean isLeftPerpendicular) {
        switch (action) {
            case NORTH:
                if (isLeftPerpendicular)
                    return Action.WEST;
                else
                    return Action.EAST;
            case SOUTH:
                if (isLeftPerpendicular)
                    return Action.EAST;
                else
                    return Action.WEST;
            case EAST:
                if (isLeftPerpendicular)
                    return Action.NORTH;
                else
                    return Action.SOUTH;
            case WEST:
                if (isLeftPerpendicular)
                    return Action.SOUTH;
                else
                    return Action.NORTH;
        }
        return action;
    }

    private State getProbableState(State state, Action action) {
        double val = Math.random();
        State next_state;
        if (val <= 0.8) {
            next_state = move(state, action);
        } else if (val > 0.8 && val <= 0.9) {
            next_state = move(state, perpendicular(action, true)); // left
        } else {
            next_state = move(state, perpendicular(action, false)); // right
        }
        return next_state;
    }

    public void readConfig(String file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        int line_num = 0;
        List<String[]> lines = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            String[] line_state = line.split(" ");
            lines.add(line_state);
        }
        grid = new GridLocation[lines.size()][lines.get(0).length];
        for (int l = 0; l < lines.size(); l++) {
            for (int i = 0; i < lines.get(l).length; i++) {
                String data = lines.get(l)[i];
                if (data.equalsIgnoreCase("S"))
                    start_state = new State(l, i);
                switch (data) {
                    case "1":
                        grid[l][i] = new GridLocation(l, i, 1.0, false);
                        break;
                    case "-1":
                        grid[l][i] = new GridLocation(l, i, -1.0, false);
                        break;
                    case "W":
                        grid[l][i] = new GridLocation(l, i, Integer.MIN_VALUE, true);
                        break;
                    default:
                        grid[l][i] = new GridLocation(l, i, -0.04, false);
                        break;
                }
            }
        }

        qsa = new HashMap<>();
        nsa = new HashMap<>();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                State state = new State(j, i);
                qsa.put(state, new HashMap<>());
                nsa.put(state, new HashMap<>());

                for (Action action : Action.values()) {
                    qsa.get(state).put(action, 0.0);
                    nsa.get(state).put(action, 0);
                }
            }
        }
    }

    public Map<State, Map<Action, Double>> getQsaClone() {
        Map<State, Map<Action, Double>> new_qsa = new HashMap<>();
        for (State state : qsa.keySet()) {
            new_qsa.put(state, new HashMap<>());
            for (Action action : Action.values()) {
                new_qsa.get(state).put(action, qsa.get(state).get(action));
            }
        }
        return new_qsa;
    }

    private Action tdMax(State state) {
        double max = Double.NEGATIVE_INFINITY;
        List<Action> max_actions = new ArrayList<>();
        Action retAction = Action.NORTH;
        for (Action a : Action.values()) {
            double val;
            if (nsa.get(state).get(a) < NE) {
                val = RPLUS;
            } else {
                val = qsa.get(state).get(a);
            }
            if (val > max) {
                max = val;
                max_actions = new ArrayList<>();
                max_actions.add(a);
            }
            if (val == max) {
                max_actions.add(a);
            }
        }
        Random random = new Random();
        retAction = max_actions.get(random.nextInt(max_actions.size()));
        return retAction;
    }

    private double getMaxQValue(State state) { // send new state
        double max = Double.NEGATIVE_INFINITY;
        for (Action a : Action.values()) {
            max = Math.max(max, qsa.get(state).get(a));
        }
        return max;
    }

    private double getAlpha(int time_step) {
        return 60.0 / (59.0 + time_step);
    }

    private boolean hasConverged(Map<State, Map<Action, Double>> qsa1, Map<State, Map<Action, Double>> qsa2) {
        for (State state : qsa1.keySet()) {
            for (Action action : Action.values()) {
                if (Math.abs(qsa1.get(state).get(action) - qsa2.get(state).get(action)) >= LEARNING_THRESHOLD)
                    return false;
                if (!grid[state.y][state.x].isWall && qsa2.get(state).get(action) < 85)
                    return false;
//                if (nsa.get(state).get(action) < NE)
//                    return false;
            }
        }
        return true;
    }

    public void tdLearning() throws IOException {
        Map<State, Map<Action, Double>> old_qsa;
        int num_runs = 0;
        PrintWriter printWriter = new PrintWriter("out.csv");
        State state = start_state;
        while (true) {
            time_step = 0;
            old_qsa = getQsaClone();
            tdLearning(state, num_runs);
            num_runs++;
            System.out.println("Num Runs = " + num_runs);
            printWriter.write(num_runs + ", ");
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[row].length; col++) {
                    printWriter.write(getMaxQValue(new State(row, col)) + ",");
                }
            }
            printWriter.write("\n");
            if (hasConverged(old_qsa, qsa))
                break;
            Random random = new Random();
//            int y = random.nextInt(6);
//            int x = random.nextInt(6);
            int num = random.nextInt(36);
            state = new State(num / grid.length, num % grid[0].length);
            if (num_runs >= 10000)
                break;
        }
        printWriter.close();
    }

    public void tdLearning(State state, int numRun) {
        for (int i = 0; i < 10000; i++) {
            if (grid[state.y][state.x].isWall ) {
                if (time_step != 0) {
                    System.out.println("Haaw how is this possible ");
                    System.exit(0);
                }
                return;
            }
            Action action = tdMax(state);
            State next_state = getProbableState(state, action);

            double old_qsa = qsa.get(state).get(action);
            double rs = grid[state.y][state.x].reward;
            double maxqsa = getMaxQValue(next_state);

            double new_qsa = old_qsa + getAlpha(numRun) * (rs + (DISCOUNT_FACTOR * maxqsa) - old_qsa);

            qsa.get(state).put(action, new_qsa);

            int old_nsa = nsa.get(state).get(action);
            nsa.get(state).put(action, old_nsa + 1);

            time_step++;

            state = next_state;
        }
    }

//    public void tdLearnings(State state) {
//        if (grid[state.y][state.x].isWall ) {
//            if (time_step != 0) {
//                System.out.println("Haaw how is this possible ");
//                System.exit(0);
//            }
//            return;
//        }
//        Action action = tdMax(state);
//        State next_state = getProbableState(state, action);
//
//        double old_qsa = qsa.get(state).get(action);
//        double rs = grid[state.y][state.x].reward;
//        double maxqsa = getMaxQValue(next_state);
//
//        double new_qsa = old_qsa + getAlpha(time_step) * (rs + (DISCOUNT_FACTOR * maxqsa) - old_qsa);
//
//        //System.out.println(state + ":" + old_qsa + "->" + new_qsa);
//
//        if (time_step > 10000)
//            return;
//
//        qsa.get(state).put(action, new_qsa);
//
//        int old_nsa = nsa.get(state).get(action);
//        nsa.get(state).put(action, old_nsa + 1);
//
//        time_step++;
//
//        tdLearning(next_state);
//    }


    public static void main(String[] args) throws IOException {
        String file = "/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/files/map";
        TDQLearning tdqLearning = new TDQLearning();
        tdqLearning.readConfig(file);
        tdqLearning.tdLearning();
    }
}
