package it.palex.rasp.parser.operations.instructions;

import it.palex.rasp.executors.MachineInstructionVisitor;
import it.palex.rasp.parser.operations.DirectArgument;
import it.palex.rasp.parser.operations.generic.DirectMachineInstruction;

public class LoadDirectInstruction extends DirectMachineInstruction {

	public LoadDirectInstruction(DirectArgument argument, int line) {
		super(argument, line);
	}
	
	@Override
	public void accept(MachineInstructionVisitor visitor) {
		visitor.visit(this);
	}
	
}
