package it.palex.rasp.parser.operations.instructions;

import it.palex.rasp.executors.MachineInstructionVisitor;
import it.palex.rasp.parser.operations.StandardArgument;
import it.palex.rasp.parser.operations.generic.StandardMachineInstruction;

public class JGEZInstruction extends StandardMachineInstruction {
	
	public JGEZInstruction(StandardArgument argument, int line) {
		super(argument, line);
	}
	
	@Override
	public void accept(MachineInstructionVisitor visitor) {
		visitor.visit(this);
	}


}
