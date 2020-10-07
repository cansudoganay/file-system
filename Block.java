package comp304;

public class Block {
	
	int blockSize; //fixed
	int blockContent; //random numbers
	
	public Block(int blockSize, int blockContent) {
		super();
		this.blockSize=blockSize;
		this.blockContent=blockContent;
	}

	public int getBlockContent() {
		return blockContent;
	}

	public void setBlockContent(int blockContent) {
		this.blockContent = blockContent;
	}

}
