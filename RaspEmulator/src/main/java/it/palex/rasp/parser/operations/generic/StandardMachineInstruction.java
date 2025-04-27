package it.palex.rasp.parser.operations.generic;

import it.palex.rasp.parser.operations.StandardArgument;

public abstract class StandardMachineInstruction implements MachineInstruction {
	
	private StandardArgument argument;
	private int line;

	public StandardMachineInstruction(StandardArgument argument, int line) {
		this.argument = argument;
		this.line = line;
	}

	public StandardArgument getArgument() {
		return argument;
	}
	
	public int getLine() {
		return this.line;
	}

	@Override
	public String toString() {
		return "StandardMachineInstruction [argument=" + argument + ", line=" + line + "]";
	}
	
}
