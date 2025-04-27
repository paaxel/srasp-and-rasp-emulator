package it.palex.rasp.parser.operations;

import it.palex.rasp.parser.operations.generic.Parameter;

public class Identifier implements Parameter {

	private String id;
	
	public Identifier(Integer id) {
		if(id<0) {
			throw new IllegalArgumentException("identifiers numbers cannot have sign");
		}
		this.id = id+"";
	}
	
	public Identifier(String id) {
		this.id = id;
	}
	
	public String getIdentifier() {
		return this.id;
	}

	@Override
	public String toString() {
		return "Identifier [id=" + id + "]";
	}
	
}
