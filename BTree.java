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
		
		this.gbkFileName = gbkFileName;

		//Set degree of the tree. If user specified 0, get the optimal degree
		if (degree > 0)
			this.degree = degree;
		else
			this.degree = getOptimalDegree();

		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
		NODE_SIZE = 13+8*(2*this.degree+1)+12*(2*this.degree-1);

		try {
			file = new RandomAccessFile(gbkFileName+".btree.data."+k+"."+this.degree, "rw");
			root = allocateNode();

			//write BTree metadata
			ByteBuffer buffer = ByteBuffer.allocate(13);
			buffer.put((byte) k);	//cast k as byte since size is limited to 31
			buffer.putInt(degree);
			buffer.putLong(root.getCurrentPointer());
			//byte[] array = null;
			//buffer.put(array);
			file.write(buffer.array());
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
		if(sequence.contains("n"))
			return;
		TreeObject newObject = new TreeObject(sequence, k);
		BTreeNode r = root;
		if(root.getNumKeys() == maxKeys) {
			//numNodes++;
			BTreeNode s = allocateNode();
			root = s;
			s.setLeaf(false);
			s.addChild(0,r.getCurrentPointer());
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
				newNode.addChild(child.removeChild(j+1));
			}
		}
		
		//move all parent's keys and children over one to allow for insertion
		//of node being moved up from child
		//child.setNumKeys(minKeys);
		for(int k = parent.getNumKeys(); k >= childIndex+1; k--) {
			parent.addChild(k+1, parent.getChild(k));
		}
		//insert child pointer of new node to parent node
		parent.addChild(childIndex, newNode.getCurrentPointer());
		for(int m = parent.getNumKeys(); m >= childIndex; m--) {
			parent.addTreeObject(parent.removeTreeObject(m), m+1);
		}
		//insert child key being moved up to parent
		parent.addTreeObject(child.getTreeObject(degree-1), childIndex-1);
		
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
		if(node.isLeaf()) {
			//key in newObject < currentObject
			while(i >= 1 && object.getKey() < node.getTreeObject(i-1).getKey()) {
				node.addTreeObject(object, i);
				i--;
			}
			node.addTreeObject(object, i);
			
			//disk-write(node);
			node.writeNode();
		} else {
			while(i>=1 && object.getKey() < node.getTreeObject(i-1).getKey()) {
				i--;
			}
			i++;
			
			//read node
			BTreeNode childNode = retrieveNode(node.getChild(i-1));
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
		inOrderTraversal(root);
		System.setOut(System.out);
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
				inOrderTraversal(retrieveNode(x.getChild(i)));
				TreeObject obj = x.getTreeObject(i);
				System.out.println(obj.getSequence()+": "+obj.getFrequency());


			}

			inOrderTraversal(retrieveNode(x.getChild(x.getNumKeys())));
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
			if (i<numKeys+1)
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
		while(i < searchNode.getNumKeys() && key > searchNode.getTreeObject(i).getKey()) {
			i++;
		}
		if(i < searchNode.getNumKeys() && key == searchNode.getTreeObject(i).getKey()) {
			return searchNode.getTreeObject(i).getFrequency();
		}
		if(searchNode.isLeaf()) {
			return 0;
		} else {
			BTreeNode newSearchNode = retrieveNode(searchNode.getChild(i));
			return BTreeSearch(newSearchNode, sequence);
		}
	}
	
	public BTreeNode getRoot() {
		return root;
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
		 * 
		 * @return pointer to one of the children of this BTree node
		 */
		public long getChild(int k)
		{
			return children.get(k);
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

		//There are two "addChild" methods here; what to do with them?
		public void addChild(int pos, long nodePointer) 
		{
			children.add(pos,nodePointer);
		}
		/*public void addChild(BTreeNode t) 
		{
			children.add(t.getCurrentPointer());
		}*/

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
}
