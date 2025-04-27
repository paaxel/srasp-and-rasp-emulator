package it.palex.rasp.parser.operations;

import it.palex.rasp.parser.operations.generic.Parameter;

public class NumberValue implements Parameter {

	private Integer value;
	
	public NumberValue(Integer value) {
		this.value = value;
	}

	public Integer getIntValue() {
		return value;
	}

	@Override
	public String toString() {
		return "NumberValue [value=" + value + "]";
	}
	
}
