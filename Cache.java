/**
 * This class allows the user to create and manage a cache.
 * 
 * @author Geoffrey Meier
 *
 */
public class Cache<T> {
	
	private int count;
	private int CAPACITY;
	private DLLNode<T> head, tail;
	
	
	/**
	 * The constructor for a new empty Cache.
	 * @param size The maximum size (capacity) of the Cache.
	 */
	public Cache(int size) {
		count = 0;
		CAPACITY = size;
		head = null;
		tail = null;
	}
	
	/**
	 * Search the cache for the specified object.
	 * @param object The object to search for.
	 * @return The returned object (null if object not found).
	 */
	public T get(T object) {
		
		if (count==0)
			return null;
		
		DLLNode<T> current = head;
		while (current != null && !current.getElement().equals(object))
			current = current.getNext();
		
		return current==null ? null : current.getElement();
	}
	
	/**
	 * Search the cache for an object, then remove it from the cache.
	 * @param object The object to remove.
	 * @return The object removed (null if object not found).
	 */
	public T remove(T object) {
		
		DLLNode<T> current = head;
		
		while (current != null && !current.getElement().equals(object))
			current = current.getNext();
			
		// if object not found, return null
		if(current==null)
			return null;
		
		// remove object from the cache
		if(count==1) 
			clearCache();
		else if(current==head) {
			head = head.getNext();
			head.setPrevious(null);
		}
		else if(current==tail) {
			tail = tail.getPrevious();
			tail.setNext(null);
		}
		else {
			DLLNode<T> next = current.getNext();
			DLLNode<T> prev = current.getPrevious();
			
			prev.setNext(next);
			next.setPrevious(prev);
		}
		
		count--;
		return current.getElement();
			
		
	}
	
	/**
	 * Removes the last item from the cache and return it.
	 * @return The removed object (null if the Cache was already empty).
	 */
	public T removeLast() {
		
		if (count==0)
			return null;
		
		T tmp; // the object to be returned
		if (count==1) {
			tmp = head.getElement();
			clearCache();
		}
		else {
			tmp = tail.getElement();
			tail = tail.getPrevious();
			tail.setNext(null);
		}
		
		count--;
		return tmp;
			
	}
	
	/**
	 * Adds the specified object to the top of the Cache, and removes any other references to it (if applicable). If
	 * capacity is reached, the last item in the Cache is also removed.
	 * @param object Object to be added
	 */
	public void add(T object) {
		
		DLLNode<T> newNode = new DLLNode<T>(object);
		
		remove(object); // if the object is already in the cache, remove it
		
		if(count==0)
			tail = newNode;
		else {
			head.setPrevious(newNode);
			newNode.setNext(head);
		}
		
		head = newNode;
		count++;
		
		//if the count is greater than the capacity, remove the last item from the cache
		if(count>CAPACITY)
			removeLast();
	}
	
	/**
	 * Makes the cache empty
	 */
	public void clearCache() {
		head = null;
		tail = null;
		count = 0;
	}
	
}
