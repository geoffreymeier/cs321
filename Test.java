import java.io.FileNotFoundException;
import java.io.IOException;

public class Test {

	public static void main(String[] args) {
		BTree btree = new BTree(2, 2, "test");
		
		String[] sequences = {"aa","at","ac","ag","ta","tt","tc","tg","ca","ct","cc","cg","ga","gt","gc","gg"};
		
		try {
		for (String s : sequences) {
			btree.BTreeInsert(s);
			btree.createDumpFile();
			System.out.println("");
		}

		btree.finalize();
		btree.createDumpFile();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
