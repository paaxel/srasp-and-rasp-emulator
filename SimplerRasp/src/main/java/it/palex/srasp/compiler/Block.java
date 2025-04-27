package it.palex.srasp.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Block {
	
	
	private int variableDeclared = 0;
	
	private String returnBlockLabel;
	private NodeType returnType;
	
	private Map<String, CompileNode> block;
	
	public Block() {
		this.block = new HashMap<>();
	}
	
	public void addNode(String identifier, CompileNode node) {
		if(identifier==null || node==null) {
			throw new NullPointerException();
		}
		this.block.put(identifier, node);
	}
	
	/**
	 * 
	 * @param identifier
	 * @return true if the identifier was already defined in the block
	 */
	public boolean isDefinedIdentifier(String identifier) {
		return this.block.containsKey(identifier);
	}
	
	
	/**
	 * 
	 * @param identifier
	 * @return the CompileNode identified by <b>identifier<b>,
	 *  null will be returned if no CompileNode identified by <b>identifier<b> is found in the block
	 */
	public CompileNode getNode(String identifier) {
		return this.block.get(identifier);
	}
	
	
	public int getVariableDeclared() {
		return variableDeclared;
	}

	public void increaseVariableDeclared() {
		this.variableDeclared++;
	}
	
	public void decreaseVariableDeclared() {
		this.variableDeclared--;
	}

	public Set<String> getAllIdentifiersInBlock(){
		return this.block.keySet();
	}

	public boolean hasReturnBlockLabel() {
		return this.returnBlockLabel != null;
	}
	public String getReturnBlockLabel() {
		return returnBlockLabel;
	}

	public void setReturnBlockLabel(String returnBlockLabel) {
		this.returnBlockLabel = returnBlockLabel;
	}

	public NodeType getReturnType() {
		return returnType;
	}

	public void setReturnType(NodeType returnType) {
		this.returnType = returnType;
	}
	
	public boolean hasReturnBlockType() {
		return this.returnType != null;
	}

	@Override
	public String toString() {
		return "Block [returnBlockLabel=" + returnBlockLabel + ", returnType=" + returnType + ", block=" + block
				+ "]";
	}

	
	
}
