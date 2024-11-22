/*  Student information for assignment:
 *
 *  On OUR honor, Vedant Athale and Srishruthik Alle, this programming assignment is OUR own
 * work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: vba252
 *  email address: vedant.athale@gmail.com
 *  Grader name:
 *
 *  Student 2
 *  UTEID: SA59576
 *  email address: shruthik.alle@gmail.com
 *
 */

public class FairPQ {
    //private instance variables
    private PQNode head;
    private int size;

    //constructor
    public FairPQ() {
        this.head = null;
        this.size = 0;
    }

    /**
     * Adds a node to the priority queue, ordered by frequency. If there is a tie, the new node
     * will be places after the node(s) with the same frequency.
     *
     * @param node The node to be inserted to the priority queue.
     */
    public void enqueue(TreeNode node) {
        PQNode newNode = new PQNode(node);
        if (head == null) {
            head = newNode;
        } else {
            // edge for the case new node less than head
            if (head.node.getFrequency() > node.getFrequency()) {
                newNode.next = head;
                head = newNode;
            } else {
                PQNode current = head;
                while (current.next != null && current.next.node.getFrequency() <=
                        node.getFrequency()) {
                    current = current.next;
                }
                newNode.next = current.next;
                current.next = newNode;
            }
        }
        size++;
    }

    /**
     * Removes the node with the lowest frequency from the priority queue.
     *
     * @return the node removed, or null if the queue is empty.
     */
    public TreeNode dequeue() {
        if (head == null) {
            return null;
        }
        TreeNode node = head.node;
        head = head.next;
        size--;
        return node;
    }

    // Return the size of the priority queue
    public int size() {
        return size;
    }

    // Return the first item in the priority queue
    public TreeNode getFirst() {
        if (head != null)
            return head.node;
        return null;
    }

    @Override
    // Return a string representation of the priority queue. (Character, Frequency)
    // Ex Apple --> (A, 1) (l, 1) (e, 1) (p, 2)
    public String toString() {
        StringBuilder sb = new StringBuilder();
        PQNode current = head;
        while (current != null) {
            char ch = (char) current.node.getValue();
            sb.append("(" + ch + " , " + current.node.getFrequency() + ")" + " ");
            current = current.next;
        }
        return sb.toString();
    }


    /**
     * Nested class PQNode to represent a node in the priority queue.
     */
    private class PQNode {
        //private instance variables
        private final TreeNode node;
        private PQNode next;

        //constructor
        public PQNode(TreeNode node) {
            this.node = node;
            next = null;
        }
    }
}
