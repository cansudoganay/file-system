package comp304;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DT {
	
//	An entry in DT consists of: file identifier,
//	the index of the starting block of the file and the file size.
	
	String fileIdentifier;
	int startingIndex;
	int fileSize;
	private HashMap<String, ArrayList<Integer>> directoryTable=new HashMap<>();

	private DT() {
		directoryTable= new HashMap<String, ArrayList<Integer>>();
	}
	
	public HashMap<String, ArrayList<Integer>> getTable(){
		return this.directoryTable;
	}
}
