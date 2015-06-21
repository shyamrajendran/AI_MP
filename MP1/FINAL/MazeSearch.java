import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by manshu on 2/6/15.
 */

public class MazeSearch {
    private class MazeState {
        char data;
        boolean hasGoal;
        MazeState(char c) {data = c; hasGoal = false;}
        MazeState(char c, boolean b) {data = c; hasGoal = b;}

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
    
    private MazeState maze[][];
    
    private final char GOAL = '.', START = 'P', WALL = '%', MARK = '|', EMPTY = ' ', WIN = 'E';
    private final int screen_x = 600, screen_y = 600;
    private PacmanGrid pg;
    
    int row_num, col_num;
    Point start, goal;
    ArrayList<Point> goals;
    ArrayList<Point> goalsDone;
    int numGoals;
    
    public MazeSearch(String fileName) {
        try {
            readMaze(fileName);    
        } catch (IOException ie) {
            System.out.println("Cannot read maze file. -> " + ie.getMessage());
            System.exit(0);
        }
        
        pg = new PacmanGrid("Test", screen_x, screen_y, row_num, col_num);
        for (int i = 0; i < row_num; i++)
            for (int j = 0; j < col_num; j++) {
                if (maze[i][j].data == WALL) pg.setWall(i, j);
                if (maze[i][j].data == GOAL) pg.setGoal(i, j);
            }
        pg.setPacmanLoc(start.x, start.y);
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
        maze = new MazeState[row_num][col_num];
        
        int row = 0;
        for (String row_maze : readMaze) {
            int col = 0;
            for (int i = 0; i < row_maze.length(); i++) {
                maze[row][col] = new MazeState(row_maze.charAt(i));
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
        System.out.println("Given maze is ");
        printAndCleanMaze();
    }
    
    private ArrayList<Point> getSuccessors(Point p) {
        ArrayList<Point> successors = new ArrayList<Point>(4);

        if (p.x - 1 >= 0 && maze[p.x - 1][p.y].getData() != WALL) successors.add(new Point(p.x - 1, p.y)); // North
        if (p.x + 1 < row_num && maze[p.x + 1][p.y].getData() != WALL) successors.add(new Point(p.x + 1, p.y)); //South
        if (p.y + 1 < col_num && maze[p.x][p.y + 1].getData() != WALL) successors.add(new Point(p.x, p.y + 1)); // East
        if (p.y - 1 >= 0 && maze[p.x][p.y - 1].getData() != WALL) successors.add(new Point(p.x, p.y - 1)); // West

        for (Point next : successors) {
            if (maze[next.x][next.y].containsGoal() && maze[next.x][next.y].getData() != WIN) next.setGoal(true);
        }
        
        return successors;
    }

    private <T extends Comparable<T>> T searchQueue(PriorityQueueWrapper<T> queue, T item) {
        Iterator<T> iterator = queue.iterator();
        while (iterator.hasNext()) {
            T current = iterator.next();
            if (current.equals(item)) return current;
        }
        return null;
    }
    
    private String getPriorityType(String searchStrategy) {
        if (searchStrategy.equals("dfs"))
            return "Stack";
        else if (searchStrategy.equals("bfs"))
            return "Queue";
        else
            return "Priority";
    }

    private void initCost(String searchStrategy, Node<Point> node) {
        if (searchStrategy.equals("uniform") || searchStrategy.equals("bfs") || searchStrategy.equals("dfs"))
            node.compared_cost = node.current_cost; 
        else if (searchStrategy.equals("greedy")) 
            node.compared_cost = node.heuristic_cost;
        else if (searchStrategy.equals("astar"))
            node.compared_cost = node.current_cost + node.heuristic_cost;
        return;
    }
    
    private int getManhattanHeuristic(Point point, Point g) {
        return Math.abs(g.x - point.x) + Math.abs(g.y - point.y);
    }
    public int searchPath(String searchStrategy){
        return searchPath(searchStrategy, null, null, true);
    }
    
    private int searchPath(String searchStrategy, Point p1, Point p2, boolean draw) {
        PriorityQueueWrapper<Node<Point>> frontier = new PriorityQueueWrapper<Node<Point>>(getPriorityType(searchStrategy));
        
        Point s1 = p1, g1 = p2;
        
        if (p1 == null && p2 == null) {
            s1 = start; g1 = goal;        
        } else if (p1 == null) {s1 = start;}
        else if (p2 == null) {g1 = goal;}
        
        frontier.add(new Node<Point>(s1, null));
        if (draw) pg.setPacmanLoc(s1.x, s1.y);
        
        HashSet<Node<Point>> explored_set = new HashSet<Node<Point>>();

        int numNodesExplored = 0;
        int goalsToBeFound = 1;

        Node<Point> current = null;
        while (!frontier.isEmpty()) {
            current = frontier.remove();
            numNodesExplored++;

            if (current.item.equals(g1)) {
                maze[current.item.x][current.item.y].setData(WIN);
                numNodesExplored--;
                goalsToBeFound--;
                break;
            }

            explored_set.add(current);
            
            
            if (draw && !current.item.equals(start) && !goals.contains(current.item))
                maze[current.item.x][current.item.y].setData(MARK);
            
            ArrayList<Point> successors = getSuccessors(current.item);
            for (Point neighbor : successors) {
                if (explored_set.contains(new Node<Point>(neighbor, null))) continue;
                Node<Point> node = new Node<Point>(neighbor, current);
                node.current_cost = current.current_cost + 1;
                node.heuristic_cost = getManhattanHeuristic(neighbor, g1);
                initCost(searchStrategy, node);
                Node<Point> temp = searchQueue(frontier, node);
                if (temp == null)
                    frontier.add(node);
                else if (temp.compared_cost > node.compared_cost) {
                    frontier.update(temp, node);
                }
            }
        }
        System.out.println("Num nodes explored = " + numNodesExplored);
        
        if (draw) printAndCleanMaze();
        
        int path_cost = 0;
        if (goalsToBeFound == 0) maze[g1.x][g1.y].setData(WIN);
        ArrayList<Point> actions = new ArrayList<Point>();

        if (current != null && g1.equals(current.item)) {
            while (current.parent != null) {
                actions.add(current.item);
                current = current.parent;
                path_cost++;
                if (current.parent == null) break;
                if (draw) maze[current.item.x][current.item.y].setData(MARK);
            }
        }
        actions.add(start);
        Collections.reverse(actions);

        System.out.println("Path Cost = " + path_cost);
        System.out.println("Now wait for printing of actual path");

        //Point p = start;
        if (draw) {
            for (Point p : actions) {
                pg.setPacmanLoc(p.x, p.y);
            }
            pg.delay(1000);
            pg.clearAll();
            printAndCleanMaze();
        }

        return path_cost;
    }

    private int getFoodHeuristic(Point point) {
        ArrayList<Integer> distances = new ArrayList<Integer>(goals.size());
        for (Point goal : goals) {
            if (maze[goal.x][goal.y].data == WIN) continue;
            int dist = Math.abs(goal.x - point.x) + Math.abs(goal.y - point.y);
            distances.add(dist);
            //distances.add(searchPath("bfs", point, goal, false));
        }
        return Collections.min(distances);
    }
    
    private void printAndCleanMaze() {
        System.out.println();
        for (int i = 0; i < row_num; i++) {
            for (int j = 0; j < col_num; j++) {
                if (start.equals(new Point(i, j))) maze[i][j].setData(START);
                if (maze[i][j].getData() == MARK) {
                    maze[i][j].setData(EMPTY);
                    System.out.print(GOAL);
                } else {
                    System.out.print(maze[i][j]);
                }
                if (maze[i][j].getData() == WIN) maze[i][j].setData(GOAL);
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        //String maze_file = "smallMaze.txt";
//        if (args.length >= 1)
//            maze_file = args[0];

        String[] mazes = {"smallMaze.txt", "mediumMaze.txt", "bigMaze.txt"};

        for (String maze_file : mazes) {

            MazeSearch mazeSearch = new MazeSearch(maze_file);

            System.out.println("Doing DFS Search");
            mazeSearch.searchPath("dfs");  // 38, 210,
            System.out.println();

            System.out.println("Doing BFS Search");
            mazeSearch.searchPath("bfs");
            System.out.println();  //93, 271, 623

            System.out.println("Doing Greedy Search");
            mazeSearch.searchPath("greedy"); //39, 158-153, 455-209
            System.out.println();

            System.out.println("Doing UCS Search");
            mazeSearch.searchPath("uniform");
            System.out.println();

            System.out.println("Doing ASTAR Search");
            mazeSearch.searchPath("astar"); //54, 222-69, 549-209
            System.out.println();
        }
        
        System.exit(0);
    }
}
