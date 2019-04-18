/**
 * Allows the user to create and manage a BTree structure.
 */
public class BTree {
	
	private int degree;	//degree of the BTree
	private int k;		//sequence length

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
	}
	
	/**
	 * Insert a sequence into the BTree
	 * @param sequence
	 */
	public void BTreeInsert(String sequence) {
		//TODO
	}
	
	
	/**
	 * Helper method for BTreeInsert that will split the specified
	 * node.
	 */
	private void BTreeSplit(BTreeNode node) {
		//TODO
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
	private class BTreeNode {
		//TODO
	}
}
