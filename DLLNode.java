/**
 * A node to be used in a double-linked list
 * @author Geoffrey Meier
 *
 */
public class DLLNode<T> {

	private DLLNode<T> next, previous;	//references to the previous and next nodes
	private T element;	//element contained in the node
	
	/**
	 * Constructor - create a new node containing element of type T
	 * @param element Element to be contained in the node
	 */
	public DLLNode(T element) {
		next = null;
		previous = null;
		this.element = element;
	}
	
	/**
	 * Get the next node
	 * @return The next node
	 */
	public DLLNode<T> getNext() {
		return next;
	}
	
	/**
	 * Set the next node
	 * @param next Node to set as next
	 */
	public void setNext(DLLNode<T> next) {
		this.next = next;
	}
	
	/**
	 * Get the previous node
	 * @return The previous node
	 */
	public DLLNode<T> getPrevious() {
		return previous;
	}
	
	/**
	 * Set the previous node
	 * @param previous Node to set as previous
	 */
	public void setPrevious(DLLNode<T> previous) {
		this.previous = previous;
	}
	
	/**
	 * Get the element for the node
	 * @return element
	 */
	public T getElement() {
		return element;
	}
	
	/**
	 * Set the node's element
	 * @param element element for the node
	 */
	public void setElement(T element) {
		this.element = element;
	}
	
}
