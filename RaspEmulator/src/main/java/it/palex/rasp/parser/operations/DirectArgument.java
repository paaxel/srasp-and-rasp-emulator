package it.palex.rasp.parser.operations;

import it.palex.rasp.parser.operations.generic.Argument;

public class DirectArgument extends Argument {
	
	private NumberValue value;
	
	public DirectArgument(NumberValue value) {
		this.value = value;
	}

	public NumberValue getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "DirectArgument [value=" + value + "]";
	}
		
}
