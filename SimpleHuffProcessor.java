/*  Student information for assignment:
*
*  On <MY|OUR> honor, <Taylor Hickman> and <Anh Nguyen), this programming assignment is <MY|OUR> own work
*  and <I|WE> have not provided this code to any other student.
*
*  Number of slip days used: 1
*
*  Student 1 (Student whose Canvas account is being used)
*  UTEID: acn2265
*  email address: anhcnguyen218@gmail.com
*  Grader name: Nidhi
*
*  Student 2
*  UTEID: tmh3799
*  email address: taylormh432@utexas.edu
*
*/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    // Represents the viewer for GUI interaction.
    private IHuffViewer myViewer;
    // Number of bits per leaf node in the Huffman tree.
    private static final int BITS_PER_LEAF = BITS_PER_WORD + 1;
    // Array to store the frequency of characters in the input data.
    private int[] frequency;
    // Integer representing the header format for compression.
    private int headerFormat;
    // Number of original bits in the uncompressed data.
    private int ogBits;
    // Huffman tree representing the encoding structure.
    private HuffmanTree tree;
    // Total number of bits used for compression.
    private int compressBits;
    // HashMap to store Huffman codings for each character.
    HashMap<Integer, String> huffCodings;
    // Total count of bits processed during compression.
    private int countBits;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * 
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind
     *                     of
     *                     header to use, standard count format, standard tree
     *                     format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     *         Note, to determine the number of
     *         bits saved, the number of bits written includes
     *         ALL bits that will be written including the
     *         magic number, the header format number, the header to
     *         reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        // Stores header format
        ogBits = 0;
        this.headerFormat = headerFormat;
        BitInputStream input = new BitInputStream(in);
        // Holds current byte
        int currByte = input.readBits(BITS_PER_WORD);
        // Creates the frequency array
        frequency = new int[ALPH_SIZE];
        while (currByte != -1) {
            frequency[currByte]++;
            ogBits += BITS_PER_WORD;
            currByte = input.readBits(BITS_PER_WORD);
        }
        input.close();
        tree = new HuffmanTree(frequency);
        huffCodings = tree.getHuffmanCodings();
        System.out.println(diffInBits());
        diffInBits();
        return diffInBits();
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br>
     * pre: <code>preprocessCompress</code> must be called before this method
     * 
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than
     *              the input file.
     *              If this is false do not create the output file if it is larger
     *              than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        int diff = diffInBits();
        if (!force && diff <= 0) {
            myViewer.showError("compression file will result in larger file than original." +
                    "Pick Force Compression to compress anyways.");
            return -1;
        }
        BitOutputStream output = new BitOutputStream(out);
        // Write magic number
        output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        countBits += BITS_PER_INT;
        // Write SCF
        if (headerFormat == STORE_COUNTS) {
            doCountFormat(output);
            // Write STF
        } else if (headerFormat == STORE_TREE) {
            doTreeFormat(output);
        }
        // Read in data
        BitInputStream input = new BitInputStream(in);
        readInData(input, output);
        // Write pseudo coding to indicate end of file
        endFile(output);
        output.close();
        return countBits;
    }

    /**
     * writes the count format for standard count format
     * pre: none
     * 
     * @param output Bitoutputstream object to write bits
     */
    private void doCountFormat(BitOutputStream output) {
        output.writeBits(BITS_PER_INT, STORE_COUNTS);
        // Write array of freqs
        for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
            output.writeBits(BITS_PER_INT, frequency[k]);
            countBits += BITS_PER_INT;
        }

    }

    /**
     * writes compressed file header in tree format representing
     * the tree in binary representation
     * pre: none
     * 
     * @param output BitoutputStream object to write bits
     */
    private void doTreeFormat(BitOutputStream output) {
        // Write header format
        output.writeBits(BITS_PER_INT, STORE_TREE);
        int bitSize = tree.bitTreeSize();
        // Write the size of tree in binary
        output.writeBits(BITS_PER_INT, bitSize);
        getBinaryRep(output);
        countBits += BITS_PER_INT;

    }

    /**
     * Reads original file whilst encoding using huffman code
     * pre: none
     * 
     * @param input  BitInputSteam object to read bits
     * @param output BitOutputSteam object to write bits
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    private void readInData(BitInputStream input, BitOutputStream output) throws IOException {
        // Read in chunks at a time
        int currByte = input.readBits(BITS_PER_WORD);
        while (currByte != -1) {
            String currCoding = huffCodings.get(currByte);
            for (int i = 0; i < currCoding.length(); i++) {
                char curr = currCoding.charAt(i);
                // Write each corresponding huffman code
                if (curr == '0') {
                    output.writeBits(1, 0);
                } else {
                    output.writeBits(1, 1);
                }
                countBits++;
            }
            // read in the next chunk
            currByte = input.readBits(IHuffConstants.BITS_PER_WORD);
        }
    }

    /**
     * ends the compressed file with pseudo end of file huffman
     * coding
     * pre: none
     * 
     * @param output BitoutputSteam object used to write bits
     */
    private void endFile(BitOutputStream output) {
        String pseudoHuff = huffCodings.get(PSEUDO_EOF);
        // get the pseudo huffman coding and write it at end of file
        for (int i = 0; i < pseudoHuff.length(); i++) {
            if (pseudoHuff.charAt(i) == '0') {
                output.writeBits(1, 0);
            } else {
                output.writeBits(1, 1);
            }
            countBits++;
        }

    }

    /**
     * Gets the binary representation of the huffman encoding
     * binary tree
     * pre: none
     * 
     * @param out outputstream object used to write out bits
     */
    private void getBinaryRep(BitOutputStream out) {
        // Get the binary representation of the tree, represent in 1's and 0's
        String binaryRep = tree.generateBinaryRepresentation();
        for (int i = 0; i < binaryRep.length(); i++) {
            if (binaryRep.charAt(i) == '0') {
                out.writeBits(1, 0);
            } else if (binaryRep.charAt(i) == '1') {
                out.writeBits(1, 1);
            }
        }
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
        Decompress decompress = new Decompress();
        // Create bitstreams
        BitInputStream input = new BitInputStream(new BitInputStream(in));
        BitOutputStream output = new BitOutputStream(new BitOutputStream(out));
        // Run Uncompress from Decrompress class
        return decompress.uncompress(input, output, myViewer);
    }

    /**
     * Sets the viewer
     * pre: none
     * 
     * @param viewer viewer to GUI
     */
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    /**
     * Shows a message on GUI
     * pre: none
     * 
     * @param s string to show on GUI
     */
    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }

    /**
     * Calculates the difference in bits before and after precompressing a file
     * pre: none
     * 
     * @return difference in bits between compressed file and original file
     */
    private int diffInBits() {
        // Initialize the number of compressed bits
        // Magic number and header format
        compressBits = BITS_PER_INT * 2;
        if (headerFormat == STORE_TREE) {
            compressBits += BITS_PER_INT;
            // Size of the tree
            compressBits += tree.getSize();
            compressBits += tree.getNumLeaf() * BITS_PER_LEAF;
        } else if (headerFormat == STORE_COUNTS) {
            // Frequency counts
            compressBits += BITS_PER_INT * ALPH_SIZE;
        }
        // Add the number of bits for Huffman codings
        for (int i = 0; i < frequency.length; i++) {
            if (frequency[i] != 0) {
                String bit = huffCodings.get(i);
                compressBits += bit.length() * frequency[i];
            }
        }
        // Add the number of bits for the pseudo-EOF symbol
        compressBits += huffCodings.get(PSEUDO_EOF).length();
        // Calculate the difference in bits between original and compressed data
        int difference = ogBits - compressBits;
        return difference;
    }

}
