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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private TreeMap<Integer, Integer> freqMap;
    private FairPQ pq;
    private boolean processed = false;
    private TreeMap<Character, String> huffCodes;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
//        showString("Not working yet");
//        myViewer.update("Still not working");
//        throw new IOException("preprocess not implemented");
        //return 0;
        freqMap = new TreeMap<Integer, Integer>();
        pq = new FairPQ();
        huffCodes = new TreeMap<Character, String>();

        BitInputStream bits = new BitInputStream(in); // wrap input stream given in BitInputStream
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        int countBits = 0;
        while (inbits != -1) {
            System.out.println(inbits); // prints the value we read in as an int regardless of
            // what it was suppose to represent
            if (freqMap.containsKey(inbits)) {
                freqMap.put(inbits, freqMap.get(inbits) + 1);
            } else {
                freqMap.put(inbits, 1);
            }
            countBits++;
            inbits =  bits.readBits(IHuffConstants.BITS_PER_WORD);
        }
        freqMap.put(PSEUDO_EOF, 1); // add the PSEUDO_EOF character to the map
        //printMap();
        for (Integer key : freqMap.keySet()) {
            pq.enqueue(new TreeNode(key, freqMap.get(key)));
        }
        //System.out.println("\n" + pq.toString());
        TreeNode builtTree = buildTree(pq);
        createHuffmanCodes(builtTree, "");
        //System.out.println(huffCodes.toString());
        processed = true;
        return countBits * IHuffConstants.BITS_PER_WORD;
    }

    private void printMap() {
        System.out.println("Counting characters in selected file.\n\nFrequencies of values in " +
                "file.");
        for (Integer key : freqMap.keySet()) {
            char ch = (char) key.intValue();
            System.out.println(key + " "+ Integer.toBinaryString(key) + " " + ch + " " + freqMap.get(key));
        }
    }

    private TreeNode buildTree(FairPQ pq) {
        while (pq.size() > 1) {
            TreeNode left = pq.dequeue();
            TreeNode right = pq.dequeue();
            TreeNode parent = new TreeNode(-1, left.getFrequency() + right.getFrequency());
            parent.setLeft(left);
            parent.setRight(right);
            pq.enqueue(parent);
        }
        TreeNode node = pq.dequeue();
        System.out.println("Tree built: " + node.toString());
        return node; // returns entire tree
    }


    private void createHuffmanCodes(TreeNode node, String code) {
        if (node.isLeaf()) {
            //huffCodes.put(node.getValue(), code);
            huffCodes.put((char) node.getValue(), code);
        } else {
            createHuffmanCodes(node.getLeft(), code + "0");
            createHuffmanCodes(node.getRight(), code + "1");
        }
    }
    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        //throw new IOException("compress is not implemented");
        if (!processed) {
            preprocessCompress(in, IHuffProcessor.STORE_COUNTS);
        }
        BitInputStream inBits = new BitInputStream(in);
        BitOutputStream outBits = new BitOutputStream(out);
        // write magic number
        outBits.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
        checkMagicNum(inBits);
        outBits.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_COUNTS);
        for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
            outBits.writeBits(IHuffConstants.BITS_PER_INT, freqMap.get(k));
        }

        return 0;


    }

    private int checkMagicNum(BitInputStream inBits) throws IOException {
        int magicNum = inBits.readBits(IHuffConstants.BITS_PER_INT);
        if (magicNum != IHuffConstants.MAGIC_NUMBER) {
            myViewer.showError("Error reading compressed file. \n" +
                    "File does not start with the huff magic number.");
            return -1;
        }
        return magicNum;
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
	        throw new IOException("uncompress not implemented");
	        //return 0;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s){
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
