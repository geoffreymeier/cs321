/**
 * Object containing a given DNA sequence and the frequency of that sequence. This
 * class is meant to be used in a tree structure. 
 * 
 * *Note: the DNA sequence is internally converted to binary to help save space.
 * @author geoffreymeier
 *
 */
public class TreeObject {
	
	private long binSequence;	//The binary sequence
	private int frequency;		//The frequency of the Object
	
	/**
	 * Create a new TreeObject containing a given DNA sequence. Frequency is set
	 * to one at initialization.
	 * @param sequence The DNA sequence.
	 */
	public TreeObject(String sequence) {
		//change to lower case
		sequence = sequence.toLowerCase();
		
		//throw exception if sequence is incorrectly formatted
		if (!(sequence.contains("a") || sequence.contains("t") || sequence.contains("c") || sequence.contains("g"))) 
			throw new IllegalArgumentException("The sequence can only contain A, T, C, or G");
		
		//change sequence to string of bits
		sequence.replace("a", "00");
		sequence.replace("t", "11");
		sequence.replace("c", "01");
		sequence.replace("g", "10");
		
		//parse the sequence into an integer
		binSequence = Long.parseLong(sequence, 2);	
		
		//set frequency to 1
		frequency = 1;
	}
	
	/**
	 * Create a new TreeObject with the given sequence. The input sequence 
	 * should be in binary such that:
	 * A = 00,
	 * T = 11,
	 * C = 01,
	 * G = 10.
	 * @param binSequence The DNA sequence in binary format.
	 */
	public TreeObject(long binSequence) {
		this.binSequence = binSequence;	
		frequency = 1;
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
		
		//convert binary sequence to String of A, T, C, and G
		for (int i=0; i<binSequence.length()-1; i+=2) {
			switch (binSequence.substring(i, i+2)) {
			case "00":
				sequence += "A";
				break;
			case "11":
				sequence += "T";
				break;
			case "01":
				sequence += "C";
				break;
			case "10":
				sequence += "G";
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
}
