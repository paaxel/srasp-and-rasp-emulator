package it.palex.rasp.parser.operations.generic;

import it.palex.rasp.parser.operations.ReferenceArgument;

public abstract class ReferenceMachineInstruction implements MachineInstruction {

	private ReferenceArgument argument;
	private int line;
	
	public ReferenceMachineInstruction(ReferenceArgument argument, int line) {
		this.argument = argument;
		this.line = line;
	}

	public ReferenceArgument getArgument() {
		return argument;
	}
	
	public int getLine() {
		return this.line;
	}

	@Override
	public String toString() {
		return "ReferenceMachineInstruction [argument=" + argument + ", line=" + line + "]";
	}
	
	
}
