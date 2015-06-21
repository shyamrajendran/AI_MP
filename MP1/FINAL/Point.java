/**
 * Created by manshu on 2/7/15.
 */
public class Point {
    int x;
    int y;
    private boolean containsGoal;
    
    Point(int x, int y) {
        this.x = x; this.y = y;
        containsGoal = false;
    }

    Point(int x, int y, boolean b) {
        this.x = x; this.y = y;
        containsGoal = b;
    }

    @Override
    protected Point clone() throws CloneNotSupportedException {
        return new Point(this.x, this.y, this.containsGoal);
    }

    public void setGoal(boolean b) {
        containsGoal = b;
    }
    
    public boolean containsGoal() {
        return containsGoal;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        Point p2 = (Point) obj;
        return p2.x == this.x && p2.y == this.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
