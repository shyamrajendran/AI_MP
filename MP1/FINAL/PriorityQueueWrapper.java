import java.util.Iterator;

/**
 * Created by manshu on 2/7/15.
 */
public class PriorityQueueWrapper<T extends Comparable<T>> implements Iterable<T> {
    
    public static enum queueType {
        Stack,
        Queue,
        Priority
    }
    
    private class MyItem<W extends Comparable<W>> implements Comparable<MyItem<W>>{
        W item;
        Integer cost;
        public MyItem(W item) {
            this.item = item;
            this.cost = 0;
        }
        public MyItem(W item, Integer cost) {
            this.item = item;
            this.cost = cost;
        }
        public void setCost(Integer cost) {
            this.cost = cost;
        }

        public Integer getCost() {
            return cost;
        }

        public W getItem() {
            return item;
        }

        @Override
        public int compareTo(MyItem o) {
            MyItem<W> other = (MyItem<W>) o;
            if (this.cost == null || o.cost == null)
                return this.item.compareTo(other.item);
            if (this.cost == 0 && o.cost == 0)
                return this.item.compareTo(other.item);
            return this.cost.compareTo(o.cost);
        }

        @Override
        public boolean equals(Object obj) {
            try {
                T item2 = (T) obj;
                return item.equals(item2);
            } catch (ClassCastException cce) {
                //Item can be of different subtype
            }
            MyItem<W> myItem2 = (MyItem<W>) obj;
            return this.item.equals(myItem2.item);
        }
    }
    
    MyPriorityQueue<MyItem<T>> queue;
    Enum myQueueType;
    private int counter;
    
    public PriorityQueueWrapper(String queue_type) {
        try {
            myQueueType = queueType.valueOf(queue_type);
        } catch (IllegalArgumentException iae) {
            System.out.println("Queue Type should be correctly specified. -> " + iae.getMessage());
            System.exit(1);
        }
        queue = new MyPriorityQueue<MyItem<T>>();
        counter = 0;
    }
    
    public void add(T item) {
        MyItem<T> myItem = new MyItem<T>(item);
        if (myQueueType == queueType.Stack)
            myItem.setCost(--counter);
        else if (myQueueType == queueType.Queue)
            myItem.setCost(++counter);
        else if (myQueueType == queueType.Priority) {
            myItem.setCost(0);
        }
        queue.add(myItem);
    }

    public void update(T item1, T item2) {
        MyItem<T> myItem1 = new MyItem<T>(item1);
        MyItem<T> myItem2 = new MyItem<T>(item2);
        
        if (myQueueType == queueType.Stack)
            myItem2.setCost(myItem1.getCost());
        else if (myQueueType == queueType.Queue)
            myItem2.setCost(myItem1.getCost());
        else if (myQueueType == queueType.Priority) {
            myItem2.setCost(0);
        }
        queue.update(myItem1, myItem2);
    }

    public T poll() {
        return queue.poll().item;
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public T remove() {
        return queue.remove().item;
    }

    public boolean contains(T item) {
        MyItem<T> myItem = new MyItem<T>(item);
        return queue.contains(myItem);
    }

    public T getContent(T item) {
        MyItem<T> myItem = new MyItem<T>(item);
        if (queue.getContent(myItem) == null) return null;
        return queue.getContent(myItem).item;
    }
    
    public int remove(T item) {
        MyItem<T> myItem = new MyItem<T>(item);
        return queue.remove(myItem);
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<MyItem<T>> items = queue.iterator();
        Iterator<T> iterator = new Iterator<T>() {
            int current = 0;

            @Override
            public boolean hasNext() {
                return items.hasNext();
            }

            @Override
            public T next() {
                return items.next().item;
            }

            @Override
            public void remove() {
                return;
            }
        };
        return iterator;
    }

    public static void main(String[] args) {
        PriorityQueueWrapper<Integer> myPriorityQueue = new PriorityQueueWrapper<Integer>("Priority");
        myPriorityQueue.add(5);
        myPriorityQueue.add(1);
        myPriorityQueue.add(6);
        myPriorityQueue.add(2);
        myPriorityQueue.add(3);
        
        myPriorityQueue.update(5, 9);

        int numInsert = 5;
//        for (int i = 0; i < numInsert; i++) {
//            int num = (int) (Math.random() * numInsert * 10);
//            myPriorityQueue.add(num);
//            System.out.println(num);
//        }

        System.out.println("===========================");
        Iterator<Integer> iterator = myPriorityQueue.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();

        System.out.println("===========================");
        numInsert = myPriorityQueue.size();
        
        for (int i = 0; i < numInsert; i++) {
            int curr = myPriorityQueue.poll();
            System.out.println(curr);
        }
    }
}
