
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

import java.util.LinkedList;

/**
 * A priority queue implementation using a linked list.
 * Elements are sorted based on their natural ordering or a custom comparator.
 *
 * @param <E> the type of elements in this priority queue
 */
public class PriorityQ<E extends Comparable<? super E>> {
	private LinkedList<E> queue;

	/**
	 * Constructs an empty priority queue.
	 */
	public PriorityQ() {
		queue = new LinkedList<E>();
	}

	/**
	 * Inserts the specified element into this priority queue.
	 * pre: none
	 *
	 * @param node the element to add
	 */
	public void enqueue(E node) {
		int index = indexHelp(node);
		// Add the node to the queue at the determined index
		queue.add(index, node);
	}

	/**
	 * Finds the appropriate index to insert the given node.
	 * pre: none
	 *
	 * @param node the node to be inserted
	 * @return the index at which the node should be inserted
	 */
	private int indexHelp(E node) {
		int index = 0;
		// Iterate over the elements in the queue
		for (E currNode : queue) {
			// Compare the current node with the given node
			int diff = currNode.compareTo(node);
			// Return the current index as the insertion point
			if (diff > 0) {
				return index;
			}
			index++;
		}
		return index;
	}

	/**
	 * Retrieves and removes the head of this queue.
	 * pre: none
	 *
	 * @return the head of this queue, or null if this queue is empty
	 */
	public E dequeue() {
		if (isEmpty()) {
			return null;
		}
		return queue.remove(0);

	}

	/**
	 * Checks if this priority queue is empty.
	 * pre: none
	 *
	 * @return true/false if this queue contains no elements
	 */
	public boolean isEmpty() {
		return queue.size() == 0;
	}

	/**
	 * Returns the number of elements in this priority queue.
	 * pre: none
	 *
	 * @return the number of elements in this queue
	 */
	public int size() {
		return queue.size();
	}

}