import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Problem1 {

    private static final List<Integer> bag = Collections.synchronizedList(new ArrayList<>());
    private static PrintWriter pw;

    public static void main(String[] args) {

        // Initialize the PrintWriter
        System.out.print("Initializing PrintWriter...");
        try {
            pw = new PrintWriter("output.txt");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println("done.");

        // Fill the bag with 500k presents, and shuffle them
        System.out.println("Filling bag with presents...");
        for(int i = 1; i <= 500000; i++){
            bag.add(i);
        }
        Collections.shuffle(bag);
        System.out.println("done.");

        // Create the list and servants
        System.out.println("Creating list and servants...");
        ConcurrentLinkedList list = new ConcurrentLinkedList();
        Servant s1 = new Servant(list);
        Servant s2 = new Servant(list);
        Servant s3 = new Servant(list);
        Servant s4 = new Servant(list);
        System.out.println("done.");

        // Create the threads
        System.out.println("Creating threads...");
        Thread t1 = new Thread(s1);
        Thread t2 = new Thread(s2);
        Thread t3 = new Thread(s3);
        Thread t4 = new Thread(s4);
        System.out.println("done.");

        // Start the threads
        System.out.println("Starting threads...");
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        System.out.println("done.");

        // Wait for the threads to finish
        System.out.println("Processing presents...");
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("done.");


        // Close the PrintWriter
        pw.close();
    }

    static class Node {
        int tag;
        Node next;

        public Node(int tag) {
            this.tag = tag;
            this.next = null;
        }
    }

    static class ConcurrentLinkedList {
        private Node head;
        private final Lock lock;

        public ConcurrentLinkedList() {
            head = null;
            lock = new ReentrantLock();
        }

        public void addPresent(int tag) {
            // Acquire lock
            lock.lock();
            try {
                // Add present to list in sorted order
                Node n = new Node(tag);
                if (head == null) {
                    head = n;
                } else {
                    Node cur = head;
                    Node prev = null;
                    while (cur != null && cur.tag < tag) {
                        prev = cur;
                        cur = cur.next;
                    }
                    if (prev == null) {
                        n.next = head;
                        head = n;
                    } else {
                        prev.next = n;
                        n.next = cur;
                    }
                }
            } finally {
                // Release lock
                lock.unlock();
            }
        }

        public Node removePresent() {
            // Acquire lock
            lock.lock();
            Node n = null;
            try {
                // Remove the first present in the list
                if (head != null) {
                    n = head;
                    head = head.next;
                }
            } finally {
                // Release lock
                lock.unlock();
            }
            return n;
        }

        public boolean isPresent(int tag) {
            // Acquire lock
            lock.lock();
            boolean present = false;
            try {
                // Check if present with the given tag is in the list
                Node cur = this.head;
                while (cur != null) {
                    if (cur.tag == tag) {
                        present = true;
                        break;
                    } else {
                        cur = cur.next;
                    }
                }
            } finally {
                // Release lock
                lock.unlock();
            }
            return present;
        }

        public Node checkHead() {
            // Acquire lock
            lock.lock();
            Node n;
            try {
                // Set return Node to head
                n = this.head;
            } finally {
                // Release lock
                lock.unlock();
            }
            return n;
        }
    }

    static class Servant implements Runnable {

        // Actions
        private static final int ADD_PRESENT = 0;
        private static final int REMOVE_PRESENT = 1;
        private static final int CHECK_PRESENT = 2;

        private final ConcurrentLinkedList list;
        private final Random rnd;

        public Servant(ConcurrentLinkedList list) {
            this.list = list;
            this.rnd = new Random();
        }

        @Override
        public void run() {
            while(!allPresentsProcessed()) {
                int action = rnd.nextInt(3);
                switch(action) {
                    case ADD_PRESENT:
                        addPresent();
                        break;
                    case REMOVE_PRESENT:
                        removePresent();
                        break;
                    case CHECK_PRESENT:
                        checkPresent();
                        break;
                }
            }
        }

        private void addPresent() {
            // If the bag isn't empty, remove the first present. If it is, just skip this method
            int tag;
            synchronized (bag) {
                if (bag.isEmpty()) {
                    return;
                }
                tag = bag.remove(0);
            }
            list.addPresent(tag);

            pw.println("Added present with tag: " + tag);
        }

        private void removePresent() {
            // Remove first present in the list, and write the thank-you message
            Node n = list.removePresent();
            if (n != null) {
                pw.println("Thanks for present with tag: " + n.tag);
            }
        }

        private void checkPresent() {
            int tag = rnd.nextInt(500000);
            boolean present = list.isPresent(tag);
            if (present) {
                pw.println("Present with tag: " + tag + " is in the list");
            } else {
                pw.println("Present with tag: " + tag + " is not in the list");
            }
        }

        private boolean allPresentsProcessed() {
            // Termination condition is if the bag and list are empty. (isEmpty() == true, checkHead() == null)
            return bag.isEmpty() && list.checkHead() == null;
        }

    }
}