package it.palex.rasp.parser.operations;

import it.palex.rasp.parser.operations.generic.Argument;

public class ReferenceArgument extends Argument {
	
	private Identifier id;
	
	public ReferenceArgument(Identifier id) {
		this.id = id;
	}

	public Identifier getIdentifier() {
		return this.id;
	}

	@Override
	public String toString() {
		return "ReferenceArgument [id=" + id + "]";
	}
	
}