package comp304;

import java.util.ArrayList;

public class DTNode {
	
    private int fileID;
    private int startingIndex;
    private int length;
    
 //   private ArrayList<Integer> nodes=new ArrayList<Integer>();
    public DTNode(int fileID, int startingIndex, int length) {
        super();
        this.fileID=fileID;
        this.startingIndex=startingIndex;
        this.length=length;
//        nodes.add(0,fileID);
//        nodes.add(1, startingIndex);
//        nodes.add(2, length);
    }
    public int getFileID() {
        return fileID;
    }
    public void setFileID(int fileID) {
        this.fileID = fileID;
    }
    public int getFirst() {
        return startingIndex;
    }
    public void setFirst(int startingIndex) {
        this.startingIndex = startingIndex;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
}
