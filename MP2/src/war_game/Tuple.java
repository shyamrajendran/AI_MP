package war_game;

/**
 * Created by manshu on 3/14/15.
 */
public class Tuple {
    private int row;
    private int col;

    public Tuple(int row, int col) {
        this.row = row; this.col = col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    @Override
    public boolean equals(Object obj) {
        Tuple t2 = (Tuple) obj;
        return this.col == t2.col && this.col == t2.col;
    }

    @Override
    public String toString() {
        return "(" + this.row + ", " + this.col + ")";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return (Object) new Tuple(this.row, this.col);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

}

