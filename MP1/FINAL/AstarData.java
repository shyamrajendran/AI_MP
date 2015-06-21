import java.util.ArrayList;
import java.util.List;

/**
 * Created by manshu on 2/22/15.
 */
public class AstarData {
    
    Integer cost;
    List<Point> path;
    Integer num_expanded;
    
    public AstarData(int cost, List<Point> path, int num_expanded) {
        this.cost = cost;
        this.path = path;
        this.num_expanded = num_expanded;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Cost = ").append(cost).append(", ");
        stringBuilder.append("Num Explored = ").append(num_expanded).append(", ");
        stringBuilder.append(" and Path is ");
        for (Point p : path) {
            stringBuilder.append(p.toString()).append(", ");
        }
        return stringBuilder.toString();
    }
}
