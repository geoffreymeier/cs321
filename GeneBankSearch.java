import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Allows the user to search a BTree using a btree and query file. *
 */
public class GeneBankSearch {

	public static void main(String[] args) {
		int cacheStatus, cacheSize, debugLevel;
		String btreeFileName;
		File query;

		try {
			//initialize variables and check for input argument errors
			cacheStatus = Integer.parseInt(args[0]);
			if (cacheStatus!=0 && cacheStatus!=1) {	//verify value of cacheStatus
				throw new IllegalArgumentException("Cache status must be 0 (without cache) or 1 (with cache)");
			}	
			btreeFileName = args[1];
			BTree btree = new BTree(btreeFileName);
			query = new File(args[2]);
			cacheSize = (cacheStatus==1)?Integer.parseInt(args[3]):0;
			if (cacheStatus==1 && cacheSize < 1) {	//verify cache size if using cache
				throw new IllegalArgumentException("When using cache, a positive cache size must be specified");
			}
			debugLevel = 0; //default - not actually implemented for this program.
			if (cacheStatus==1 && args.length==6) {
				debugLevel = Integer.parseInt(args[6]);	
			}				
			else if (cacheStatus==0 && args.length==5) {
				debugLevel = Integer.parseInt(args[5]);
			}
			
			//begin scanning file
			Scanner scan = new Scanner(query);
			while(scan.hasNext()) {
				String sequence = scan.next();
				int freq = btree.BTreeSearch(btree.getRoot(),sequence);
				System.out.println(sequence+": "+freq);				
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Error: Please make sure that the filename is valid.");
			e.printStackTrace();
		} catch(IllegalArgumentException e) {
			e.getMessage();
			printUsage();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Error: Could not properly access B-Tree file.");
			e.printStackTrace();
		}

	}
	
	/**
	 * Helper method which will print the usage statement to the
	 * console. 
	 */
	private static void printUsage() {
		System.out.println("Usage: java GeneBankSearch <0/1(no/with Cache)> <btree file> <query file> [<cache size>] [<debug level>]"
				+ "\nNote: If using with Cache, cache size must be specified.");
	}
}
