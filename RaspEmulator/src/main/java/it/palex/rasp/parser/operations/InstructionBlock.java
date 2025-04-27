package it.palex.rasp.parser.operations;

import java.util.List;

import it.palex.rasp.executors.MachineInstructionVisitor;
import it.palex.rasp.parser.operations.generic.CompositeMachineInstruction;
import it.palex.rasp.parser.operations.generic.MachineInstruction;

public class InstructionBlock extends CompositeMachineInstruction {

	private Identifier label;
	
	public InstructionBlock(List<MachineInstruction> instructions, Identifier label, int line) {
		super(instructions, line);
		this.label = label;
	}

	public Identifier getLabel() {
		return this.label;
	}

	@Override
	public void accept(MachineInstructionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "InstructionBlock [label=" + label +", "
				+ "instructions=" + this.getInstructions() + ", line=" + this.getLine() + "]";
	}
	
}
