package it.palex.rasp.parser.operations.instructions;

import it.palex.rasp.executors.MachineInstructionVisitor;
import it.palex.rasp.parser.operations.StandardArgument;
import it.palex.rasp.parser.operations.generic.StandardMachineInstruction;

public class DivStandardInstruction extends StandardMachineInstruction {

	public DivStandardInstruction(StandardArgument argument, int line) {
		super(argument, line);
	}
	
	@Override
	public void accept(MachineInstructionVisitor visitor) {
		visitor.visit(this);
	}

}
