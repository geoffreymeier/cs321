import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class will allow the user to create a new GeneBank B-Tree
 * from the command line.
 */
public class GeneBankCreateBTree {

	/**
	 * Run the program.
	 */
	public static void main(String[] args) {
		
		int cacheStatus,degree,length,cacheSize,debugLevel;	
		File filename;
		
		//Initialize variables
		try {
			cacheStatus = Integer.parseInt(args[0]);
			if (cacheStatus!=0 && cacheStatus!=1) {	//verify value of cacheStatus
				throw new IllegalArgumentException("Cache status must be 0 (without cache) or 1 (with cache)");
			}				
			degree = Integer.parseInt(args[1]);
			if (degree<0) {	//verify value of degree
				throw new IllegalArgumentException("Degree must be non-negative");
			}
			filename = new File(args[2]);
			length = Integer.parseInt(args[3]);
			if (length < 1 || length > 31) {	//verify bounds of sequence length
				throw new IllegalArgumentException("Sequence length must be between 1 and 31 (inclusive)");
			}
			cacheSize = (cacheStatus==1)?Integer.parseInt(args[4]):0;
			if (cacheStatus==1 && cacheSize < 1) {	//verify cache size if using cache
				throw new IllegalArgumentException("When using cache, a positive cache size must be specified");
			}
			debugLevel = 0; //default
			if (cacheStatus==1 && args.length==6) {
				debugLevel = Integer.parseInt(args[6]);	
			}				
			else if (cacheStatus==0 && args.length==5) {
				debugLevel = Integer.parseInt(args[5]);
			}
			
			//initialize scanner and BTree
			Scanner scan = new Scanner(filename);
			scan.useDelimiter("//s*ORIGIN//s|//s*////s");	//use delimiters ORIGIN and //
			BTree btree = new BTree(degree,length);
			
			while (scan.hasNext()) {
				String data = scan.next();
				data = data.replaceAll("[^atcgn]", "");	//process data (only keep a, t, c, g, and n)
				for (int i = 0; i <= data.length(); i++) {
					//TODO
				}
				
			}
			
			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IllegalArgumentException e) {
			e.getMessage();
			printUsage();
			System.exit(1);
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
