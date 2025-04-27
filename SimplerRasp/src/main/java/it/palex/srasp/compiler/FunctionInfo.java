package it.palex.srasp.compiler;

import java.util.Arrays;

public class FunctionInfo {

	private NodeType returnType;
	private int parameterNumber;
	private NodeType[] parametersTypes;
	private String label;
	
	public FunctionInfo(NodeType returnType, int parameterNumber, NodeType[] parametersTypes, String label) {
		super();
		this.returnType = returnType;
		this.parameterNumber = parameterNumber;
		this.parametersTypes = parametersTypes;
		this.label = label;
	}

	public NodeType getReturnType() {
		return returnType;
	}

	public int getParameterNumber() {
		return parameterNumber;
	}

	public NodeType[] getParametersTypes() {
		return parametersTypes;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return "FunctionInfo [returnType=" + returnType + ", parameterNumber=" + parameterNumber + ", parametersTypes="
				+ Arrays.toString(parametersTypes) + ", label=" + label + "]";
	}
	
}
