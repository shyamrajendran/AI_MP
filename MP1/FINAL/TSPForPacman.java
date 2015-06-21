import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by manshu on 2/22/15.
 */
public class TSPForPacman {

    private int num_cities;
    private String[] cities;
    private HashMap<String, Integer> index_map;
    private int[][] cost_city;

    private void readFile(String file_name) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file_name));
        num_cities = Integer.parseInt(br.readLine());
        cost_city = new int[num_cities][num_cities];
        index_map = new HashMap<String, Integer>();

        cities = br.readLine().split(" ");
        int city_num = 0;
        for (String city : cities) {
            index_map.put(city, city_num++);
            System.out.print(city + " ");
        }
        System.out.println();
        for (int i = 0; i < num_cities; i++) {
            String line = br.readLine();
            String[] costs = line.split(" ");
            int col = 0;
            for (String s : costs)
                cost_city[i][col++] = Integer.parseInt(s);
        }

        System.out.println(num_cities);
        for (int i = 0; i < num_cities; i++){
            for (int j = 0; j < num_cities; j++) {
                System.out.print(cost_city[i][j] + " ");
            }
            System.out.println();
        }
    }

    public TSPForPacman(String file_name) throws IOException{
        readFile(file_name);
    }

    public TSPForPacman(int[][] goal_costs) throws IOException{
        num_cities = goal_costs.length;
        cost_city = goal_costs;
        index_map = new HashMap<String, Integer>();
        cities = new String[num_cities];
        
        int city_num = 0;
        for (int i = 0; i < num_cities; i++) {
            cities[i] = String.valueOf(i);
            index_map.put(cities[i], city_num++);
        }

//        System.out.println(num_cities);
//        for (int i = 0; i < num_cities; i++){
//            for (int j = 0; j < num_cities; j++) {
//                System.out.print(cost_city[i][j] + " ");
//            }
//            System.out.println();
//        }
        
    }

    private class State implements Comparable<State>{
        String city;
        Integer edge_cost;
        Integer heuristic_cost;
        Integer cost;
        List<String> parent;
        //        State(String s, String p, int i){
//            city = s;
//            parent = new LinkedList<String>();
//            parent.add(p);
//            cost = i;
//        }
        State(String s, List<String> p, int i){
            city = s;
            parent = p;
            edge_cost = i;
            cost = i;
        }

        @Override
        public int compareTo(State o)
        {
            return this.cost.compareTo(o.cost);
        }

        @Override
        public boolean equals(Object obj) {
            State other = (State) obj;
            if (other.parent.size() != this.parent.size()) return false;

            if (!this.city.equals(other.city))// || this.cost != other.cost)
                return false;
            Set<String> parent1 = new HashSet<String>(this.parent);
            Set<String> parent2 = new HashSet<String>(other.parent);
            if (!parent1.containsAll(parent2)) return false;
//            Iterator iterator1 = parent.iterator();
//            Iterator iterator2 = other.parent.iterator();
//            while(iterator1.hasNext()) {
//                if (!iterator1.next().equals(iterator2.next()))
//                    return false;
//            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hashcode = 0;
            if (parent != null)
                for (String s : parent)
                    hashcode = s.hashCode() + 31 * hashcode;
            return 31 * 31 * hashcode + 31 * city.hashCode() + cost.hashCode();
        }

        @Override
        public String toString(){
            StringBuilder s = new StringBuilder();
            s.append(city).append(" ").append(cost);
            if (parent == null) return s.toString();
            for (String prt : parent)
                s.append(" ").append(prt);
            return s.toString();
        }
    }

    private int getHeuristic(State state) {
        long t1 = System.currentTimeMillis();

        ArrayList<String> goals = new ArrayList<String>();
        for (String city : cities) {
            if (state.parent == null || (!state.parent.contains(city) && !state.city.equals(city)))
                goals.add(city);
        }
        String start = state.city;
        if (state.parent != null) start = state.parent.get(0);

        int closest_dist_current = Integer.MAX_VALUE;
        int closest_dist_start = Integer.MAX_VALUE;
        
        for (String goal : goals) {
            if (goal.equals(state.city)) continue;
            int dist = cost_city[index_map.get(state.city)][index_map.get(goal)];
            if (dist < closest_dist_current)
                closest_dist_current = dist;
            dist = cost_city[index_map.get(start)][index_map.get(goal)];
            if (dist < closest_dist_start)
                closest_dist_start = dist;
        }
        if (closest_dist_current == Integer.MAX_VALUE) closest_dist_current = 0;
        if (closest_dist_start == Integer.MAX_VALUE) closest_dist_start = 0;

        PriorityQueue<Edge> queue = new PriorityQueue<Edge>();
        for (int i = 0; i < goals.size() - 1; i++)
            for (int j = i + 1; j < goals.size(); j++) {
                queue.add(new Edge(goals.get(i), goals.get(j), cost_city[index_map.get(goals.get(i))][index_map.get(goals.get(j))]));
            }
        DisjointSet<String> disjointSet = new DisjointSet<String>(goals.size());
        for (String goal : goals)
            disjointSet.makeSet(goal);

        HashSet<String> verticesInMST = new HashSet<String>();

        int spanning_cost = 0;
        int num_edges = 0;
        while (!queue.isEmpty()) {
            Edge edge = queue.poll();
            if (!verticesInMST.contains(edge.src)) {
                verticesInMST.add(edge.src);
            }
            if (!verticesInMST.contains(edge.dest)) {
                verticesInMST.add(edge.dest);
            }
            if (disjointSet.inSameSet(edge.src, edge.dest)) {
                //System.out.println("\nCycle");
                continue;
            }
            disjointSet.union(edge.src, edge.dest);
            num_edges++;
            spanning_cost += edge.weight;
            if (num_edges == (goals.size() - 1)) break;
        }
        
        //System.out.println("Parents = " + state.parent + " City = " + state.city);
        //System.out.println("Spanning Cost = " + spanning_cost + " Closest Dist current = " + closest_dist_current);
        //System.out.println("HN = " + (closest_dist_current + spanning_cost));// + closest_dist_start));

        long t2 = System.currentTimeMillis();
        //System.out.println("Time taken for heuristic = " + (t2 - t1));
        return closest_dist_current + spanning_cost;// + closest_dist_start;
    }

    public ArrayList<String> tsp_search() {
        String start = cities[0];
        HashSet<State> explored_state = new HashSet<State>();

        PriorityQueueWrapper<State> queue = new PriorityQueueWrapper<State>("Priority");
        State start_state = new State(start, null, 0);
        start_state.heuristic_cost = getHeuristic(start_state);
        start_state.cost = start_state.heuristic_cost + start_state.edge_cost;

        queue.add(start_state);

        int num_states_explored = 0;

        State current = null;
        while (!queue.isEmpty()) {
            long t1 = System.currentTimeMillis();
            current = queue.poll();
            //System.out.println(current);
            if (current.parent != null && (current.parent.size() == (cities.length - 1))) break;
            
            if (explored_state.contains(current)) continue;
            explored_state.add(current);
            num_states_explored++;
            
            List<String> parents = current.parent;
            for (String city : cities) {
                if (city.equals(current.city)) continue;
                if (parents != null && parents.contains(city)) {
                        continue;
                }
                List<String> new_parents = new LinkedList<String>();
                if (parents != null)
                    for (String parent : parents)
                        new_parents.add(parent);
                new_parents.add(current.city);
                int gn = current.edge_cost + cost_city[index_map.get(current.city)][index_map.get(city)];
                State new_state = new State(city, new_parents, gn);
                new_state.heuristic_cost = getHeuristic(new_state);
                new_state.cost = new_state.heuristic_cost + new_state.edge_cost;
                if (explored_state.contains(new_state)) continue;
                
                if (queue.contains(new_state)) {
                    State temp = queue.getContent(new_state);
                    if ( temp != null && temp.cost > new_state.cost) {
                        queue.remove(new_state);
                        queue.add(new_state);
                    } else if (temp == null) {
                        queue.add(new_state);
                    }
                } else {
                    queue.add(new_state);
                }
            }
            long t2 = System.currentTimeMillis();
            //System.out.println("Time taken for each iteration " + (t2 - t1));
        }
        int cost = current.edge_cost;
        current.parent.add(current.city);
        
//        System.out.println("TSP nodes explored = " + num_states_explored);
//        System.out.println("TSP Cost = " + cost);
        
        ArrayList<String> tsp_path = new ArrayList<String>(current.parent);
        tsp_path.add(String.valueOf(cost));
        tsp_path.add(String.valueOf(num_states_explored));
        return tsp_path;
    }
    public static void main(String[] args) throws IOException {
        String file_name = "TSPsample.txt";
        file_name = "small_pacman.tsp";
        TSPForPacman problem = new TSPForPacman(file_name);
        ArrayList<String> cities = problem.tsp_search();
        int num_explored = Integer.parseInt(cities.remove(cities.size() - 1));
        int cost = Integer.parseInt(cities.remove(cities.size() - 1));
        System.out.println("TSP nodes explored = " + num_explored);
        System.out.println("TSP Cost = " + cost);
        for (String city : cities) {
            System.out.println(city);
        }
    }

}
