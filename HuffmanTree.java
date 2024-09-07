
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
import java.util.HashMap;

/**
 * HuffmanTree class represents a Huffman coding tree constructed from a given
 * array of frequencies.
 * It provides methods to build the Huffman tree, create Huffman codings, and
 * store/retrieve the tree.
 * The Huffman tree is used for compression and decompression of data.
 */
public class HuffmanTree implements IHuffConstants {

    // The root node of the Huffman tree
    private TreeNode root;
    // Mapping of Huffman codings for each symbol.
    private HashMap<Integer, String> huffmanCodings;
    // The size of the Huffman tree
    private int size;
    // The number of leaf nodes in the Huffman tree
    private int numLeaf;

    /**
     * Constructor for HuffmanTree class.
     * Constructs a Huffman coding tree based on the given frequencies.
     *
     * @param frequencies frequencies of symbols from the original data
     */
    public HuffmanTree(int[] frequencies) {
        buildTree(frequencies);
        createCodeMap();
    }

    /**
     * Helper method to build the Huffman tree using a priority queue.
     * pre: none
     *
     * @param frequencies frequencies of symbols from the original data
     */
    private void buildTree(int[] frequencies) {
        PriorityQ<TreeNode> pq = new PriorityQ<>();

        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                pq.enqueue(new TreeNode(i, frequencies[i]));
                size++;
                numLeaf++;
            }
        }
        // Create a treeNode representing PSEUDO_EOF with a frequency of 1
        TreeNode pseudo = new TreeNode(PSEUDO_EOF, 1);
        numLeaf++;
        // Add the PSEUDO_EOF node into the priority queue
        pq.enqueue(pseudo);

        // Build the Huffman tree by merging nodes until only the root remains
        while (pq.size() > 1) {
            TreeNode left = pq.dequeue();
            TreeNode right = pq.dequeue();
            int sumFrequency = left.getFrequency() + right.getFrequency();
            TreeNode parent = new TreeNode(left, sumFrequency, right);
            size++;
            pq.enqueue(parent);
        }
        // Set the root of the Huffman tree
        root = pq.dequeue();
        size++;
        // Create Huffman codings for each symbol in the tree
        createCodeMap();
    }

    /**
     * Creates a mapping of Huffman codings for each symbol in the Huffman tree.
     * pre: none
     */
    private void createCodeMap() {
        huffmanCodings = new HashMap<>();
        traverseTree(root, "");
    }

    /**
     * Traverses the Huffman tree recursively to build Huffman codings for each
     * symbol.
     * pre: none
     *
     * @param node current node in the Huffman tree
     * @param code Huffman coding for the current node
     */
    private void traverseTree(TreeNode node, String code) {
        // If the current node is a leaf, add its Huffman coding to the map
        if (node.isLeaf()) {
            huffmanCodings.put(node.getValue(), code);
            return;
        }
        // Traverse the left subtree with '0' appended to the code
        traverseTree(node.getLeft(), code + "0");
        // Traverse the right subtree with '1' appended to the code
        traverseTree(node.getRight(), code + "1");
    }

    /**
     * Computes the size of the Huffman tree in bits.
     * pre: none
     *
     * @return size of the Huffman tree in bits
     */
    public int bitTreeSize() {
        return size + (numLeaf * (IHuffConstants.BITS_PER_WORD + 1));
    }

    /**
     * Retrieves the number of leaf nodes in the Huffman tree.
     * pre: none
     *
     * @return number of leaf nodes in the Huffman tree
     */
    public int getNumLeaf() {
        return numLeaf;
    }

    /**
     * Retrieves the root node of the Huffman tree.
     * pre: none
     *
     * @return root node of the Huffman tree
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Retrieves the mapping of Huffman codings for each symbol.
     * pre: none
     *
     * @return mapping of Huffman codings for each symbol
     */
    public HashMap<Integer, String> getHuffmanCodings() {
        return huffmanCodings;
    }

    /**
     * Retrieves the size of the Huffman tree.
     * pre: none
     *
     * @return size of the Huffman tree
     */
    public int getSize() {
        return size;
    }

    /**
     * Constructs a Huffman tree by reading the header information from the input
     * stream.
     * pre: none
     *
     * @param input input stream containing the header information
     * @throws IOException if an I/O error occurs while reading the header
     */
    public HuffmanTree(BitInputStream input) throws IOException {
        size = 0;
        root = storeTreeHeader(input);
    }

    /**
     * Helper method to recursively read the header information and reconstruct the
     * Huffman tree.
     * pre: none
     *
     * @param input input stream containing the header information
     * @return root node of the reconstructed Huffman tree
     * @throws IOException if an I/O error occurs while reading the header
     */
    private TreeNode storeTreeHeader(BitInputStream input) throws IOException {
        int currBit = input.readBits(1);
        if (currBit == -1) {
            throw new IllegalArgumentException("Error!");
        } else if (currBit == 0) {
            // Read an internal node and recursively construct its left and right subtrees
            size++;
            TreeNode node = new TreeNode(-1, 0);
            node.setLeft(storeTreeHeader(input));
            node.setRight(storeTreeHeader(input));
            return node;
        } else {
            // Read a leaf node with its value and return it
            size++;
            int val = input.readBits(IHuffConstants.BITS_PER_WORD + 1);
            return new TreeNode(val, 0);
        }
    }

    /**
     * Prints a vertical representation of the Huffman tree.
     * The tree is rotated counter-clockwise by 90 degrees, with the root on the
     * left.
     * Each node is printed on its own row, and its children are indented three
     * spaces from the parent.
     * pre: none
     */
    public void printTree() {
        printTree(root, "");
    }

    /**
     * Helper method to recursively print the Huffman tree in a vertical
     * representation.
     * pre: none
     *
     * @param node   current node being printed
     * @param spaces indentation for the current node
     */
    private void printTree(TreeNode node, String spaces) {
        if (node != null) {
            // Print the right subtree first with additional indentation
            printTree(node.getRight(), spaces + "  ");
            // Print the current node's value
            System.out.println(spaces + node.getValue());
            // Print the left subtree with the same indentation as the parent
            printTree(node.getLeft(), spaces + "  ");
        }
    }

    /**
     * Generates the binary representation of the Huffman tree.
     * Internal nodes are represented by '0' and leaf nodes by '1' followed by the
     * 9-bit value.
     * pre: none
     *
     * @return binary representation of the Huffman tree
     */
    public String generateBinaryRepresentation() {
        StringBuilder binaryRepresentation = new StringBuilder();
        generateBinaryRepresentation(root, binaryRepresentation, "");
        return binaryRepresentation.toString();
    }

    /**
     * Helper method to recursively generate the binary representation of the
     * Huffman tree.
     * pre: none
     *
     * @param node                 current node being processed
     * @param binaryRepresentation string builder for the binary representation
     * @param prefix               binary prefix for the current node
     */
    private void generateBinaryRepresentation(TreeNode node, StringBuilder binaryRepresentation,
            String prefix) {
        if (node != null) {
            // Append '0' if the current node is not a leaf
            if (!node.isLeaf()) {
                binaryRepresentation.append("0");
            }
            // Recursively generate representation for the left subtree
            generateBinaryRepresentation(node.getLeft(), binaryRepresentation, prefix + "0");
            // Recursively generate representation for the right subtree
            generateBinaryRepresentation(node.getRight(), binaryRepresentation, prefix + "1");
            // For leaf nodes, append '1' followed by the 9-bit value
            if (node.isLeaf()) {
                binaryRepresentation.append("1");
                // Convert the value to binary with leading zeros if necessary
                String leafValueBinary = String.format(" %09d",
                        Integer.parseInt(Integer.toBinaryString(node.getValue())));
                binaryRepresentation.append(leafValueBinary);
            }
        }
    }
}
