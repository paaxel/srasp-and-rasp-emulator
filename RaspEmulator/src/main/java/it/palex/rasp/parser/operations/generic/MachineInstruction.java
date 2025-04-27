package it.palex.rasp.parser.operations.generic;

import it.palex.rasp.executors.MachineInstructionVisitor;

public interface MachineInstruction {

	public int getLine();
	
	/**
	 * Allow the visitor to inspect the object
	 * @param visitor
	 */
	public void accept(MachineInstructionVisitor visitor);
	
	
}
