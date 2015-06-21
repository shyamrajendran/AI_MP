/**
 * Created by manshu on 2/19/15.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/**
 * Program to draw grids.
 *
 * @author Ian Darwin, http://www.darwinsys.com/
 */

class Location {
    int x;
    int y;
    Location(int x, int y){this.x = x; this.y = y;}
}

class GridCanvas extends Canvas {
    int width, height;
    int rows;
    int cols;
    int col_width;
    int row_height;
    private boolean walls[][];
    private boolean goals[][];
    
    GridCanvas(int w, int h, int r, int c) {
        setSize(width = w, height = h);
        rows = r;
        cols = c;
        row_height = height / rows;
        col_width = width / cols;
        walls = new boolean[rows][cols];
        goals = new boolean[rows][cols];
    }

    public void setGoal(int x, int y){
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        g2.setColor(Color.white);
        //delay();
        g2.fillOval(y * col_width, x * row_height, col_width, row_height);
        goals[x][y]= true;
    }
    
    public void setWall(int x, int y) {
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        g2.setColor(Color.red);
        //delay();
        Rectangle2D rect = new Rectangle2D.Double(y * col_width, x * row_height, col_width, row_height);
        g2.draw(rect);
        walls[x][y] = true;
    }


    public void draw(Graphics g) {
        int i;
        width = getSize().width;
        height = getSize().height;

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLUE);
        // draw the rows
        int rowHt = height / (rows);
        for (i = 0; i < rows; i++) {
            Line2D line = new Line2D.Double(0, i * rowHt, width, i * rowHt);
            g2.draw(line);
        }
        // draw the columns
        int rowWid = width / (cols);
        for (i = 0; i < cols; i++) {
            Line2D line = new Line2D.Float(i * rowWid, 0, i * rowWid, height);
            g2.draw(line);
        }

        for (i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (walls[i][j]) setWall(i, j);
                if (goals[i][j]) setGoal(i, j);
            }
        }
     //   delay();
    }
    
    public void delay() {
//        for (int i = 0; i < Integer.MAX_VALUE; i++)
//            for (int j = 0; j < Integer.MAX_VALUE; j++)
//                    ;
        ActionListener animate = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                repaint();
            }
        };
        
        Timer timer = new Timer(200, animate);
        timer.start();
    }


    public void paint(Graphics g) {
        super.paint(g);
        draw(g);
    }
}

public class PacmanGrid extends Frame {

    private GridCanvas xyz;
    private int row_height;
    private int col_width;
    private final HashMap<String, Color> color_map = new HashMap<String, Color>();
    private Location pacman_loc;
    private boolean walls[][];

    public PacmanGrid(String title, int w, int h, int rows, int cols) {
        setTitle(title);
        setResizable(false);
        
        pacman_loc = new Location(-1, -1);
        walls = new boolean[rows][cols];
        color_map.put("WALL", Color.RED); color_map.put("PACMAN", Color.YELLOW); color_map.put("EMPTY", Color.black);

        xyz = new GridCanvas(w, h, rows, cols);
        row_height = h / rows;
        col_width = w / cols;

        xyz.setBackground(color_map.get("EMPTY"));

        add(xyz);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                dispose();
                System.exit(0);
            }
        });

        pack();
        setVisible(true);
    }


    public void setPacmanLoc(int x, int y) {
        Graphics g = xyz.getGraphics();

        Graphics2D g2 = (Graphics2D)g;

        if (pacman_loc.x != -1 && pacman_loc.y != -1) {
            g2.setColor(color_map.get("PACMAN"));
            Rectangle2D rect = new Rectangle2D.Float(pacman_loc.y * col_width, pacman_loc.x * row_height, col_width, row_height);
            //g2.draw(rect);
            //g2.clearRect(pacman_loc.y * col_width, pacman_loc.x * row_height, col_width, row_height);
            g.fillRect(pacman_loc.y * col_width - 1, pacman_loc.x * row_height - 1, col_width, row_height);
           //delay(400);
        }
        
        if (walls[x][y]) {
            System.out.println("Pacman can't be on the wall");
            return; //Pacman can't be on the wall
        }

        pacman_loc.x = x; pacman_loc.y = y;
        
//        int r1 = Math.min(col_width, row_height);
//        int r2 = Math.max(col_width, row_height);
        //xyz.repaint();
        delay(75);
        g2.setColor(color_map.get("PACMAN"));
        //Ellipse2D ellipse2D = new Ellipse2D.Double(y * col_width, x * row_height, col_width, row_height);
        //g2.draw(ellipse2D);
        g2.fillOval(y * col_width, x * row_height, col_width, row_height);
        
    }
    
    public void clearAll(){
        xyz.repaint();
        delay(1000);
    }
    
    public void setWall(int x, int y) {
        xyz.setWall(x, y);
    }
    
    public void setGoal(int x, int y) {
        xyz.setGoal(x, y);
    }
    public void delay(int x) {
//        for (int i = 0; i < 3273768; i++)
//            for (int j = 0; j < 3273768; j++)
//                ;
        try{
            Thread.sleep(x);
        } catch (Exception e) {
            System.out.println();
        }
    }

    public static void main(String[] a) {
        int screen_x = 500;
        int screen_y = 500;
        int rows = 20;
        int cols = 20;
        
        PacmanGrid g = new PacmanGrid("Test", screen_x, screen_y, rows, cols);
        g.setWall(1, 1);
        g.setWall(1, 2);
        g.setWall(2, 1);
        g.setWall(3, 3);

        g.setGoal(19, 19);

        for (int i = 0; i < rows / 2; i++)
            g.setPacmanLoc(i, 0);
        for (int i = 0; i < cols / 2; i++)
            g.setPacmanLoc(0, i);

    }
}

