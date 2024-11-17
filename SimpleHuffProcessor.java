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
        BitInputStream bits = new BitInputStream(in); // wrap the input stream I am given in a bit
        // input stream
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
        //System.out.println("countBits: " + countBits * IHuffConstants.BITS_PER_WORD);
        printMap();

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
        throw new IOException("compress is not implemented");
        //return 0;
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
