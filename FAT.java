package comp304;

import java.util.HashMap;
import java.util.LinkedList;

public class FAT {

	String fileIdentifier;
	int startingIndex;
	int fileSize;
	private HashMap<String,LinkedList<Integer>> fileAllocationTable=new HashMap<>();
	
}
