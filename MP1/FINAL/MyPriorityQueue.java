import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by manshu on 2/7/15.
 */
public class MyPriorityQueue<T extends Comparable<T>> implements Iterable<T> {
    
    private Object[] queue;
    private int size;
    private HashMap<T, Integer> data_index_map;
    
    public MyPriorityQueue() {
        this(2);
    }

    public MyPriorityQueue(int capacity) {
        queue = new Object[capacity];
        size = 0;
        data_index_map = new HashMap<T, Integer>(size);
    }
    
    private Integer getParent(int i) {
        int p = (i - 1) / 2;
        if (p < 0) return null;
        return p;
    }
    
    private Integer getLeft(int i) {
        int l = 2 * i + 1;
        if (l >= size) return null;
        return l;
    }
    
    private Integer getRight(int i) {
        int r = 2 * i + 2;
        if (r >= size) return null;
        return r;
    }
    
    private void growQueue() {
        Object[] new_queue = new Object[2 * queue.length];
        for (int i = 0; i < queue.length; i++) {
            new_queue[i] = queue[i];
        }
        queue = new_queue;
    }
    
    private void heapify(int i) {
        if (i >= size) return;
        if (i < 0) return;
        
        Integer parent = getParent(i);
        if (parent == null) return;
        
        if (compareTo(queue[parent], queue[i]) > 0) {
            T temp = (T) queue[i];
            queue[i] = queue[parent];
            queue[parent] = temp;
            data_index_map.put((T) queue[parent], parent);
            data_index_map.put((T) queue[i], i);
            heapify(parent);
        }
    }
    
    private int compareTo(Object o1, Object o2) {
        T item1 = (T) o1;
        T item2 = (T) o2;
        return item1.compareTo(item2);
    }
    
    private void percolateDown(int i) {
        if (i >= size) return;
        if (i < 0) return;

        Integer l = getLeft(i);
        Integer r = getRight(i);
        
        if (l == null) return;
        
        if (l != null && r != null) {
            if (compareTo(queue[i], queue[l]) <= 0 && compareTo(queue[i], queue[r]) <= 0)  return;
            else {
                int smaller = l;
                if (compareTo(queue[l], queue[r]) > 0) smaller = r;
                T temp = (T) queue[i];
                queue[i] = queue[smaller];
                queue[smaller] = temp;
                data_index_map.put((T) queue[smaller], i);
                data_index_map.put((T) queue[i], smaller);
                percolateDown(smaller);
            }
        } else if (r == null) {
            if (compareTo(queue[i], queue[l]) <= 0) return;
            else {
                T temp = (T) queue[i];
                queue[i] = queue[l];
                queue[l] = temp;
                data_index_map.put((T) queue[l], i);
                data_index_map.put((T) queue[i], l);
                percolateDown(l);
            }
        }
    }
    
    public void add(T item) {
        if (data_index_map.containsKey(item)) {
            System.out.println("Already present in queue");
            return;
        }
        if (size == queue.length) {
            growQueue();        
        }    
        data_index_map.put(item, size);
        queue[size++] = item;
        heapify(size - 1);
    }
    
    private int findItem(T item) {
        for (int i = 0; i < size; i++) {
            if (item.compareTo((T) queue[i]) == 0)
                return i;
        }
        return -1;
    }
    
    public int remove(T item) {
        if (!data_index_map.containsKey(item)) {
            System.out.println("Doesn't contain this item");
            return -1;
        }
        int remove_index = data_index_map.get(item);
        queue[remove_index] = queue[size - 1];
        queue[size - 1] = item;
        size--;
        data_index_map.remove(item);
        data_index_map.put((T) queue[remove_index], remove_index);
        
        Integer parent = getParent(remove_index);
        if (parent == null) {
            percolateDown(remove_index);
        } else {
            T parent_item = (T) queue[parent];
            T current_item = (T) queue[remove_index];

            if (parent_item.compareTo(current_item) == -1) heapify(remove_index);
            else percolateDown(remove_index);
        }
        return 0;
    }
    
    public void update(T item, T item2) {
        if (!data_index_map.containsKey(item)) {
            add(item2);
            return;
        }
        int index = data_index_map.get(item);//findItem(item);
//        if (index == -1) {
//            add(item2);
//            return;
//        }
        data_index_map.put(item2, index);
        data_index_map.remove(item);
        
        queue[index] = item2;
        if (item2.compareTo(item) == -1) heapify(index);
        else if (item2.compareTo(item) == 1) percolateDown(index);
    }
    
    public T poll() {
        if (size == 0) return null;
        Object item = queue[0];
        queue[0] = queue[size - 1];
        size--;
        data_index_map.remove(item);
        percolateDown(0);
        return (T) item;
    }

    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public T remove() {
        return poll();
    }
    
    public T getContent(T item) {
        if (!contains(item)) return null;
        return (T)queue[data_index_map.get(item)];
    }
    
    public boolean contains(T item) {
        return data_index_map.containsKey(item);
//        for (int i = 0; i < size; i++) {
//            T item2 = (T)queue[i];
//            if (item2.compareTo(item) == 0) return true;
//        }    
//        return false;
    }
    
    @Override
    public Iterator<T> iterator() {
        Iterator<T> iterator = new Iterator<T>() {
            int current = 0;
            
            @Override
            public boolean hasNext() {
                return current != size;
            }

            @Override
            public T next() {
                return (T) queue[current++];
            }

            @Override
            public void remove() {
                return;
            }
        };
        return iterator;
    }


    public static void main(String[] args) {
        MyPriorityQueue<Integer> myPriorityQueue = new MyPriorityQueue<Integer>();
        myPriorityQueue.add(3);
        myPriorityQueue.add(2);
        myPriorityQueue.add(1);
        myPriorityQueue.add(5);
        myPriorityQueue.add(6);
        myPriorityQueue.update(9, 10);
        
        myPriorityQueue.remove(3);

        int numInsert = 5;
        for (int i = 0; i < numInsert; i++) {
            int num = (int) (Math.random() * numInsert * 10);
            myPriorityQueue.add(num);
            System.out.println(num);
        }

        myPriorityQueue.remove(5);
        myPriorityQueue.remove(6);


        System.out.println("===========================");
        Iterator<Integer> iterator = myPriorityQueue.iterator();
        int counter = 0;
        while (iterator.hasNext()) {
            Integer item = iterator.next();
            System.out.print(item + " " + counter++ + " > ");
            System.out.println(myPriorityQueue.data_index_map.get(item));
        }
        System.out.println();

        System.out.println("===========================");
        int prev = -1;
        int size = myPriorityQueue.size();
        for (int i = 0; i < size; i++) {
            int curr = myPriorityQueue.poll();
            System.out.println(curr);
            if (curr < prev) {
                System.out.println("Oh no na");
                break;
            }
            prev = curr;
        }

    }

}
