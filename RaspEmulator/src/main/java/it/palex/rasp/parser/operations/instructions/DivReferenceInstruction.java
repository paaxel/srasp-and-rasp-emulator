package it.palex.rasp.parser.operations.instructions;

import it.palex.rasp.executors.MachineInstructionVisitor;
import it.palex.rasp.parser.operations.ReferenceArgument;
import it.palex.rasp.parser.operations.generic.ReferenceMachineInstruction;

public class DivReferenceInstruction extends ReferenceMachineInstruction{

	public DivReferenceInstruction(ReferenceArgument argument, int line) {
		super(argument, line);
	}
	
	@Override
	public void accept(MachineInstructionVisitor visitor) {
		visitor.visit(this);
	}

}
