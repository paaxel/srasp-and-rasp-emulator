package it.palex.rasp.parser.operations.instructions;

import it.palex.rasp.executors.MachineInstructionVisitor;
import it.palex.rasp.parser.operations.generic.MachineInstruction;

public class HaltInstruction implements MachineInstruction {
	
	private int line;
	
	public HaltInstruction(int line) {
		this.line = line;
	}
	
	@Override
	public void accept(MachineInstructionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int getLine() {
		return this.line;
	}

	@Override
	public String toString() {
		return "HaltInstruction [line=" + line + "]";
	}

}
