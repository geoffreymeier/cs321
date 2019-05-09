import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Allows the user to create and manage a BTree structure.
 */
public class BTree {

	private int degree;					//degree of the BTree
	private final int maxKeys, minKeys;	//max/min number of keys for each node			
	private int k;						//sequence length
	private BTreeNode root; 			//root node
	private RandomAccessFile file;		//Random Access File
	private final int NODE_SIZE;
	private String gbkFileName;			//the filename of the gbk file
	private boolean usingCache;				//Cache status; if its 1, we are using a cache. if 0, we are not.
	private Cache<Long> cache;
	/**
	 * Create a new BTree of TreeObjects (does not use a cache; default constructor).
	 * @param degree The degree of the tree.
	 * @param k The length of each binary sequence
	 * @param gbkFile The name of the gbk file, which will be used to create the RandomAccessFile
	 */
	public BTree(int degree, int k, String gbkFileName) {
		this.k = k;
		
		this.gbkFileName = gbkFileName;

		usingCache=false;
		//Set degree of the tree. If user specified 0, get the optimal degree
		if (degree > 0)
			this.degree = degree;
		else
			this.degree = getOptimalDegree();

		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
		NODE_SIZE = 13+8*(2*this.degree+1)+12*(2*this.degree-1);

		try {
			File tmp = new File(gbkFileName+".btree.data."+k+"."+this.degree);
			if (tmp.exists())
				tmp.delete();
			
			file = new RandomAccessFile(gbkFileName+".btree.data."+k+"."+this.degree, "rw");
			//write BTree metadata
			ByteBuffer buffer = ByteBuffer.allocate(13);
			buffer.put((byte) k);	//cast k as byte since size is limited to 31
			buffer.putInt(degree);
			buffer.putLong(13);	//root pointer will always be 13
			//byte[] array = null;
			//buffer.put(array);
			file.write(buffer.array());
			root = allocateNode();
			root.writeNode();	//write root to file to allocate space, even though it will be empty

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Error: the BTree file could not be created.");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error: could not write to BTree file.");
		}
		
		
	}
	/**
	 * Create a new BTree of TreeObjects and uses a cache.
	 * @param degree The degree of the tree.
	 * @param k The length of each binary sequence
	 * @param gbkFile The name of the gbk file, which will be used to create the RandomAccessFile
	 * @param cacheSize The size of the cache
	 */
	public BTree(int degree, int k, String gbkFileName, int cacheSize) {
		this.k = k;
		
		this.gbkFileName = gbkFileName;

		usingCache=true;
		//Set degree of the tree. If user specified 0, get the optimal degree
		if (degree > 0)
			this.degree = degree;
		else
			this.degree = getOptimalDegree();

		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
		NODE_SIZE = 13+8*(2*this.degree+1)+12*(2*this.degree-1);

		try {
			File tmp = new File(gbkFileName+".btree.data."+k+"."+this.degree);
			if (tmp.exists())
				tmp.delete();
			
			file = new RandomAccessFile(gbkFileName+".btree.data."+k+"."+this.degree, "rw");

			cache = new Cache<Long>(cacheSize);
			//write BTree metadata
			ByteBuffer buffer = ByteBuffer.allocate(13);
			buffer.put((byte) k);	//cast k as byte since size is limited to 31
			buffer.putInt(degree);
			buffer.putLong(13);	//root pointer will always be 13
			//byte[] array = null;
			//buffer.put(array);
			file.write(buffer.array());
			root = allocateNode();
			root.writeNode();	//write root to file to allocate space, even though it will be empty

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Error: the BTree file could not be created.");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error: could not write to BTree file.");
		}
		
		
	}

	/**
	 * Create a BTree object from a BTree File
	 * @param file The RandomAccessFile which contains the BTree
	 * @throws IOException If there is an error accessing the file
	 */
	public BTree(String filename) throws IOException {
		this.file = new RandomAccessFile(filename,"rw");
		file.seek(0);
		k = file.readByte();
		degree = file.readInt();
		
		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
		NODE_SIZE = 13+8*(2*degree+1)+12*(2*degree-1);
		
		root = retrieveNode(file.readLong());
		
		usingCache = false;
	}
	/**
	 * Create a BTree object from a BTree File
	 * @param file The RandomAccessFile which contains the BTree
	 * @throws IOException If there is an error accessing the file
	 */
	public BTree(String filename,int cacheSize) throws IOException {
		this.file = new RandomAccessFile(filename,"rw");
		file.seek(0);
		k = file.readByte();
		degree = file.readInt();
		
		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
		NODE_SIZE = 13+8*(2*degree+1)+12*(2*degree-1);
		
		root = retrieveNode(file.readLong());
		
		usingCache = true;
		cache = new Cache<Long>(cacheSize);
	}


	/**
	 * Insert a sequence into the BTree
	 * @param sequence
	 * @throws IOException 
	 */
	public void BTreeInsert(String sequence) throws IOException {
		if(sequence.contains("n"))
			return;
		TreeObject newObject = new TreeObject(sequence, k);
		BTreeNode r = root;
		BTreeNode foundNode = null;
		if(usingCache)
		{
			//Two cases: one where the object exists within the cache, one where it doesn't

			//CASE 1: Exists in the cache
			foundNode = cache.find(newObject.getKey());
			if(foundNode != null)
			{
				//Step 1: Increment the frequency
				for(int i=0;i<foundNode.getNumKeys();i++) 
				{
					//Iterate through the list of keys until we find the sequence
					if(foundNode.getTreeObject(i).getSequence().equals(sequence))
					{
						foundNode.getTreeObject(i).incrementFrequency();
						foundNode.writeNode(); //Write back to file
						break;
					}
				}

				//Step 2: Remove the node from the cache
				Long nodePointer = new Long(foundNode.getCurrentPointer());
				Long pointerObject = cache.remove(nodePointer);

				//Long pointerObject = new Long(foundNode.getCurrentPointer());
				//Step 3: Add to the (top) cache
				cache.add(pointerObject);
				return;
			}
		}
		if(root.getNumKeys() == maxKeys) 
		{

			//CASE 2: Does not exist in the cache
			BTreeNode s = allocateNode();
			root = s;
			s.setLeaf(false);
			s.addChild(0,r.getCurrentPointer());
			BTreeSplit(s, 0, r);
			BTreeInsertNonfull(s, newObject);

		} else {
			BTreeInsertNonfull(r, newObject);
		}
	}
	
	
	/**
	 * Helper method for BTreeInsert that will split the specified
	 * node.
	 */
	private void BTreeSplit(BTreeNode parent, int childIndex, BTreeNode child) {
		BTreeNode newNode = allocateNode();
		newNode.setLeaf(child.isLeaf());
		
		//split half of child node to new node z
		for(int i = 0; i < minKeys; i++) {
			newNode.addTreeObject(child.removeTreeObject(minKeys+1), i);
		}
		//if newNode is not a leaf then 
		if(child.isLeaf() == false) {
			for(int j = 0; j < degree; j++) {
				newNode.addChild(child.removeChild(degree));
			}
		}
		
		//move all parent's keys and children over one to allow for insertion
		//of node being moved up from child
		//child.setNumKeys(minKeys);
		/*for(int j = parent.getNumKeys()+childIndex; j >= childIndex+1; j--) {
			parent.addChild(j+1, parent.getChild(j));
		}*/
		//insert child pointer of new node to parent node
		parent.addChild(childIndex+1, newNode.getCurrentPointer());
		/*for(int m = parent.getNumKeys(); m >= childIndex; m--) {
			parent.addTreeObject(parent.removeTreeObject(m), m+1);
		}*/
		//insert child key being moved up to parent
		parent.addTreeObject(child.removeTreeObject(degree-1), childIndex);
		
		/*
		 * write changes of parent, child, and newNode  nodes to .gbk file*/
		 parent.writeNode();
		 child.writeNode();
		 newNode.writeNode();
		 
	}
	
	/**
	 * Inserts TreeObject into a node that isn't full
	 * @param node
	 * @param object
	 * @throws IOException
	 */
	private void BTreeInsertNonfull(BTreeNode node, TreeObject object) throws IOException {
		//TODO
		int i = node.getNumKeys();
		
		//check for empty node (should only happen when inserting to root at beginning
		if (i==-1) {
			node.addTreeObject(object, 0);
			if(usingCache)
			{
				Long nodePointer = new Long(node.getCurrentPointer());
				cache.add(nodePointer);
			}
			return;
		}
		
		if(node.isLeaf()) {
			//key in newObject < currentObject
			while(i >= 1 && object.getKey() < node.getTreeObject(i-1).getKey()) {
				i--;
			}
			//if key already exists, increment frequency. else, add object into node
			if (i!=0 && object.getKey() == node.getTreeObject(i-1).getKey()) {
				node.getTreeObject(i-1).incrementFrequency();
			}
			else {			
				node.addTreeObject(object, i);
			}
			
			//disk-write(node);
			node.writeNode();
		} else {
			while(i > 0 && object.getKey() < node.getTreeObject(i-1).getKey()) {
				i--;
			}
			if(i!=0 && object.getKey() == node.getTreeObject(i-1).getKey()) {
				node.getTreeObject(i-1).incrementFrequency();
				node.writeNode();
				if(usingCache)
				{
					Long nodePointer = new Long(node.getCurrentPointer());
					cache.add(nodePointer);
				}
				return;
			}
			
			//read node
			BTreeNode childNode = retrieveNode(node.getChild(i));
			//if not a leaf then recursively 
			if(childNode.getNumKeys() == maxKeys) {
				BTreeSplit(node, i, childNode);
				if(object.getKey() > node.getTreeObject(i).getKey()) {
					i++;
					childNode = retrieveNode(node.getChild(i));
				}
				else if (object.getKey() == node.getTreeObject(i).getKey()) {
					node.getTreeObject(i).incrementFrequency();
					node.writeNode();
					if(usingCache)
					{
						Long nodePointer = new Long(node.getCurrentPointer());
						cache.add(nodePointer);
					}
					return;
				}
			}
			BTreeInsertNonfull(childNode, object);
		}
		if(usingCache)
		{
			Long nodePointer = new Long(node.getCurrentPointer());
			cache.add(nodePointer);
		}
	}

	/**
	 * Finalizes the BTree file. ALWAYS call this method when finished with
	 * BTree operations.
	 * @throws IOException If RandomAccessFile cannot be accessed.
	 */
	public void finalize() throws IOException {
		//write the root to file, then close file to prevent further changes
		root.writeNode();
		file.seek(5);
		file.writeLong(root.getCurrentPointer());
	}

	/**
	 * Creates a dump file of the BTree.
	 * @throws FileNotFoundException If there is an error creating the dump file.
	 */
	public void createDumpFile() throws FileNotFoundException {
		System.setOut(new PrintStream(gbkFileName+".btree.dump."+k));
		inOrderTraversal(root);
		System.setOut(System.out);
	}

	/**
	 * Performs an in order traversal of the tree and prints each node. Meant to be 
	 * used in conjunction with createDumpFile()
	 * @param x Root of tree
	 */
	public void inOrderTraversal(BTreeNode x) {
		
		try {

			for(int i=0;i<x.getNumKeys();i++) {
				if (!x.isLeaf())
					inOrderTraversal(retrieveNode(x.getChild(i)));
				TreeObject obj = x.getTreeObject(i);
				System.out.println(obj.getSequence()+": "+obj.getFrequency());
			}

			if (!x.isLeaf())
				inOrderTraversal(retrieveNode(x.getChild(x.getNumChildren()-1)));

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error - traversal failed to access node.");
		}
	}

	/**
	 * allocate space in BTree file for a new node
	 * @return
	 */
	private BTreeNode allocateNode() {
		BTreeNode newNode;
		try {
			newNode = new BTreeNode();
			return newNode;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error: New node could not be created.");
			return null;
		}

	}

	/**
	 * Return the optimal degree for the BTree based on a disk
	 * block size of 4096 and the size of a BTreeNode.
	 * @return
	 */
	private int getOptimalDegree() {
		// 13+8(2t+1)+12(2t-1) = 40t+9
		// 40t+9 <= 4096
		// t <= (4096-9)/40
		// t = floor((4096-9)/40)
		return (4096-9)/40;
	}
	/**
	 * Given a pointer, return the node at that pointer
	 * @param pointer
	 * @return The node at the given pointer (assumes the pointer value is correct)
	 * @throws IOException If there is an error accessing the file
	 */
	private BTreeNode retrieveNode(long pointer) throws IOException {
		BTreeNode node = new BTreeNode();
		file.seek(pointer);
		byte[] array = new byte[NODE_SIZE];
		file.read(array);

		ByteBuffer buffer = ByteBuffer.wrap(array);

		byte leaf = buffer.get();
		int numKeys = buffer.getInt();
		long currentPointer = buffer.getLong();
		long parentPointer = buffer.getLong();

		node.setLeaf((leaf==0)?false:true);
		node.setCurrent(currentPointer);
		node.setParent(parentPointer);

		//add the child pointers
		for (int i=0;i<2*degree;i++) {
			if (i<numKeys+1 && !node.isLeaf())
				node.addChild(buffer.getLong());
			else 
				buffer.getLong();	//ignore this long in the buffer
		}

		//add the TreeObjects
		for (int i=0;i<2*degree-1;i++) {
			if (i<numKeys) {
				TreeObject object = new TreeObject(buffer.getLong(),k);
				object.setFrequency(buffer.getInt());
				node.addTreeObject(object,i);
			}
		}

		return node;
	}
	
	/**
	 * Searches for a sequence within a BTree
	 * and returns the frequency of the sequence
	 * @param key
	 * @return
	 * @throws IOException 
	 */
	public int BTreeSearch(BTreeNode searchNode, String sequence) throws IOException {
		TreeObject compareObject = new TreeObject(sequence, k);
		long key = compareObject.getKey();
		int i = 0;
		
		//Two cases: one where the object exists within the cache and one where it does not
		if(usingCache)
		{
			//CASE 1: Exists within the cache
			BTreeNode foundNode = cache.find(compareObject.getKey());
			if(foundNode != null)
			{
				//Step 1: Get the frequency
				int frequency=0;
				for(int j=0;j<foundNode.getNumKeys();j++) 
				{
					//Iterate through the list of keys until we find the sequence
					if(foundNode.getTreeObject(j).getSequence().equals(sequence))
					{
						frequency = foundNode.getTreeObject(j).getFrequency();
						break;
					}
				}

				//Step 2: Remove from the cache
				Long currentPointer = new Long(foundNode.getCurrentPointer());
				Long pointerObject = cache.remove(currentPointer);
				cache.add(pointerObject);
				
				return frequency;
			}
		}
		//CASE 2: Does not exist within the cache
		while(i < searchNode.getNumKeys() && key > searchNode.getTreeObject(i).getKey()) {
			i++;
		}
		//If we found the sequence
		if(i < searchNode.getNumKeys() && key == searchNode.getTreeObject(i).getKey()) {
			if(usingCache)
			{
				Long nodePointer = new Long(searchNode.getCurrentPointer());
				cache.add(nodePointer);
			}
			return searchNode.getTreeObject(i).getFrequency();
		}
		if(searchNode.isLeaf()) {
			//Not found in file; do nothing to the cache;
			return 0;
		} else {
			BTreeNode newSearchNode = retrieveNode(searchNode.getChild(i));
			return BTreeSearch(newSearchNode, sequence);
		}
	}
	
	public BTreeNode getRoot() {
		return root;
	}

	

/* ****** B-TREE NODE ************************************************************************ */

	/**
	 * BTree Node represents a single node in a BTree. This
	 * class is an inner class of BTree per CS321 Project 4
	 * requirements.
	 */
	public class BTreeNode 
	{
		ArrayList<TreeObject> BtreeNode;
		ArrayList<Long> children;
		long parent,currentNode;
		boolean full, leaf;
		int max;

		/**
		 * Constructor for the BTree node
		 * @throws IOException 
		 */
		public BTreeNode() throws IOException
		{
			BtreeNode = new ArrayList<TreeObject>();
			children = new ArrayList<Long>();
			leaf = true;
			full = false;
			currentNode = file.length();
			this.writeNode();
		}
		/**
		 * Adds a TreeObject to the node
		 */
		public void addTreeObject(TreeObject t, int index)
		{
			if(!full) 
			{
				BtreeNode.add(index,t);
				if(BtreeNode.size() == max)
				{
					full = true;
				}
			}
		}
		/**
		 * Removes specified TreeObject within the BTree node.
		 * 
		 * @return the removed TreeObject
		 */
		public TreeObject removeTreeObject(int index)
		{
			//Change this to specify index?
			//BtreeNode.remove(t);

			return BtreeNode.remove(index);
		}
		/**
		 * Gets a TreeObject from the current BTreeNode at the specified index.
		 * @return specified TreeObject within this BTree node
		 */
		public TreeObject getTreeObject(int index)
		{
			return BtreeNode.get(index);
		}
		
		/**
		 * 
		 * @return pointer to one of the children of this BTree node
		 */
		public long getChild(int k)
		{
			return children.get(k);
		}
		
		/**
		 * Return number of children
		 * @return
		 */
		public int getNumChildren() {
			return children.size();
		}

		public long getCurrentPointer() {
			return currentNode;
		}

		public void setParent(long t) {
			parent = t;
		}

		public void setCurrent(long pointer) {
			currentNode = pointer;
		}

		public void addChild(int pos, long nodePointer) 
		{
			children.add(pos,nodePointer);
		}

		public void addChild(long nodePointer) {
			children.add(nodePointer);
		}

		public long removeChild(int index) 
		{
			return children.remove(index); 
		}

		public void setLeaf(boolean l) 
		{
			leaf = l;
		}

		public int getNumKeys() 
		{
			//The size of the list of elements is equivalent to the number of keys within the node because every element only has one key.
			return BtreeNode.size();
		}

		/**
		 * 
		 * @return true if BTree node is a leaf, false otherwise
		 */
		public boolean isLeaf()
		{
			return leaf;
		}

		/**
		 * Write node to the BTree file. This method should ALWAYS be called after making
		 * changes to a node
		 */
		public void writeNode() {
			ByteBuffer buffer = ByteBuffer.allocate(NODE_SIZE);	//metadata + parent/child pointers + objects

			try {
				//put metadata in buffer
				buffer.put((byte) (leaf?1:0));
				buffer.putInt(BtreeNode.size());
				buffer.putLong(currentNode);

				//put pointers in buffer
				buffer.putLong(parent);
				for (int i=0;i<2*degree;i++) {
					if (i<children.size())
						buffer.putLong(children.get(i));
					else
						buffer.putLong((long) 0);
				}

				//put objects in buffer
				for (int i=0;i<2*degree-1;i++) {
					if (i<BtreeNode.size()) {
						buffer.putLong(BtreeNode.get(i).getKey());
						buffer.putInt(BtreeNode.get(i).getFrequency());
					}
					else {
						buffer.putLong((long) 0);
						buffer.putInt(0);
					}
				}

				//write buffer's contents to file		
				file.seek(currentNode);
				//byte[] array = null;
				buffer.flip();
				//buffer.put(array);
				//file.write(array);
				file.write(buffer.array());
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error: Failed to write node to file.");
			}			
		}
	}
/* ****** CACHE ************************************************************************ */
	
	/**
	 * This class allows the user to create and manage a cache.
	 * 
	 * @author Geoffrey Meier
	 *
	 */
	public class Cache<T> {
		
		private int size;
		private int CAPACITY;
		private DLLNode<Long> head, tail;
		private BTree bTree;
		
		
		/**
		 * The constructor for a new empty Cache.
		 * @param size The maximum size (capacity) of the Cache.
		 */
		public Cache(int size) {
			this.size = 0;
			CAPACITY = size;
			head = null;
			tail = null;
		}

		/**
		 * Search the cache for the specified object.
		 * @param object The object to search for.
		 * @return The returned object (null if object not found).
		 */
		public BTreeNode find(long sequence) {
			
			if (size==0)
				return null;
			
			BTreeNode foundNode = null;
			//Cache is supposed to store BTreeNode objects
			DLLNode<Long> current = head;
			while (current != null)
			{
				long currentNodePointer = (long) current.getElement();
				int numKeys;
				try {
					BTreeNode currentNode = retrieveNode(currentNodePointer);
					numKeys = currentNode.getNumKeys();
					//Need a forloop here to iterate through each key in the BTreeNode
					for(int i=0;i<numKeys;i++)
					{
						if(currentNode.getTreeObject(i).getKey() == sequence)
						{
							//We found the sequence
							foundNode = currentNode;
							return foundNode; //Immediately return so we don't check the entire cache and don't check any other keys 
						}
					}
					current = current.getNext();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			//Object was not found in the cache; return null
			return null;
		}
		
		/**
		 * Search the cache for an object, then remove it from the cache.
		 * @param object The object to remove.
		 * @return The object removed (null if object not found).
		 */
		public long remove(Long bTreeNodePointer) {
			
			DLLNode<Long> current = head;
			
			/* We don't need to look for the sequence in this method; If this method is called,
			 * the bTreeNode that contains the sequence exists within the cache. This also
			 * means that this method will never return -1.
			 */
			while (current != null && !current.getElement().equals(bTreeNodePointer))
			{
				current = current.getNext();
			}
				
			// if the pointer not found, return -1
			if(current==null)
				return -1;
			
			// remove object from the cache
			if(size==1) 
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
				DLLNode<Long> next = current.getNext();
				DLLNode<Long> prev = current.getPrevious();
				
				prev.setNext(next);
				next.setPrevious(prev);
			}
			
			size--;
			return (long) current.getElement();
		}
		
		/**
		 * Removes the last item from the cache and return it.
		 * @return The removed object (null if the Cache was already empty).
		 */
		public Long removeLast() {
			
			if (size==0)
				return null;
			
			Long tmp; // the object to be returned
			if (size==1) {
				tmp = head.getElement();
				clearCache();
			}
			else {
				tmp = tail.getElement();
				tail = tail.getPrevious();
				tail.setNext(null);
			}
			
			size--;
			return tmp;
				
		}
		
		/**
		 * Adds the specified object to the top of the Cache, and removes any other references to it (if applicable). If
		 * capacity is reached, the last item in the Cache is also removed.
		 * @param object Object to be added
		 */
		public void add(Long pointerObject) {
			
			DLLNode<Long> newNode = new DLLNode<Long>(pointerObject);
			
			//not needed; add will only be called if the sequence is not in the cache
			//remove(object); // if the object is already in the cache, remove it
			
			if(size==0)
				tail = newNode;
			else {
				head.setPrevious(newNode);
				newNode.setNext(head);
			}
			
			head = newNode;
			size++;
			
			//if the size is greater than the capacity, remove the last item from the cache
			if(size>CAPACITY)
				removeLast();
		}
		
		/**
		 * 
		 * @return true if the cache if full, false otherwise
		 */
		public boolean isFull()
		{
			return (size==CAPACITY);
		}
		/**
		 * Makes the cache empty
		 */
		public void clearCache() {
			head = null;
			tail = null;
			size = 0;
		}
		
	}
}
