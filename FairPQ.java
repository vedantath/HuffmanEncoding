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
     * @param node The node to be inserted to the priority queue.
     */
    public void enqueue(TreeNode node) {
        PQNode newNode = new PQNode(node);
        if (head == null) {
            head = newNode;
        }
        else {
            // edge for the case new node less than head
            if (head.node.getFrequency() > node.getFrequency()) {
                newNode.next = head;
                head = newNode;
            }
            else {
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
        System.out.println(this.toString());
    }

    /**
     * Removes the node with the lowest frequency from the priority queue.
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

    public int size() {
        return size;
    }

    public TreeNode getFirst() {
        return head.node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        PQNode current = head;
        while (current != null) {
            char ch = (char) current.node.getValue();
            sb.append("("+ch+" , "+ current.node.getFrequency()+")" + " ");
            current = current.next;
        }
        return sb.toString();
    }

    private class PQNode {
        //private instance variables
        private TreeNode node;
        private PQNode next;

        //constructor
        public PQNode(TreeNode node) {
            this.node = node;
            next = null;
        }
    }
}