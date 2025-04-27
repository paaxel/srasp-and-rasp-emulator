package it.palex.srasp.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class LayeredMemory {

	private LinkedList<Block> layeredBlocks;
	private Map<String, FunctionInfo> declaredFunctions;
	
	public LayeredMemory() {
		this.layeredBlocks = new LinkedList<>();
		this.declaredFunctions = new HashMap<>();
	}
	
	public void addNewTempNode(CompileNode node) {
		String identifier = UniqueIdentifierManager.getInstance().getNewUniqueId();
		node.setUniqueNodeId(identifier);
		
		this.layeredBlocks.getLast().addNode(identifier, node);
	}
	
	public void addNewStackNode(String identifier, CompileNode valNode) {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Cannot add. Empty block stack");
		}

		if(this.hasAlreadyDefinedIdentifier(identifier)) {
			throw new IllegalArgumentException("The identifier --> "+identifier+", was already defined");
		}
		
		Block lastBlock = this.layeredBlocks.getLast();
		int declaredVariableInBlock = lastBlock.getVariableDeclared();
		valNode.setRelativeStackIndexPosition(declaredVariableInBlock);
		lastBlock.increaseVariableDeclared();
		valNode.setUniqueNodeId(UniqueIdentifierManager.getInstance().getNewUniqueId());
		valNode.setStackVariable(true);
		lastBlock.addNode(identifier, valNode);
	}
		
	public void increaseNumberOfVariableDeclared() {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Cannot add. Empty block stack");
		}
		Block lastBlock = this.layeredBlocks.getLast();
		lastBlock.increaseVariableDeclared();
	}
	
	
	public void decreseNumberOfVariableDeclared() {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Cannot add. Empty block stack");
		}
		
		Block lastBlock = this.layeredBlocks.getLast();
		lastBlock.decreaseVariableDeclared();
		
		if(lastBlock.getVariableDeclared()<0) {
			throw new RuntimeException("Block declared variable is negative");
		}
	}
	
	
	/**
	 * 
	 * @return the total number of variable defined in a function. (Not Before a blocking block)<br>
	 * This can be useful when the function force a return 
	 */
	public int getTotalVariableDefinedInFunction() {
		int totalVariableDefined = 0;
		
		ListIterator<Block> it = this.layeredBlocks.listIterator(this.layeredBlocks.size());
				
		while(it.hasPrevious()) {
			Block currentBlock = it.previous();
			
			totalVariableDefined = totalVariableDefined + currentBlock.getVariableDeclared();
		}
		
		return totalVariableDefined;
	}
	
	/**
	 * 
	 * @param uniqueNodeId
	 * @return the number of variables defined in block
	 * This function is useful if you want to calculate the relative position of a variable 
	 * considering all variable defined in the stack for the current visible block
	 */
	public int getTotalVariableDefinedAfterNode(String uniqueNodeId) {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Empty block stack");
		}
		
		int totalVariableDefined = 0;
		
		ListIterator<Block> it = this.layeredBlocks.listIterator(this.layeredBlocks.size());
				
		while(it.hasPrevious()) {
			Block currentBlock = it.previous();
			
			totalVariableDefined = totalVariableDefined + currentBlock.getVariableDeclared();
					
			if(currentBlock.isDefinedIdentifier(uniqueNodeId)) {
				return totalVariableDefined;
			}
		}
		
		throw new RuntimeException("Not found node at getTotalVariableDefinedAfterNode");
	}
	
	
	public Block getCurrentBlock() {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Cannot pop. Empty block stack");
		}
		Block lastBlock = this.layeredBlocks.getLast();
		
		return lastBlock;
	}
	
	public String getNewLabelIdentifier() {
		String identifier = UniqueIdentifierManager.getInstance().getNewUniqueId();
		
		return identifier;
	}
	
	/**
	 * Remove the block from the top of the stack.<br>
	 * This method should be called when a block statement is closed<br>
	 * The method will clear the identifier used
	 * @return 
	 * @throws NoSuchElementException if the stack is empty
	 */
	public Block popBlock() {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Cannot pop. Empty block stack");
		}
		Block removedBlock = this.layeredBlocks.removeLast();
		//free the identifiers that can be reused
		UniqueIdentifierManager idManager = UniqueIdentifierManager.getInstance();
		Set<String> set = removedBlock.getAllIdentifiersInBlock();

		return removedBlock;
	}
	
	/**
	 * Add new non blocking block at the top of the stack.<br>
	 * This method should be called when a block statement is opened
	 */
	public void pushNewBlock() {
		this.layeredBlocks.addLast(new Block());
	}
	
	public int blockNum() {
		return this.layeredBlocks.size();
	}
	
	/**
	 * 
	 * @param identifier
	 * @return true if the identifier is already defined in the block stack false otherwise
	 */
	public boolean hasAlreadyDefinedIdentifier(String identifier) {
		if(identifier==null) {
			throw new NullPointerException("Identifier is null at checkExistanceOfNode");
		}
		
		// scan the block in the nested order
		ListIterator<Block> it = this.layeredBlocks.listIterator(this.layeredBlocks.size());
		
		while(it.hasPrevious()) {
			Block currentBlock = it.previous();
			
			if(currentBlock.isDefinedIdentifier(identifier)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * The block return type is the label to jump when a return is called
	 * @param blockLabel
	 */
	public void setReturnBlockType(NodeType voidType) {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Cannot pop. Empty block stack");
		}
		
		this.layeredBlocks.getLast().setReturnType(voidType);
	}
	
	/**
	 * The block label rapresent the label to jump when a return is called
	 * @param blockLabel
	 */
	public void setReturnBlockLabel(String blockLabel) {
		if(this.layeredBlocks.isEmpty()) {
			throw new NoSuchElementException("Cannot pop. Empty block stack");
		}
		
		this.layeredBlocks.getLast().setReturnBlockLabel(blockLabel);		
	}
	
	public NodeType getCurrentBlockType() {
		// scan the block in the nested order
		ListIterator<Block> it = this.layeredBlocks.listIterator(this.layeredBlocks.size());
		
		
		while(it.hasPrevious()) {
			Block currentBlock = it.previous();
			
			if(currentBlock.hasReturnBlockType()) {
				return currentBlock.getReturnType();
			}
			
		}
		
		return null;
	}
	
	public String getCurrentBlockLabel() {
		// scan the block in the nested order
		ListIterator<Block> it = this.layeredBlocks.listIterator(this.layeredBlocks.size());
				
		while(it.hasPrevious()) {
			Block currentBlock = it.previous();
			
			if(currentBlock.hasReturnBlockLabel()) {
				return currentBlock.getReturnBlockLabel();
			}
			
		}
		
		return null;
	}

	/**
	 * 
	 * @param identifier
	 * @return the CompileNode identified by <b>identifier</b>,
	 *  null will be returned if no CompileNode identified by <b>identifier</b> is found in the block
	 */
	public CompileNode getNodeById(String identifier) {
		if(identifier==null) {
			throw new NullPointerException("Identifier is null at checkExistanceOfNode");
		}
		// scan the block in the nested order
		ListIterator<Block> it = this.layeredBlocks.listIterator(this.layeredBlocks.size());
				
		while(it.hasPrevious()) {
			Block currentBlock = it.previous();
			
			if(currentBlock.isDefinedIdentifier(identifier)) {
				return currentBlock.getNode(identifier);
			}
		}
		
		return null;
	}

	public void addNewDeclaredFunction(String functionName, FunctionInfo functionInfo) {
		this.declaredFunctions.put(functionName, functionInfo);	
	}
	
	public FunctionInfo getDeclaredFunction(String functionName) {
		return this.declaredFunctions.get(functionName);
	}
	
	
	
}
