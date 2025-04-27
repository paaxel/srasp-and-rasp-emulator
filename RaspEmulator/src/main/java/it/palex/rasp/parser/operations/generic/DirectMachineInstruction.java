package it.palex.rasp.parser.operations.generic;

import it.palex.rasp.parser.operations.DirectArgument;

public abstract class DirectMachineInstruction implements MachineInstruction {

	private DirectArgument argument;
	private int line;

	public DirectMachineInstruction(DirectArgument argument, int line) {
		this.argument = argument;
		this.line = line;
	}

	public DirectArgument getArgument() {
		return argument;
	}
	
	public int getLine(){
		return this.line;
	}

	@Override
	public String toString() {
		return "DirectMachineInstruction [argument=" + argument + ", line=" + line + "]";
	}
	
}
