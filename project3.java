package comp304;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class project3 {

	private static int blockSize = 2048; // given
	private static Block[] Directory = new Block[32768]; // Directory consists of 32768 blocks

	private static ArrayList<DTNode> directoryTable = new ArrayList<DTNode>();

	private static HashMap<Integer, Integer> fileAllocationTable = new HashMap<Integer, Integer>(); // blockIndex and next element

	private static HashMap<Integer, Pair<Integer, Integer>> dtForFat = new HashMap<Integer, Pair<Integer, Integer>>();
	
	private static int dt_creation_rejects=0;
	private static int dt_extension_rejects=0;
	private static int fat_creation_rejects=0;
	private static int fat_extension_rejects=0;
	private static int size=0;

	// METHODS OF DIRECTORY TABLE IMPLEMENTATION

	private static void dt_create_file(int file_id, int file_length) {
		int counter = 0;
		
		if (file_length % blockSize == 0) {
			counter = file_length / blockSize;
		} else {
			counter = (file_length / blockSize) + 1;
		}
		
		if(size+counter>Directory.length) { //it checks whether there is enough space in the directory
			System.out.println("There is not enough place for to create the file with file id: "+file_id);
			dt_creation_rejects++;
			return;
		}
		
		for(int i=0;i<directoryTable.size();i++) { //it checks if there is already a file with the same file id or not 
			if(directoryTable.get(i).getFileID()==file_id);
			dt_creation_rejects++;
			System.out.println("There is already a file with file id: "+file_id);
			return;
		}

		if (directoryTable.size() == 0) { //if the directoryTable is empty
			DTNode values = new DTNode(file_id, 0, counter);
			directoryTable.add(values);
			directoryTableSort();
			filling(0, counter);
			size+=counter;
			return;
		}
		directoryTableSort();

		int firstElement = directoryTable.get(0).getFirst();

		if (counter <= firstElement) { // first file
			DTNode node = new DTNode(file_id, 0, counter);
			directoryTable.add(node);
			directoryTableSort();
			filling(0, counter);
			size+=counter;
			return;
		}

		for (int i = 0; i < directoryTable.size() - 1; i++) { // for putting the file between two files
			int spaces = 0;
			spaces = ((directoryTable.get(i + 1).getFirst())
					- (directoryTable.get(i).getFirst() + directoryTable.get(i).getLength()));
			if (counter <= spaces) {
				DTNode newNode = new DTNode(file_id, directoryTable.get(i).getFirst() + directoryTable.get(i).getLength(), counter);
				directoryTable.add(newNode);
				directoryTableSort();
				filling(newNode.getFirst(), counter);
				size+=counter;
				return;
			}
		}

		// for putting the file to the end of the table
		directoryTableSort();
		if (32768 - (directoryTable.get(directoryTable.size() - 1).getFirst() + directoryTable.get(directoryTable.size() - 1).getLength()) >= counter) { // if there is enough space
			DTNode newNode = new DTNode(file_id, directoryTable.get(directoryTable.size() - 1).getFirst() + directoryTable.get(directoryTable.size() - 1).getLength(), counter);
			directoryTable.add(newNode);
			directoryTableSort();
			filling(newNode.getFirst(), counter);
			size+=counter;
		}

	}

	// it fills the Directory with random numbers because -1 means the block is empty and the numbers between 0-10 indicates that the block is full
	private static void filling(int startingIndex, int file_length) {
		for (int i = startingIndex; i < startingIndex + file_length; i++) {
			Random randomNumber = new Random();
			Directory[i].blockContent = randomNumber.nextInt(10);
		}
	}

	// it sorts the directory table to find the files more quick
	private static void directoryTableSort() {
		Collections.sort(directoryTable, new Comparator<DTNode>() {
			public int compare(DTNode o1, DTNode o2) {
				if (o1.getFirst() > o2.getFirst())
					return 1;
				else
					return -1;
			}
		});
	}

	private static int dt_access(int file_id, int byte_offset) {
		int loc;
		DTNode newNode = new DTNode(0, 0, 0);
		for (int i = 0; i < directoryTable.size(); i++) {
			if (directoryTable.get(i).getFileID() == file_id) {
				newNode = directoryTable.get(i);
			}
		}
		if(newNode.getLength()==0) {
			System.out.println("There is no file with file id: "+file_id);
			return 0;
		}
		
		loc = newNode.getFirst() + byte_offset / blockSize; // location of the file that is searched
		System.out.println("the location of the byte having the offset " + byte_offset + " in the directory: " + loc);
		return loc;
	}

	private static void dt_extend(int file_id, int extension) {
		directoryTableSort();
		
		if(size+extension>Directory.length) { //it checks the directory for to control if there is enough space 
			System.out.println("There is not enough place for the extension");
			dt_extension_rejects++;
			return;
		}
		
		for (int i = 0; i < directoryTable.size(); i++) {			
			if (directoryTable.get(i).getFileID() == file_id) { //target file 
				// if the file is the last file
				if (directoryTable.get(i) == directoryTable.get(directoryTable.size()-1)) {
					int oldLength = directoryTable.get(i).getLength();
					directoryTable.get(i).setLength(oldLength + extension);
					filling(directoryTable.get(i).getFirst() + oldLength, extension);
					size+=extension;
					return;
					// if the file is not the last file the code checks if that is enough space between the next file and the target file because 
					// I did not write a code for shifting
				} else if (directoryTable.get(i + 1).getFirst() - (directoryTable.get(i).getFirst() + directoryTable.get(i).getLength()) >= extension) {
					int temp = directoryTable.get(i).getLength();
					directoryTable.get(i).setLength(temp + extension);
					filling(directoryTable.get(i).getFirst() + temp, extension);
					size+=extension;
					return;
				}
			}
		}

		System.out.println("There is no file which has file ID: " + file_id); //it could not find the file
		dt_extension_rejects++;

	}


	private static void dt_shrink(int file_id, int shrinking) {
		directoryTableSort();
		for (int i = 0; i < directoryTable.size(); i++) {
			if (directoryTable.get(i).getFileID() == file_id) { //target file 
				if (directoryTable.get(i).getLength() > shrinking) {
					int oldLength = directoryTable.get(i).getLength();
					directoryTable.get(i).setLength(oldLength - shrinking);
					size-=shrinking;
					for (int j = (directoryTable.get(i).getFirst() + oldLength)- 1; j >= directoryTable.get(i).getFirst() + oldLength - shrinking; j--) {
						Directory[j].setBlockContent(-1); // -1 means going back to the initial
						return;
					}
				} else {
					System.out.println("File size could not be decreased!"); //when the shrinking value is bigger than the initial size of the file 
				}
			}
			
		}
		System.out.println("There is no file which has file ID: " + file_id); //it could not find the file 
		
	}

	// METHODS OF FILE ALLOCATION TABLE IMPLEMENTATION

	private static void fat_create_file(int file_id, int file_length) {
		int counter = 0;
		int index = 0;
		int prev = 0;

		if (file_length % blockSize == 0) {
			counter = file_length / blockSize;
		} else {
			counter = (file_length / blockSize) + 1;
		}
		
		if(dtForFat.containsKey(file_id)) { //it checks whether there is a file with the given file id 
			fat_creation_rejects++;
			System.out.println("There is already a file with the file id: "+file_id);
			return;
		}
		
		if (dtForFat.size() == 0) { //if the file is the first file
			dtForFat.put(file_id, new Pair(0, counter));
			for (int i = 0; i < counter; i++) {
				filling(i, 1); //filling the directory block by block. The code is not doing multiple filling because this is not contiguous allocation 
				fileAllocationTable.put(i, i + 1);
				if (i == counter - 1) {
					fileAllocationTable.replace(i, -1); //new pointer, -1 means the end of the file 
				}
			}
			return;
		}

		if (enoughSpaceInDirectory(file_length)) { //if there is enough space in directory for creation of the given file
			for (int i = 0; i < Directory.length; i++) {
				if (index == counter)
					return;
				if (!fileAllocationTable.containsKey(i)) {
					if (index == 0) {
						dtForFat.put(file_id, new Pair(i, counter));
						filling(i, 1);
						fileAllocationTable.put(i, -1);
						prev = i;
						index++;
					} else {
						filling(i, 1);
						fileAllocationTable.put(i, -1);
						fileAllocationTable.replace(prev, i);
						prev = i;
						index++;
					}
				}
			}
		} else {
			System.out.println("There is not enough space for this file with length: " + file_length);
			fat_creation_rejects++;
		}

	}

	// it checks whether the number of spaces in the directory is enough
	private static boolean enoughSpaceInDirectory(int file_length) {
		int count = 0;
		for (int i = 0; i < Directory.length - 1; i++) {
			if (Directory[i].getBlockContent() == -1) {
				count++;
			}
		}
		if (count >= file_length) {
			return true;
		}
		return false;
	}

	private static int fat_access(int file_id, int byte_offset) {

		int loc = 0;
		int x = 0;

		if (!dtForFat.containsKey(file_id)) {
			System.out.println("Access request is unsuccessful since there is not any file with file id: " + file_id);
			return loc;
		} else {

			loc = dtForFat.get(file_id).getFirst();

			x = byte_offset / blockSize;

			for (int i = 0; i < x; i++) {
				loc = fileAllocationTable.get(loc);
			}
		}
		System.out.println("the location of the byte having the offset " + byte_offset + " in the directory: " + loc);
		return loc;
	}

	private static void fat_extend(int file_id, int extension) {

		if (dtForFat.containsKey(file_id)) { //it checks whether there is such a file 
			int filelength = dtForFat.get(file_id).getSecond();
			int first = dtForFat.get(file_id).getFirst();
			int prev = 0;
			int index = 0;
			int startingIndex=dtForFat.get(file_id).getFirst();

			if (enoughSpaceInDirectory(extension)) { //it checks whether there is enough space 

				for (int i = 0; i < filelength; i++) {
					prev = first;
					first = fileAllocationTable.get(first);
				}
				for (int j = 0; j < Directory.length; j++) {
					if (index == extension) {
						dtForFat.replace(file_id, new Pair(startingIndex, filelength+extension)); //the code changes the length of the file 
						return;
					}
					if (!fileAllocationTable.containsKey(j)) {
						filling(j, 1);
						fileAllocationTable.put(j, -1);
						fileAllocationTable.replace(prev, j);
						prev = j;
						index++;
					}
				}

			} else {
				System.out.println("There is not enough space for the extension");
				fat_extension_rejects++;
			}
		}

		System.out.println("There is no such file with the file id: " + file_id);
		fat_extension_rejects++;
	}

	private static void fat_shrink(int file_id, int shrink) {

		if (dtForFat.containsKey(file_id)) { //it checks whether there is such a file 

			int filelength = dtForFat.get(file_id).getSecond();
			int first = dtForFat.get(file_id).getFirst();
			int prev = dtForFat.get(file_id).getFirst();
			int temp = 0;
			int startingIndex=dtForFat.get(file_id).getFirst();

			if (filelength >= shrink) { //this means that we are able to shrink the file 

				for (int i = 0; i < filelength - shrink; i++) {
					first = fileAllocationTable.get(first);
				}

				for (int i = 0; i < filelength - shrink - 1; i++) {
					prev = fileAllocationTable.get(prev);
				}

				for (int j = 0; j < shrink; j++) {
					temp = fileAllocationTable.get(first);
					Directory[first].setBlockContent(-1);
					fileAllocationTable.replace(prev, -1);
					fileAllocationTable.remove(first);
					first = temp;
				}
				dtForFat.replace(file_id, new Pair(startingIndex, filelength-shrink)); //the code changes the length of the file 

			} else {
				System.out.println("File size could not be decreased!");
			}
		} else {
			System.out.println("There is no such file with the file id: " + file_id);
		}

	}

	public static void main(String[] args) {

		
		// initializing the directory
		for (int i = 0; i < Directory.length; i++) {
			Directory[i] = new Block(blockSize, -1);
		}
		
		long startTime=System.nanoTime();
		try {
			File inputFile = new File("input_2048_600_5_5_0.txt");
			Scanner fileScanner = new Scanner(inputFile);
			int ID = 0;
			while (fileScanner.hasNext()) {
				String[] array = fileScanner.nextLine().split(":");
				if (array[0].equals("c")) {
					int len = Integer.parseInt(array[1]);
					dt_create_file(ID,len); //open it when you want to test directory table methods
					//fat_create_file(ID, len); // open it when you want to test file allocation table methods
					ID++;
				}
				if (array[0].equals("a")) {
					int file_id = Integer.parseInt(array[1]);
					int offset = Integer.parseInt(array[2]);
					dt_access(file_id,offset); //open it when you want to test directory table methods
					//fat_access(file_id, offset); // open it when you want to test file allocation table methods
				}
				if (array[0].equals("sh")) {
					int file_id = Integer.parseInt(array[1]);
					int shrinking = Integer.parseInt(array[2]);
					dt_shrink(file_id,shrinking); //open it when you want to test directory table methods
					//fat_shrink(file_id, shrinking); // open it when you want to test file allocation table methods
				}
				if (array[0].equals("e")) {
					int file_id = Integer.parseInt(array[1]);
					int extension = Integer.parseInt(array[2]);
					dt_extend(file_id,extension); //open it when you want to test directory table methods
					//fat_extend(file_id, extension); // open it when you want to test file allocation table methods
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		long endTime=System.nanoTime();
		long duration = (endTime-startTime)/1000000; //I divide to get milliseconds.
		
		System.out.println("The duration of the operation is: "+duration);

		System.out.println("The number of files that their creation is rejected in Directory Table implementation: "+dt_creation_rejects);
		System.out.println("The number of files that their extension is rejected in Directory Table implementation: "+dt_extension_rejects);
		
	//	System.out.println("The number of files that their creation is rejected in File Allocation Table implementation: "+fat_creation_rejects);
	//	System.out.println("The number of files that their extension is rejected in File Allocation Table implementation: "+fat_extension_rejects);
	}

}
