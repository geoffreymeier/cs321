/**
 * This class will allow the user to create a new GeneBank B-Tree
 * from the command line.
 */
public class GeneBankCreateBTree {

	/**
	 * Run the program.
	 */
	public static void main(String[] args) {
		if (args.length < 4 || args.length > 6) {
			printUsage();
			System.exit(0);
		}

	}
	
	/**
	 * Helper method which will print the usage statement to the
	 * console. 
	 */
	private static void printUsage() {
		System.out.println("Usage: java GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]"
				+ "\nNote: If using with Cache, cache size must be specified.");
	}
}
