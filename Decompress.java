/*
* Student information for assignment:
*
* On my honor, <Taylor Hickman> and <Anh Nguyen>, this programming assignment is my own work
* and I have not provided this code to any other student.
*
* Number of slip days used: 1
*
* Student 1 (Student whose Canvas account is being used)
* UTEID: acn2265
* email address: anhcnguyen218@gmail.com
* Grader name: Nidhi
*
* Student 2
* UTEID: tmh3799
* email address: taylormh432@utexas.edu
*/

import java.io.IOException;

/**
 * This class handles the decompression of files encoded using Huffman coding.
 * It contains methods to uncompress a file using the Huffman tree constructed
 * from the compressed file's header.
 */
public class Decompress {

    // Array to store frequencies of symbols
    private int[] frequency;
    // Huffman tree used for decompression
    private HuffmanTree tree;

    /**
     * Decompresses a file using Huffman coding.
     * pre: none
     *
     * @param input    the BitInputStream containing the compressed file
     * @param output   the BitOutputStream to write the uncompressed data to
     * @param myViewer the IHuffViewer to display any error messages
     * @return the number of bits written during decompression, or -1 if an error
     *         occurs
     * @throws IOException if an I/O error occurs while reading or writing data
     */
    public int uncompress(BitInputStream input, BitOutputStream output, IHuffViewer myViewer) throws IOException {
        // Read the magic number from the compressed file
        int current = input.readBits(IHuffConstants.BITS_PER_INT);
        // Check if the magic number matches the expected value
        if (current != IHuffConstants.MAGIC_NUMBER) {
            // Display an error message if the magic number is incorrect
            myViewer.showError("Error reading compressed file. \n" + "File did not start with the huff magic number.");
            return -1;
        }
        // Read header format
        current = input.readBits(IHuffConstants.BITS_PER_INT);
        // Process based on the SCF format
        if (current == IHuffConstants.STORE_COUNTS) {
            // Read the frequencies for each symbol
            frequency = new int[IHuffConstants.ALPH_SIZE + 1];
            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                frequency[k] = input.readBits(IHuffConstants.BITS_PER_INT);
            }
            // Construct the Huffman tree using the frequencies
            tree = new HuffmanTree(frequency);
        }
        // Process based on the STF format 
        if (current == IHuffConstants.STORE_TREE) {
            // Skip reading the number of bits used to represent the tree
            input.readBits(IHuffConstants.BITS_PER_INT);
            // Reconstruct the Huffman tree from the compressed data
            tree = new HuffmanTree(input);
        }
        // Call the method to write the uncompressed data to the output stream
        return writeUncompression(input, output);
    }

    /**
     * Writes the uncompressed data to the output stream.
     * pre: none
     *
     * @param input  the BitInputStream containing the compressed file data
     * @param output the BitOutputStream to write the uncompressed data to
     * @return the number of bits written during decompression, or -1 if an error
     *         occurs
     * @throws IOException if an I/O error occurs while reading or writing data
     */
    public int writeUncompression(BitInputStream input, BitOutputStream output) throws IOException {
        // Initialize the variable to store the number of bits written during decompression
        int bitsWritten = 0;
        // Get the root of the Huffman tree
        TreeNode currNode = tree.getRoot();
        int currBit;
        // Loop until the end of the compressed data is reached
        while ((currBit = input.readBits(1)) != -1) {
            // Traverse the Huffman tree based on the read bit
            currNode = (currBit == 0) ? currNode.getLeft() : currNode.getRight();
            // Check if the current node is a leaf node
            if (currNode.isLeaf()) {
                int val = currNode.getValue();
                // Check for PSEUDO_EOF to stop uncompressing
                if (val == IHuffConstants.PSEUDO_EOF) {
                    // Return the total bits written
                    return bitsWritten;
                }
                // Write the value of the leaf node to the output stream
                output.writeBits(IHuffConstants.BITS_PER_WORD, val);
                // Increment bitsWritten
                bitsWritten += IHuffConstants.BITS_PER_WORD;
                // Reset to the root of the Huffman tree for the next symbol
                currNode = tree.getRoot();
            }
        }
        output.close();
        // Indicate error if PSEUDO_EOF is not encountered
        return -1;
    }
}
