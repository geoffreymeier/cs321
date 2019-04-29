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

	/**
	 * Create a new BTree of TreeObjects. 
	 * @param degree The degree of the tree.
	 * @param k The length of each binary sequence
	 * @param gbkFile The name of the gbk file, which will be used to create the RandomAccessFile
	 */
	public BTree(int degree, int k, String gbkFileName) {
		this.k = k;
		root = allocateNode();
		this.gbkFileName = gbkFileName;

		//Set degree of the tree. If user specified 0, get the optimal degree
		if (degree > 0)
			this.degree = degree;
		else
			this.degree = getOptimalDegree();

		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
		NODE_SIZE = 13+8*(2*degree+1)+12*(2*degree-1);

		try {
			file = new RandomAccessFile(gbkFileName+".btree.data."+k+".degree", "rw");

			//write BTree metadata
			ByteBuffer buffer = ByteBuffer.allocate(13);
			buffer.put((byte) k);	//cast k as byte since size is limited to 31
			buffer.putInt(degree);
			buffer.putLong(root.getCurrentPointer());
			byte[] array = null;
			buffer.put(array);
			file.write(array);
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
		root = retrieveNode(file.readLong());

		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
		NODE_SIZE = 13+8*(2*degree+1)+12*(2*degree-1);
	}

	/**
	 * Insert a sequence into the BTree
	 * @param sequence
	 * @throws IOException 
	 */
	public void BTreeInsert(String sequence) throws IOException {
		//TODO
		TreeObject newObject = new TreeObject(sequence, 10);
		BTreeNode r = root;
		if(root.getNumKeys() == maxKeys) {
			numNodes++;
			BTreeNode s = allocateNode();
			root = s;
			s.setLeaf(false);
			s.addChildPointer(r);
			BTreeSplit(s, 1, r);
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
		//TODO
		BTreeNode newNode = allocateNode();
		newNode.setLeaf(child.isLeaf());
		
		//split half of child node to new node z
		for(int i = 0; i < minKeys; i++) {
			newNode.addTreeObject(child.removeTreeObject(0), i);
		}
		//if newNode is not a leaf then 
		if(child.isLeaf() == false) {
			for(int j = 0; j < degree; j++) {
				newNode.addChildPointer(child.removeChildPointer(j+1));
			}
		}
		
		//move all parent's keys and children over one to allow for insertion
		//of node being moved up from child
		//child.setNumKeys(minKeys);
		for(int k = parent.getNumKeys(); k >= childIndex+1; k--) {
			parent.addChildPointer(k+1, parent.getChildPointer(k));
		}
		//insert child pointer of new node to parent node
		parent.addChildPointer(childIndex+1, newNode);
		for(int m = parent.getNumKeys(); m >= childIndex; m--) {
			parent.addTreeObject(parent.removeTreeObject(m), m+1);
		}
		//insert child key being moved up to parent
		parent.addTreeObject(child.getTreeObject(degree), childIndex);
		
		/*
		 * write changes of parent, child, and newNode  nodes to .gbk file*/
		 parent.writeNode();
		 child.writeNode();
		 newNode.writeNode();
		 
	}
	
	private void BTreeInsertNonfull(BTreeNode node, TreeObject object) throws IOException {
		//TODO
		int i = node.getNumKeys();
		if(node.isLeaf()) {
			while(i >= 1 && object.getKey() < node.getTreeObject(i).getKey()) {
				node.addTreeObject(node.getTreeObject(i), i+1);
				i--;
			}
			node.addTreeObject(object, i+1);
			
			//disk-write(node);
			node.writeNode();
		} else {
			while(i>=1 && object.getKey() < node.getTreeObject(i).getKey()) {
				i--;
			}
			i++;
			
			//read node
			BTreeNode childNode = retrieveNode(node.getChildPointer(i));
			//if not a leaf then recursively 
			if(childNode.getNumKeys() == maxKeys) {
				BTreeSplit(node, i , childNode);
				if(object.getKey() > node.getTreeObject(i).getKey()) {
					i++;
					BTreeInsertNonfull(childNode, object);
				}
			}
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
		file.close();
	}

	/**
	 * Creates a dump file of the BTree.
	 * @throws FileNotFoundException If there is an error creating the dump file.
	 */
	public void createDumpFile() throws FileNotFoundException {
		System.setOut(new PrintStream(gbkFileName+".btree.dump."+k));

	}

	/**
	 * Performs an in order traversal of the tree and prints each node. Meant to be 
	 * used in conjunction with createDumpFile()
	 * @param x Root of tree
	 */
	public void inOrderTraversal(BTreeNode x) {
		if (x==null)
			return;
		try {
			for(int i=0;i<x.getNumKeys();i++) {
				inOrderTraversal(retrieveNode(x.getChildPointer(i)));
				TreeObject obj = x.getTreeObject(i);
				System.out.println(obj.getSequence()+": "+obj.getFrequency());


			}

			inOrderTraversal(retrieveNode(x.getChildPointer(x.getNumKeys())));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error - traversal failed to access node.");
		}
	}

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
		node.setCurrentPointer(currentPointer);
		node.setParentPointer(parentPointer);

		//add the child pointers
		for (int i=0;i<2*degree;i++) {
			if (i<numKeys+1)
				node.addChildPointer(buffer.getLong());
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
			else {	//skip these bytes
				buffer.getLong();
				buffer.getInt();
			}
		}

		return node;
	}



	/**
	 * BTree Node represents a single node in a BTree. This
	 * class is an inner class of BTree per CS321 Project 4
	 * requirements.
	 */
	private class BTreeNode 
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
		 * Gets the parent pointer to this BTreeNode.
		 * @return pointer to the parent of this BTree node
		 */
		public long getParentPointer()
		{
			//disk read - get offset from disk and return offset value
			return parent;
		}
		/**
		 * 
		 * @return pointer to one of the children of this BTree node
		 */
		public long getChildPointer(int k)
		{
			return children.get(k);
		}

		public long getCurrentPointer() {
			return currentNode;
		}

		public void setParentPointer(BTreeNode t) 
		{
			parent = t.getCurrentPointer();
		}

		public void setParentPointer(long t) {
			parent = t;
		}

		public void setCurrentPointer(long pointer) {
			currentNode = pointer;
		}

		public void addChildPointer(BTreeNode t) 
		{
			children.add(t.getCurrentPointer());
		}

		public void addChildPointer(long nodePointer) {
			children.add(nodePointer);
		}

		public long removeChildPointer(int index) 
		{
			//disk read - get offset from disk and return offset value
			children.remove(index);
			//long offset = pointerObj()
			return 0; 
		}

		/**
		 * Method that determines whether or not a BTree node is full. This method should ALWAYS
		 * be called before attempting to add a TreeObject to a node.
		 * @return whether or not the BTreeNode is full
		 */
		public boolean isFull()
		{
			return full;
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
				byte[] array = null;
				buffer.flip();
				buffer.put(array);
				file.write(array);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error: Failed to write node to file.");
			}			
		}
	}
}
