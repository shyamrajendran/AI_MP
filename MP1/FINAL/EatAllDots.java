import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by manshu on 2/21/15.
 */
public class EatAllDots {
    private class Maze {
        char data;
        boolean hasGoal;
        Maze(char c) {data = c; hasGoal = false;}
        Maze(char c, boolean b) {data = c; hasGoal = b;}

        public boolean containsGoal() {
            return hasGoal;
        }

        public void setGoal(boolean hasGoal) {
            this.hasGoal = hasGoal;
        }

        public char getData() {
            return data;
        }

        public void setData(char data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return String.valueOf(data);
        }
    }

    private Maze maze[][];

    private final char GOAL = '.', START = 'P', WALL = '%', MARK = '|', EMPTY = ' ', WIN = 'E';
    
    int row_num, col_num;
    Point start, goal;
    ArrayList<Point> goals;
    ArrayList<Point> goalsDone;
    int numGoals;

    public EatAllDots(String fileName) {
        try {
            readMaze(fileName);
        } catch (IOException ie) {
            System.out.println("Cannot read maze file. -> " + ie.getMessage());
            System.exit(0);
        }

        //System.out.println(start + " " + goal);
    }

    public void readMaze(String fileName) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(fileName));

        ArrayList<String> readMaze = new ArrayList<String>();
        goals = new ArrayList<Point>();
        goalsDone = new ArrayList<Point>();
        numGoals = 0;

        String line;
        while ((line = br.readLine()) != null) {
            readMaze.add(line);
        }
        if (readMaze.isEmpty()) throw new IOException("File not ok for search");
        row_num = readMaze.size();
        col_num = readMaze.get(0).length();
        maze = new Maze[row_num][col_num];

        int row = 0;
        for (String row_maze : readMaze) {
            int col = 0;
            for (int i = 0; i < row_maze.length(); i++) {
                maze[row][col] = new Maze(row_maze.charAt(i));
                if (maze[row][col].getData() == START) start = new Point(row, col);
                if (maze[row][col].getData() == GOAL) {
                    maze[row][col].setGoal(true);
                    goal = new Point(row, col);
                    goals.add(new Point(row, col, true));
                    numGoals++;
                }
                col++;
            }
            row++;
        }
        printAndCleanMaze();
    }


    private ArrayList<Point> getSuccessors(Point p) {
        ArrayList<Point> successors = new ArrayList<Point>(4);

        if (p.x - 1 >= 0 && maze[p.x - 1][p.y].getData() != WALL) successors.add(new Point(p.x - 1, p.y)); // North
        if (p.x + 1 < row_num && maze[p.x + 1][p.y].getData() != WALL) successors.add(new Point(p.x + 1, p.y)); //South
        if (p.y + 1 < col_num && maze[p.x][p.y + 1].getData() != WALL) successors.add(new Point(p.x, p.y + 1)); // East
        if (p.y - 1 >= 0 && maze[p.x][p.y - 1].getData() != WALL) successors.add(new Point(p.x, p.y - 1)); // West

//        for (Point next : successors) {
//            if (maze[next.x][next.y].containsGoal() && maze[next.x][next.y].getData() != WIN) next.setGoal(true);
//        }

        return successors;
    }

    private int getManhattanHeuristic(Point point, Point g) {
        return Math.abs(g.x - point.x) + Math.abs(g.y - point.y);
    }

    private int getManhattanHeuristic(Point point,  Set<Point> goalsDone) {
        int closest_dist = Integer.MAX_VALUE;
        
        for (Point gp : goals) {
            if (goalsDone.contains(gp)) continue;
            int dist = getManhattanHeuristic(point, gp);
            if (dist < closest_dist) {
                closest_dist = dist;
            }
        }    
        
        return closest_dist;
    }
    
    

    private void printAndCleanMaze() {
        System.out.println();
        for (int i = 0; i < row_num; i++) {
            for (int j = 0; j < col_num; j++) {
                if (start.equals(new Point(i, j))) maze[i][j].setData(START);
                if (maze[i][j].getData() == MARK) {
                    maze[i][j].setData(EMPTY);
                    System.out.print('.');
                } else {
                    System.out.print(maze[i][j]);
                }
                if (maze[i][j].getData() == MARK) maze[i][j].setData(EMPTY);
                if (maze[i][j].getData() == WIN) maze[i][j].setData(GOAL);
                if (!goalsDone.contains(new Point(i, j)) && goals.contains(new Point(i, j))) maze[i][j].setData(GOAL);
                if (start.equals(new Point(i, j))) maze[i][j].setData(START);
            }
            System.out.println();
        }
        System.out.println();
    }
    
    private AstarData[][] preprocessPaths(Point start) throws CloneNotSupportedException {
        AstarData[][] costs = new AstarData[numGoals + 1][numGoals + 1];
        for (int i = 1; i <= numGoals; i++) {
            costs[0][i] = generalAstarSearch(start, goals.get(i - 1));
        }
        for (int i = 1; i < numGoals; i++)
            for (int j = i + 1; j <= numGoals; j++) {
                costs[i][j] = generalAstarSearch(goals.get(i - 1), goals.get(j - 1));
            }
        
        for (int i = 0; i <= numGoals; i++) {
            for (int j = 0; j <= numGoals; j++) {
                if (i > j) {
                    List<Point> temp = new ArrayList<Point>();
                    for (Point p : costs[j][i].path)
                        temp.add(p.clone());
                    Collections.reverse(temp);
                    costs[i][j] = new AstarData(costs[j][i].cost, temp, costs[j][i].num_expanded);
                } else if (i == j) {
                    List<Point> temp = new ArrayList<Point>();
                    costs[i][j] = new AstarData(0, temp, 0);
                }
                //System.out.print(costs[i][j] + " ");//System.out.print(String.format("%02d", costs[i][j]) + " ");
            }
            //System.out.println();
        }
        
        return costs;
    }
    
    public AstarData generalAstarSearch() throws IOException, CloneNotSupportedException{
        AstarData[][] path_costs = preprocessPaths(start);
        Set<Integer> indexes_done = new HashSet<Integer>();
        
        int num_goals_found = 0;
        
        List<Point> goals_ordering = new LinkedList<Point>();
        List<Integer> goals_cost = new LinkedList<Integer>();
        
        int current_index = 0;
        while (num_goals_found != goals.size()) {
            indexes_done.add(current_index);
            int closest_goal_dist = Integer.MAX_VALUE;
            int closest_goal_index = -1; 
            for (int i = 0; i < path_costs.length; i++) {
                if (indexes_done.contains(i)) continue;
                if (closest_goal_dist > path_costs[current_index][i].cost) {
                    closest_goal_dist = path_costs[current_index][i].cost;
                    closest_goal_index = i;
                }
            }
            current_index = closest_goal_index;
            goals_ordering.add(goals.get(closest_goal_index - 1));
            goals_cost.add(closest_goal_dist);
            num_goals_found++;
        }

        //System.out.println(goals_ordering);
        //System.out.println(goals_cost);

        int sum_cost = 0;
        int counter = 49;
        for (int i = 0; i < goals_ordering.size(); i++) {
            Point p = goals_ordering.get(i);
            if (counter > 57 && counter < 65)
                counter = 65;
            if (counter > 91 && counter < 97)
                counter = 97;
            if (counter > 123)
                counter = '&';
            maze[p.x][p.y].setData((char)counter);
            if (counter < 123)
                counter++;
            sum_cost += goals_cost.get(i);
        }
        printAndCleanMaze();
        //printAndCleanMaze();

        int[][] costs = new int[path_costs.length][path_costs.length];
        int num_explored = 0;
        for (int i = 0; i < path_costs.length; i++)
            for (int j = 0; j < path_costs[i].length; j++) {
                costs[i][j] = path_costs[i][j].cost;
                if (j > i) {
                    num_explored += path_costs[i][j].num_expanded;
                }
            }

        System.out.println("Sum of the path cost when done subOptimally = " + sum_cost);
        System.out.println("Nodes explored suboptimally = " + num_explored);
        
        TSPForPacman tsp_pacman_problem = new TSPForPacman(costs);
        ArrayList<String> cities = tsp_pacman_problem.tsp_search();
        int tsp_num_explored = Integer.parseInt(cities.remove(cities.size() - 1));
        int tsp_cost = Integer.parseInt(cities.remove(cities.size() - 1));

        /***********************************************
         * 
         * 
         */
        ArrayList<Point> tsp_path = new ArrayList<Point>();
        
        for (String city : cities) {
            int i = Integer.parseInt(city);
            if (i == 0) {
                System.out.print(start + " ");
                tsp_path.add(start);
            } else {
                System.out.print(goals.get(i - 1) + " ");
                tsp_path.add(goals.get(i - 1));
            }
        }
        System.out.println();
        
        ArrayList<Point> final_path = correctPath(tsp_path, path_costs);

        counter = 48;
        for (int i = 0; i < final_path.size(); i++) {
            Point p = final_path.get(i);
            if (counter > 57 && counter < 65)
                counter = 65;
            if (counter > 91 && counter < 97)
                counter = 97;
            if (counter > 123)
                counter = '&';
            maze[p.x][p.y].setData((char)counter);
            if (counter < 123)
                counter++;
        }
        printAndCleanMaze();

        System.out.println("TSP nodes explored = " + tsp_num_explored);
        System.out.println("TSP Cost = " + tsp_cost);
        
        return new AstarData(tsp_cost, final_path, tsp_num_explored + num_explored);
    }
    
    private AstarData generalAstarSearch(Point p1, Point p2) {
        Point start = p1;
        Point goal = p2;
        
        Node<Point> start_node = new Node<Point>(start, null);

        Set<Node<Point>> explored_set = new HashSet<Node<Point>>();
        
        //PriorityQueue<Node<Point>> frontier = new PriorityQueue<Node<Point>>();
        PriorityQueueWrapper<Node<Point>> frontier = new PriorityQueueWrapper<Node<Point>>("Priority");

        frontier.add(start_node);
               
        Node<Point> current_node = null;
        int num_explored = 0;
        boolean goal_found = false;
        
        while (!frontier.isEmpty()) {
            current_node = frontier.poll();
            if (explored_set.contains(current_node)) continue;
            num_explored++;
            
            if (current_node.item.equals(goal)) {
                maze[current_node.item.x][current_node.item.y].setData(WIN);
                num_explored--;
                goal_found = true;
                break;
            }
            explored_set.add(current_node);
            
            for (Point p : getSuccessors(current_node.item)) {
                Node<Point> neighbor = new Node<Point>(p, current_node);
                if (explored_set.contains(neighbor)) continue;
                
                neighbor.current_cost = current_node.current_cost + 1;
                neighbor.heuristic_cost = getManhattanHeuristic(neighbor.item, goal);
                neighbor.compared_cost = neighbor.current_cost + neighbor.heuristic_cost;
                
                if (frontier.contains(neighbor)) {
                    if (frontier.getContent(neighbor).compared_cost > neighbor.compared_cost) {
                        frontier.remove(neighbor);
                        frontier.add(neighbor);
                    }
                } else {
                    frontier.add(neighbor);
                }
            }
        }
        
        List<Point> actions = null;
        int final_cost = -1;
        if (goal_found) {
            actions = new ArrayList<Point>();
            final_cost = current_node.current_cost;
            while (current_node != null && current_node.parent != null) {
                actions.add(current_node.item);
                current_node = current_node.parent;
            }
            if (current_node != null && current_node.parent == null) actions.add(start); 
            Collections.reverse(actions);
            
        } else {
            System.out.println(start + " " + goal);
            System.out.println("No Goal Found");
            System.exit(0);
        }

        //System.out.println("Final Cost = " + final_cost);
        //System.out.println("Num Explored = " + num_explored);
        
        for (Point p : actions) {
            //System.out.print(p + " ");
            maze[p.x][p.y].setData(MARK);
        }
        //System.out.println();
        maze[goal.x][goal.y].setData(WIN);
        
        //printAndCleanMaze();
        
        AstarData return_data = new AstarData(final_cost, actions, num_explored);
        return return_data;
        
    }

    private boolean containsGoalAhead(ArrayList<Point> list, int start, Point p) {
        for (int i = start; i < list.size(); i++) 
            if (list.get(i).equals(p)) return true;
        return false;
    }
    
    private ArrayList<Point> correctPath(ArrayList<Point> tsp_path, AstarData[][] computed_costs) {
        Set<Point> goals_removed = new HashSet<Point>();
        ArrayList<Point> final_paths = new ArrayList<Point>();
        
        Map<Point, Integer> goal_index = new HashMap<Point, Integer>();
        int counter = 0;
        goal_index.put(start, counter++);
        for (Point p : goals) {
            goal_index.put(p, counter++);
        }
        
        for (int i = 0; i < tsp_path.size(); i++) {
            if (goals_removed.contains(tsp_path.get(i))) continue;
            
            int j = i + 1;
            while (j < tsp_path.size()) {
                if (goals_removed.contains(tsp_path.get(j))) {
                    j++;
                } else {
                    break;
                }
            }
            // here j contains goal
            final_paths.add(tsp_path.get(i));
            
            if (j != tsp_path.size()) {
                Point temp_start = tsp_path.get(i);
                Point temp_goal = tsp_path.get(j);
                List<Point> temp_path = computed_costs[goal_index.get(temp_start)][goal_index.get(temp_goal)].path;
                for (Point path_loc : temp_path) {
                    if (containsGoalAhead(tsp_path, j + 1, path_loc)) {
                        final_paths.add(path_loc);
                        goals_removed.add(path_loc);
                    }
                }
            }
        }
        return final_paths;
    }
    
    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        String maze_file = "trickySearch.txt";// "smallSearch.txt";
        if (args.length >= 1)
            maze_file = args[0];

        EatAllDots mazeSearch = new EatAllDots(maze_file);
        System.out.println(mazeSearch.generalAstarSearch());
    }
}
