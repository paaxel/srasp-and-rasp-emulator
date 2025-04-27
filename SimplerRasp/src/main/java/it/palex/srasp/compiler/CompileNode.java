package it.palex.srasp.compiler;

public class CompileNode {

	private NodeType type;
	private boolean stackVariable;
	private int relativeStackIndexPosition;
	private String uniqueNodeId;

	public CompileNode(NodeType type) {
		this.type = type;
		this.stackVariable = false;
		this.relativeStackIndexPosition = -1;
	}

	public CompileNode(NodeType type, boolean stackVariable, int relativeStackIndexPosition) {
		if(relativeStackIndexPosition<0) {
			throw new IllegalArgumentException("Invalid relativeStackIndexPosition");
		}
		this.type = type;
		this.stackVariable = stackVariable;
		this.relativeStackIndexPosition = relativeStackIndexPosition;
	}

	public NodeType getType() {
		return this.type;
	}

	public String getUniqueNodeId() {
		return uniqueNodeId;
	}

	public void setUniqueNodeId(String uniqueNodeId) {
		this.uniqueNodeId = uniqueNodeId;
	}

	public boolean isStackVariable() {
		return stackVariable;
	}
	
	public void setStackVariable(boolean stackVariable) {
		this.stackVariable = stackVariable;
	}

	public int getRelativeStackIndexPosition() {
		if(this.relativeStackIndexPosition<0) {
			throw new NullPointerException("Stack index position never initialized");
		}
		return relativeStackIndexPosition;
	}

	public void setRelativeStackIndexPosition(int relativeStackIndexPosition) {
		this.relativeStackIndexPosition = relativeStackIndexPosition;
	}

	@Override
	public String toString() {
		return "CompileNode [type=" + type + ", stackVariable=" + stackVariable + ", relativeStackIndexPosition="
				+ relativeStackIndexPosition + ", uniqueNodeId=" + uniqueNodeId + "]";
	}

}
