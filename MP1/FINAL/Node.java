import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by manshu on 2/7/15.
 */
public class Node<T> implements Comparable<Node<T>> {
    T item;
    Node<T> parent;
    int compared_cost;
    int current_cost;
    int heuristic_cost;
    
    Stack<Node<T>> prt = new Stack<Node<T>>();
    
    
    Node(T item, Node<T> parent) {
        this.item = item;
        this.parent = parent;
        prt.push(parent);
        current_cost = 0;
        heuristic_cost = 0;
        compared_cost = 0;
    }
    
    public Node<T> getParent() {
        if (prt.empty()) return null;
        return prt.pop();
    }

    public void addParent(Node<T> parent) {
        prt.push(parent);
    }


//    public boolean equalsReference(Object obj) {
//        try {
//            Node<T> node2 = (Node<T>) obj;
//            return this.item.equals(node2.item) && (this.parent == node2.parent);
//        } catch (ClassCastException ce) {
//            System.out.println("Class Exception " + obj);
//            return false;
//        }
//    }
    
    @Override
    public boolean equals(Object obj) {
        Node<T> node2 = (Node<T>) obj;
        return this.item.equals(node2.item);// && (parent == node2.parent);
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public int compareTo(Node<T> o) {
        int compared_cost1 = this.compared_cost;
        int compared_cost2 = o.compared_cost;
        
        if (compared_cost1 > compared_cost2)
            return 1;
        else if (compared_cost1 < compared_cost2)
            return -1;
        else
            return 0;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(item).append(" ");
        stringBuilder.append(String.valueOf(compared_cost));

        return stringBuilder.toString();
    }
    
    public static void main(String[] args) {
        Node<String> A1 = new Node<String>("A", null);
        Node<String> A2 = new Node<String>("A", null);

        Node<String> B1 = new Node<String>("B", A1);
        Node<String> B2 = new Node<String>("B", A1);

        System.out.println(B1 == B2);



    }
}
