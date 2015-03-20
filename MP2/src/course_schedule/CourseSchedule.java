package course_schedule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by manshu on 3/13/15.
 */
public class CourseSchedule {
    private final int MAX_SEM = 10;
    private int N;
    private int CMAX, CMIN;
    private int BUDGET;
    private int[] interested_course_ids; //given interested courses from file
    private CourseMetaData[] course_details; // course data
    private int[] assignment; // stores sem_id for each ith - 1 course
    private int[] sem_load; // stores current load in ith - 1 sem
    private Set<Integer> interested_set; // ordered_interested_set;
    private Set<Integer> uninterested_set; // ordered_uninterested_set;
    private Deque<Integer> unassigned_interested_set; // values not assigned in interested set
    private Deque<Integer> unassigned_uninterested_set; // values not assigned in uninterested set
    private int current_budget;


    public void readCourseDetails(String file_name) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file_name));

        String temp_line[];
        temp_line = bufferedReader.readLine().split(" ");
        N = Integer.parseInt(temp_line[0]);
        CMIN = Integer.parseInt(temp_line[1]);
        CMAX = Integer.parseInt(temp_line[2]);
        course_details = new CourseMetaData[N];

        for (int i = 0; i < N; i++) {
            temp_line = bufferedReader.readLine().split(" ");
            course_details[i] = new CourseMetaData(i + 1, Integer.parseInt(temp_line[0]),
                    Integer.parseInt(temp_line[1]), Integer.parseInt(temp_line[2]));
        }
        for (int i = 0; i < N; i++) {
            temp_line = bufferedReader.readLine().split(" ");
            for (int j = 1; j < temp_line.length; j++) {
                course_details[i].addDep(Integer.parseInt(temp_line[j]));
            }
        }

        temp_line = bufferedReader.readLine().split(" ");
        interested_course_ids = new int[Integer.parseInt(temp_line[0])];
        for (int j = 1; j < temp_line.length; j++) {
            interested_course_ids[j - 1] = Integer.parseInt(temp_line[j]);
        }
        BUDGET = Integer.parseInt(bufferedReader.readLine());
    }

    private void makeInterestedSet() {
        interested_set = new LinkedHashSet<Integer>();
        uninterested_set = new LinkedHashSet<Integer>();

        for (int i = 0; i < interested_course_ids.length; i++) {
            if (course_details[interested_course_ids[i] - 1].getDeps().isEmpty())
                interested_set.add(interested_course_ids[i]);
        }

        for (int i = 0; i < interested_course_ids.length; i++) {
            Stack<Integer> stack = new Stack<Integer>();
            if (!course_details[interested_course_ids[i] - 1].getDeps().isEmpty()) {
                stack.add(interested_course_ids[i]);
                Integer current_course_id;
                while (!stack.isEmpty()) {
                    current_course_id = stack.pop();
                    boolean dep_satisfied = true;
                    for (Integer dep : course_details[current_course_id - 1].getDeps()) {
                        if (interested_set.contains(dep)) continue;
                        else {
                            if (dep_satisfied) stack.add(current_course_id);
                            dep_satisfied = false;
                            stack.add(dep);
                        }
                    }
                    if (dep_satisfied)
                        interested_set.add(current_course_id);
                }
            }
        }
        System.out.println(interested_set);

        for (int i = 1; i <= N; i++) {
            if (interested_set.contains(i)) continue;
            if (course_details[i - 1].getDeps().isEmpty())
                uninterested_set.add(i);
        }

        for (int i = 1; i <= N; i++) {
            if (interested_set.contains(i) || uninterested_set.contains(i)) continue;

            Stack<Integer> stack = new Stack<Integer>();
            if (!course_details[i - 1].getDeps().isEmpty()) {
                stack.add(i);
                Integer current_course_id;
                while (!stack.isEmpty()) {
                    current_course_id = stack.pop();
                    boolean dep_satisfied = true;
                    for (Integer dep : course_details[current_course_id - 1].getDeps()) {
                        if (uninterested_set.contains(dep) || interested_set.contains(dep)) continue;
                        else {
                            if (dep_satisfied) stack.add(current_course_id);
                            dep_satisfied = false;
                            stack.add(dep);
                        }
                    }
                    if (dep_satisfied)
                        uninterested_set.add(current_course_id);
                }
            }
        }
        System.out.println(interested_set);
        System.out.println(uninterested_set);
    }

    private void printSolution() {
        for (int i = 0; i < N; i++) {
            if (assignment[i] == 0) continue;
            System.out.println(i + " -> " + assignment[i]);
        }

        for (int i = 1; i <= MAX_SEM; i++)
            System.out.print(sem_load[i] + " ");

        System.out.println();

        LinkedList<Integer>[] sem_courses = new LinkedList[MAX_SEM];
        int num_sem = 0;
        for (int i = 1; i <= MAX_SEM; i++) {
            if (sem_load[i] != 0) {
                sem_courses[i] = new LinkedList<Integer>();
                num_sem = i;
            }
        }
        int[] sem_budget = new int[num_sem];

        int actual_budget = 0;
        for (int i = 1; i <= N; i++) {
            if (assignment[i - 1] != 0) {
                sem_courses[assignment[i - 1]].add(i);
                if (assignment[i - 1] % 2 == 1) {// fall price
                    actual_budget += course_details[i - 1].getFprice();
                    sem_budget[assignment[i - 1] - 1] += course_details[i - 1].getFprice();
                }
                else {
                    actual_budget += course_details[i - 1].getSprice();
                    sem_budget[assignment[i - 1] - 1] += course_details[i - 1].getSprice();
                }
            }
        }

        System.out.println("<<<<<<<<<<<<<========================Solution ============================>>>>>>>>>>>>>>");
        System.out.println(actual_budget + " " + num_sem);

        for (int i = 1; i <= MAX_SEM; i++) {
            if (sem_load[i] != 0) {
                System.out.print(sem_courses[i].size() + " ");
                for (Integer course : sem_courses[i]) {
                    System.out.print(course + " ");
                }
                System.out.println();
            }
        }

        for (int i = 1; i <= MAX_SEM; i++) {
            if (sem_load[i] != 0) {
                System.out.print(sem_budget[i - 1] + " ");
            }
        }
        System.out.println();
    }

    private boolean isAssignmentComplete() {
        int sem_count = 0;
        for (int i = 1; i <= MAX_SEM; i++) {
            if (sem_load[i] != 0) {
                if (sem_load[i] < CMIN || sem_load[i] > CMAX) return false;
                sem_count++;
            } else {
                if (sem_count == 0) return false;
                while (i <= MAX_SEM) {
                    if (sem_load[i] != 0) return false;
                    i++;
                }
                return unassigned_interested_set.isEmpty();
            }
        }
        return unassigned_interested_set.isEmpty();
    }

    private boolean isConstraintSatisfied(int course_id, int sem_assigned, int course_price) {
        for (Integer dep : course_details[course_id - 1].getDeps()) {
            if (assignment[dep - 1] >= sem_assigned) return false;
        }

        if ((sem_load[sem_assigned] + course_details[course_id - 1].getChours()) > CMAX)
            return false;

        if ((current_budget + course_price) > BUDGET)
            return false;
        return true;
    }

    private boolean recursive_backtracking() {
        if (isAssignmentComplete()) return true;

        int current_course;
        if (!unassigned_interested_set.isEmpty()) {
            current_course = unassigned_interested_set.pollFirst(); // select unassigned variable from interested set
        } else {
            if (unassigned_uninterested_set.isEmpty()) return false;
            current_course = unassigned_uninterested_set.pollFirst(); // select unassigned variable from uninterested set
        }

        for (int sem_value = 1; sem_value <= MAX_SEM; sem_value++) {
            int course_price = course_details[current_course - 1].getFprice();
            if (sem_value % 2 == 0)
                course_price = course_details[current_course - 1].getSprice();
            if (!isConstraintSatisfied(current_course, sem_value, course_price)) continue;
            assignment[current_course - 1] = sem_value;
            sem_load[sem_value] += course_details[current_course - 1].getChours();
            current_budget += course_price;

            // check forward assignments
            if (recursive_backtracking()) return true;

            // revert back if failed
            assignment[current_course - 1] = 0;
            sem_load[sem_value] -= course_details[current_course - 1].getChours();
            current_budget -= course_price;
        }
        // add back to unassigned if failure
        if (!unassigned_interested_set.isEmpty() || (unassigned_uninterested_set.size() == uninterested_set.size()))
            unassigned_interested_set.addFirst(current_course);
        else {
            unassigned_uninterested_set.addFirst(current_course);
        }
        return false;
    }

    public boolean findSchedule(String file_name) throws IOException {
        readCourseDetails(file_name);
        makeInterestedSet();
        assignment = new int[N];
        sem_load = new int[MAX_SEM + 1];
        unassigned_interested_set = new LinkedList<Integer>();
        unassigned_uninterested_set = new LinkedList<Integer>();

        for (Integer i : interested_set)
            unassigned_interested_set.add(i);
        for (Integer i : uninterested_set)
            unassigned_uninterested_set.add(i);

        current_budget = 0;

        if (recursive_backtracking()) { // Calling recursive backtracking
            System.out.println("Yes! Interested course schedule found!");
            printSolution();
        } else {
            System.out.println("No interested course schedule found!");
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        String[] scenarios = {"sample", "first", "second", "third", "fourth"};
        for (String scenario : scenarios) {
            String file_name = "graduation_instances/" + scenario + "Scenario.txt";
            long start = System.currentTimeMillis();
            CourseSchedule cs = new CourseSchedule();
            cs.findSchedule(file_name);
            long end = System.currentTimeMillis();
            System.out.println("Time taken = " + (end - start));
        }
    }
}
