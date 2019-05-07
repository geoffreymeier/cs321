/**
 * Object containing a given DNA sequence and the frequency of that sequence. This
 * class is meant to be used in a tree structure. 
 * 
 * *Note: the DNA sequence is internally converted to binary to help save space.
 */
public class TreeObject {
	
	private long binSequence;	//The binary sequence
	private int frequency;		//The frequency of the Object
	private int k;				//The length of the sequence
	private final String VALID_CHARS = "atcg";	//valid characters in a DNA sequence
	
	/**
	 * Create a new TreeObject containing a given DNA sequence. Frequency is set
	 * to one at initialization.
	 * @param sequence The DNA sequence.
	 * @param k The length of the sequence; this should be equal to the length 
	 * of String sequence parameter. Range: [1,31]
	 */
	public TreeObject(String sequence, int k) {
		//change to lower case
		sequence = sequence.toLowerCase();
		
		//check for illegal arguments and throw exceptions if necessary.
		if (k<1 || k>31)
			throw new IllegalArgumentException("Parameter 'k' is out of bounds [1,31].");
		if (sequence.length()!=k)
			throw new IllegalArgumentException("The length of the sequence should be equal to k");
		for (int i=0; i<sequence.length(); i++) {
			if (!VALID_CHARS.contains(sequence.substring(i,i+1))) 
				throw new IllegalArgumentException("The sequence can only contain A, T, C, or G");
		}
		
		//change sequence to string of bits
		sequence = sequence.replace("a", "00");
		sequence = sequence.replace("t", "11");
		sequence = sequence.replace("c", "01");
		sequence = sequence.replace("g", "10");
		
		//parse the sequence into an integer
		binSequence = Long.parseLong(sequence, 2);	
		
		//set k and frequency
		frequency = 1;
		this.k = k;
	}
	
	/**
	 * Create a new TreeObject with the given sequence. The input sequence 
	 * should be in binary such that:
	 * A = 00,
	 * T = 11,
	 * C = 01,
	 * G = 10.
	 * 
	 * *Note: If the number of bits in binSequence exceed 2*k, then any
	 * extra bits will be truncated.
	 * @param binSequence The DNA sequence in binary format.
	 * @param k The length of the sequence. This should be equal to the 
	 * number of digits in binSequence parameter divided by 2. Range: [1,31]
	 */
	public TreeObject(long binSequence, int k) {		
		if (k<1 || k>31)
			throw new IllegalArgumentException("Parameter 'k' is out of bounds [1,31].");
		
		this.binSequence = binSequence & (~(~0<<(2*k))); //force size of k on binSequence
		frequency = 1;
		this.k = k;
	}
	
	/**
	 * Return the key (DNA sequence in binary format).
	 */
	public long getKey() {
		return binSequence;
	}
	
	/**
	 * Return the DNA sequence in readable format.
	 */
	public String getSequence() {
		String sequence = "";
		String binSequence = Long.toBinaryString(this.binSequence);	//get the binary sequence
		
		while(binSequence.length()<2*k)
		{
			//insert 0's in front of the existing data to ensure it matches the expected length.
			binSequence = "0" + binSequence;
		}
		//convert binary sequence to String of A, T, C, and G
		for (int i=2*k; i>=2; i-=2) {
			switch (binSequence.substring(i-2, i)) {
			case "00":
				sequence = "a"+sequence;
				break;
			case "11":
				sequence = "t"+sequence;
				break;
			case "01":
				sequence = "c"+sequence;
				break;
			case "10":
				sequence = "g"+sequence;
			}
		}
		
		return sequence;
	}

	/**
	 * Increment the frequency of this DNA sequence.
	 */
	public void incrementFrequency() {
		frequency++;
	}
	
	/**
	 * Return the frequency of this DNA sequence.
	 */
	public int getFrequency() {
		return frequency;
	}
	
	/**
	 * Set the frequency of the sequence. *CAUTION*: only use when retrieving node from file.
	 * @param freq The new frequency
	 */
	public void setFrequency(int freq) {
		frequency = freq;
	}
}
