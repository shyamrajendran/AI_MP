/**
 * Created by manshu on 2/22/15.
 */
public class Edge implements Comparable<Edge> {
    String src;
    String dest;
    Double weight;
    Edge(String s, String d, double w) {
        src = s;
        dest = d;
        weight = w;
    }

    @Override
    public int compareTo(Edge edge){
        return weight.compareTo(edge.weight);
    }
}