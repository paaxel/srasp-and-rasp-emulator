package it.palex.rasp.parser.operations.generic;

import java.util.List;

public abstract class CompositeMachineInstruction implements MachineInstruction {

	private List<MachineInstruction> instructions;
	private int line;
	
	public CompositeMachineInstruction(List<MachineInstruction> instructions, int line) {
		this.instructions = instructions;
		this.line = line;
	}

	public List<MachineInstruction> getInstructions() {
		return instructions;
	}
	
	public int getLine(){
		return this.line;
	}

	@Override
	public String toString() {
		return "CompositeMachineInstruction ["
				+ "instructions=" + instructions + ", line=" + line + "]";
	}
}
