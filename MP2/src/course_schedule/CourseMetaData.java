package course_schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manshu on 3/13/15.
 */
public class CourseMetaData {
    private int course_id;
    private int f_price;
    private int s_price;
    private int c_hours;
    private List<Integer> c_deps;

    public CourseMetaData(int course_id, int f_price, int s_price, int c_hours) {
        this.course_id = course_id;
        this.f_price = f_price;
        this.s_price = s_price;
        this.c_hours = c_hours;
        this.c_deps = new ArrayList<Integer>();
    }

    @Override
    public String toString() {
        String line =  String.valueOf(course_id) + " " + String.valueOf(f_price) + " " + String.valueOf(s_price) + " " +
                String.valueOf(c_hours) + " Deps =";
        for (Integer i : c_deps)
            line += " " + i;
        return line;
    }

    public List<Integer> getDeps() {
        return c_deps;
    }

    public void addDep(Integer dep) {
        this.c_deps.add(dep);
    }

    public int getCourseId() {
        return course_id;
    }

    public int getFprice() {
        return f_price;
    }

    public int getSprice() {
        return s_price;
    }

    public int getChours() {
        return c_hours;
    }
}
