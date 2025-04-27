package it.palex.rasp.parser.operations;

import it.palex.rasp.parser.operations.generic.Argument;

public class StandardArgument extends Argument {
	
	private Identifier id;
	
	public StandardArgument(Identifier id) {
		this.id = id;
	}

	public Identifier getIdentifier() {
		return this.id;
	}

	@Override
	public String toString() {
		return "StandardArgument [id=" + id + "]";
	}
	
}