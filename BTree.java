/**
 * Allows the user to create and manage a BTree structure.
 */
public class BTree {
	
	private int degree;					//degree of the BTree
	private int maxKeys, minKeys;		//max/min number of keys for each node			
	private int k;						//sequence length
	private BTreeNode root; 			//root node

	/**
	 * Create a new BTree of TreeObjects. 
	 * @param degree The degree of the tree.
	 * @param k The length of each binary sequence
	 */
	public BTree(int degree, int k) {
		this.k = k;
		
		//Set degree of the tree. If user specified 0, get the optimal degree
		if (degree > 0)
			this.degree = degree;
		else
			this.degree = getOptimalDegree();
		
		maxKeys = (2*this.degree) - 1;
		minKeys = this.degree -1;
	}
	
	/**
	 * Insert a sequence into the BTree
	 * @param sequence
	 */
	public void BTreeInsert(String sequence) {
		//TODO
		TreeObject newObject = new TreeObject(sequence, 10);
		BTreeNode r = root;
		if(root.getNumKeys() == maxKeys) {
			BTreeNode s = allocateNode();
			root = s;
			s.setIsLeaf(false);
			s.setNumKeys(0);
			s.setChild(1, r);
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
		BTreeNode z = allocateNode();
		z.setIsLeaf(child.isLeaf());
		z.setNumKeys(minKeys);
		
		//split half of child node to new node z
		for(int i = 1; i <= minKeys; i++) {
			z.setKey(i,child.getKey(i+1));
		}
		if(child.isLeaf() == false) {
			for(int j = 1; j <= degree; j++) {
				z.setChild(j, child.getChild(j+1));
			}
		}
		
		//move all parent's keys and children over one to allow for insertion
		//of node being moved up from child
		child.setNumKeys(minKeys);
		for(int k = parent.getNumKeys()+1; k >= childIndex+1; k--) {
			parent.setChild(k+1, parent.getChild(k));
		}
		//insert child pointer of new node z to parent node
		parent.setChild(childIndex+1, z);
		for(int m = parent.getNumKeys(); m >= childIndex; m--) {
			parent.setKey(m+1, parent.getKey(m));
		}
		//insert child key being moved up to parent
		parent.setKey(childIndex, child.getKey(degree));
		parent.setNumKeys(parent.getNumKeys()+1);
		
		/*
		 * disk write parent, child, and z  nodes
		 * disk_write(parent);
		 * disk_write(child);
		 * disk_write(z);
		 */
	}
	
	private void BTreeInsertNonfull(BTreeNode node, TreeObject object) {
		//TODO
		int i = node.getNumKeys();
		if(node.isLeaf()) {
			while(i >= 1 && object.getKey() < node.getObject(i).getKey()) {
				node.setKey(i+1, node.getKey(i));
				i--;
			}
			node.setKey(i+1, object);
			node.setNumKeys(node.getNumKeys()+1);
			//disk-write(node);
		} else {
			while(i>=1 && object.getKey() < node.getObject(i).getKey()) {
				i--;
			}
			i++;
			//disk-read(node.getChild(i));
			if(node.getChild(i).getNumKeys() == maxKeys) {
				BTreeSplit(node, i , node.getChild(i));
				if(object.getKey() > node.getObject(i).getKey()) {
					i++;
					BTreeInsertNonfull(node.getChild(i), object)
				}
			}
		}
	}
	
	private BTreeNode allocateNode() {
		BTreeNode newNode = new BTreeNode(degree);
		return newNode;
	}
	
	/**
	 * Return the optimal degree for the BTree based on a disk
	 * block size of 4096 and the size of a BTreeNode.
	 * @return
	 */
	private int getOptimalDegree() {
		//TODO
		return -1;
	}
		
	/**
	 * BTree Node represents a single node in a BTree. This
	 * class is an inner class of BTree per CS321 Project 4
	 * requirements.
	 */
	private class BTreeNode 
	{
		ArrayList<TreeObject> BtreeNode;
		ArrayList<BTreeNode> children;
		BTreeNode parent;
		boolean full, leaf;
		int max;
		/**
		 * Constructor for the BTree node
		 */
		BTreeNode(int treeHeight)
		{
			BtreeNode = new ArrayList<TreeObject>();
			children = new ArrayList<BTreeNode>();
			leaf = true;
			full = false;
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
			BTreeNode nodeObj = parent;
			//fix this
			long offset = nodeObj.hashCode();
			return offset;
		}
		/**
		 * 
		 * @return pointer to one of the children of this BTree node
		 */
		public long getChildPointer(int k)
		{
			//disk read - get offset from disk and return offset value
			BTreeNode pointerObj = children.get(k);
			
			//fix this
			long offset = pointerObj.hashCode();
			return offset;
		}
		public void setParentPointer(BTreeNode t) 
		{
			parent = t;
		}
		public void addChildPointer(BTreeNode t) 
		{
			children.add(t);
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
		public boolean getLeaf()
		{
			return false;
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
			return false;
		}
	}
}
