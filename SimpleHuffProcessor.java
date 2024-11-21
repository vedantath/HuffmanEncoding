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
    private final TreeMap<Integer, Integer> freqMap;
    private final FairPQ pq;
    private boolean processed = false;
    private final TreeMap<Character, String> huffCodes;
    private int header;

    //Override default constructor
    public SimpleHuffProcessor() {
        freqMap = new TreeMap<Integer, Integer>();
        pq = new FairPQ();
        huffCodes = new TreeMap<Character, String>();
    }

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     *
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     *                     header to use, standard count format, standard tree format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        header = headerFormat;
        BitInputStream bits = new BitInputStream(in); // wrap input stream given in BitInputStream
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        int countBits = 0;
        while (inbits != -1) {
            if (freqMap.containsKey(inbits)) {
                freqMap.put(inbits, freqMap.get(inbits) + 1);
            } else {
                freqMap.put(inbits, 1);
            }
            countBits ++;
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        }
        System.out.println("countBits preprocess: " + countBits);
        freqMap.put(PSEUDO_EOF, 1); // add the PSEUDO_EOF character to the map
        for (Integer key : freqMap.keySet()) {
            pq.enqueue(new TreeNode(key, freqMap.get(key)));
        }
        TreeNode builtTree = buildTree(pq);
        createHuffmanCodes(builtTree, "");

        processed = true;
        in.reset();
        int compressedBitsTotal = compress(in, new BitOutputStream(System.out)
                , false);
        return( countBits * IHuffConstants.BITS_PER_WORD)- compressedBitsTotal;
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
        TreeNode node = pq.getFirst();
        return node; // returns entire tree
    }


    private void createHuffmanCodes(TreeNode node, String code) {
        if (node.isLeaf()) {
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
     *
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     *              If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if (!processed) {
            preprocessCompress(in, IHuffProcessor.STORE_COUNTS);
        }
        BitInputStream inBits = new BitInputStream(in);
        BitOutputStream outBits = new BitOutputStream(out);
        int bitsWritten = 0;
        // write magic number
        outBits.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
        bitsWritten += IHuffConstants.BITS_PER_INT;
        // Standard count format
        if (header == IHuffConstants.STORE_COUNTS) {
            outBits.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_COUNTS);
            bitsWritten += IHuffConstants.BITS_PER_INT;
            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                if (freqMap.containsKey(k)) {
                    outBits.writeBits(IHuffConstants.BITS_PER_INT, freqMap.get(k));
                } else {
                    outBits.writeBits(IHuffConstants.BITS_PER_INT, 0);
                }
                bitsWritten += IHuffConstants.BITS_PER_INT;
            }
        }
        else if (header == IHuffConstants.STORE_TREE) { // Standard tree format
            outBits.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_TREE);
            int representSize = (freqMap.size() * 9) + treeSize(pq.getFirst());
            outBits.writeBits(IHuffConstants.BITS_PER_INT, representSize);
            bitsWritten += IHuffConstants.BITS_PER_INT * 2;
            bitsWritten += preorder(pq.getFirst(), outBits, 0);
        }

        int inbit = inBits.readBits(IHuffConstants.BITS_PER_WORD);
        while (inbit != -1) {
            String code = huffCodes.get((char) inbit);
            for (int i = 0; i < code.length(); i++) {
                outBits.writeBits(1, code.charAt(i) == '0' ? 0 : 1);
            }
            bitsWritten += code.length();
            inbit = inBits.readBits(IHuffConstants.BITS_PER_WORD);
        }
        String eofCode = huffCodes.get((char) PSEUDO_EOF);
        for (int i = 0; i < eofCode.length(); i++) {
            outBits.writeBits(1, eofCode.charAt(i) == '0' ? 0 : 1);
        }
        bitsWritten += eofCode.length();

        System.out.println("\nbits written compress: " + bitsWritten);
        if (bitsWritten > (freqMap.size() * IHuffConstants.BITS_PER_WORD) && !force) {
            outBits = new BitOutputStream(out);
            myViewer.showError("Compressed file is larger than original file. \n" +
                    "Use force compression to compress anyway.");
            return -1;
        }
        outBits.flush();
        outBits.close();
        return bitsWritten;
    }

    private int treeSize(TreeNode node) {
        if (node == null) {
            return 0;
        }
        int left = treeSize(node.getLeft());
        int right = treeSize(node.getRight());
        return left + right + 1;
    }

    private int preorder(TreeNode node, BitOutputStream out, int bitsWritten) {
        if (node != null) {
            if (!node.isLeaf()) {
                out.writeBits(1, 0);
                bitsWritten++;
            } else {
                out.writeBits(1, 1);
                out.writeBits(9, node.getValue());
                bitsWritten += 10;
            }
            bitsWritten = preorder(node.getLeft(), out, bitsWritten);
            bitsWritten = preorder(node.getRight(), out, bitsWritten);
        }
        return bitsWritten;
    }

    private boolean checkMagicNum(BitInputStream inBits) throws IOException {
        int magicNum = inBits.readBits(IHuffConstants.BITS_PER_INT);
        if (magicNum != IHuffConstants.MAGIC_NUMBER) {
            myViewer.showError("Error reading compressed file. \n" +
                    "File does not start with the huff magic number.");
            return false;
        }
        return true;
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     *
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        // read & check magic number
        BitInputStream inBits = new BitInputStream(in);
        BitOutputStream outBits = new BitOutputStream(out);
        int bitsWritten = 0;
        if (!checkMagicNum(inBits)) {
            return bitsWritten;
        }
        FairPQ huffPQ = new FairPQ();
        int header = inBits.readBits(IHuffConstants.BITS_PER_INT);
        TreeNode reconstructedTree;
        if (header == IHuffConstants.STORE_COUNTS) {
            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                int frequencyInOriginalFile = inBits.readBits(BITS_PER_INT);
                if (frequencyInOriginalFile > 0)
                    huffPQ.enqueue(new TreeNode(k, frequencyInOriginalFile));
            }
            huffPQ.enqueue(new TreeNode(IHuffConstants.PSEUDO_EOF, 1));
            reconstructedTree = buildTree(huffPQ);
        } else {
            int treeSize = inBits.readBits(IHuffConstants.BITS_PER_INT);
            reconstructedTree = readTreeFormatHelper(inBits, new TreeNode(-1, 0));
        }
        TreeNode currentNode = reconstructedTree;

        boolean done = false;
        while (!done) {
            int bit = inBits.readBits(1);
            if (bit == -1) {
                myViewer.showError("Error reading compressed file. Unexpected end of file.");
            } else {
                if (bit == 0) {
                    currentNode = currentNode.getLeft();
                }
                if (bit == 1) {
                    currentNode = currentNode.getRight();
                }
                if (currentNode.isLeaf()) {
                    if (currentNode.getValue() == IHuffConstants.PSEUDO_EOF) {
                        done = true;
                    } else {
                        outBits.writeBits(IHuffConstants.BITS_PER_WORD, currentNode.getValue());
                        bitsWritten += IHuffConstants.BITS_PER_WORD;
                        currentNode = reconstructedTree;
                    }
                }
            }
        }
        outBits.flush();
        outBits.close();

        return bitsWritten;
    }

    private TreeNode readTreeFormatHelper(BitInputStream in, TreeNode node) throws IOException {
        int bit = in.readBits(1);
        if (bit == 0) {
            node.setLeft(readTreeFormatHelper(in, new TreeNode(-1, 0)));
            node.setRight(readTreeFormatHelper(in, new TreeNode(-1, 0)));
            return node;
        } else if (bit == 1) {
            int newBit = in.readBits(9);
            node = new TreeNode(newBit, 0);
            return node;
        } else {
            myViewer.showError("Error reading compressed file. \n" +
                    "Unexpected end of file.");
        }
        return null;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
