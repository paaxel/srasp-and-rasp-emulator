package it.palex.rasp.executors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.palex.rasp.parser.operations.InstructionBlock;
import it.palex.rasp.parser.operations.generic.MachineInstruction;
import it.palex.rasp.parser.operations.instructions.AddDirectInstruction;
import it.palex.rasp.parser.operations.instructions.AddReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.AddStandardInstruction;
import it.palex.rasp.parser.operations.instructions.DivDirectInstruction;
import it.palex.rasp.parser.operations.instructions.DivReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.DivStandardInstruction;
import it.palex.rasp.parser.operations.instructions.HaltInstruction;
import it.palex.rasp.parser.operations.instructions.JGEZInstruction;
import it.palex.rasp.parser.operations.instructions.JGZInstruction;
import it.palex.rasp.parser.operations.instructions.JLEZInstruction;
import it.palex.rasp.parser.operations.instructions.JLZInstruction;
import it.palex.rasp.parser.operations.instructions.JNZInstruction;
import it.palex.rasp.parser.operations.instructions.JUMPInstruction;
import it.palex.rasp.parser.operations.instructions.JUMPReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.JZInstruction;
import it.palex.rasp.parser.operations.instructions.LoadDirectInstruction;
import it.palex.rasp.parser.operations.instructions.LoadReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.LoadStandardInstruction;
import it.palex.rasp.parser.operations.instructions.MulDirectInstruction;
import it.palex.rasp.parser.operations.instructions.MulReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.MulStandardInstruction;
import it.palex.rasp.parser.operations.instructions.ReadReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.ReadStandardInstruction;
import it.palex.rasp.parser.operations.instructions.StoreReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.StoreStandardInstruction;
import it.palex.rasp.parser.operations.instructions.SubDirectInstruction;
import it.palex.rasp.parser.operations.instructions.SubReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.SubStandardInstruction;
import it.palex.rasp.parser.operations.instructions.WriteDirectInstruction;
import it.palex.rasp.parser.operations.instructions.WriteReferenceInstruction;
import it.palex.rasp.parser.operations.instructions.WriteStandardInstruction;

public class RaspInterpreterPreprocessor implements MachineInstructionVisitor {
	
	private static final Logger LOGGER = LogManager.getLogger(RaspInterpreterPreprocessor.class);
	
	private HashMap<String, Integer> machineLabelledInstruction;
	private boolean haltNotFound = false;
	private List<MachineInstruction> instructions;
	private ArrayList<MachineInstruction> instructionsArray;
	private int instructionNumRegister;
	
	public RaspInterpreterPreprocessor(List<MachineInstruction> instructions) {
		this.machineLabelledInstruction = new HashMap<>();
		this.haltNotFound = false;
		this.instructions = instructions;
		this.instructionsArray = new ArrayList<MachineInstruction>();
		this.instructionNumRegister = 0;
	}
	
	void executePreprocessing() {
		LOGGER.info("Preprocessing started...");
		
		for (MachineInstruction machineInstruction : instructions) {
			machineInstruction.accept(this);
		}
		
		LOGGER.info("Preprocessing finished...");
	}
	
	void reset() {
		this.machineLabelledInstruction.clear();
		this.haltNotFound = false;
		this.instructionNumRegister = 0;
		this.instructionsArray = new ArrayList<MachineInstruction>();
	}
	
	HashMap<String, Integer> getLabelledInstructions(){
		return this.machineLabelledInstruction;
	}
	
	MachineInstruction[] getInstructionsArray(){
		return this.instructionsArray.toArray(new MachineInstruction[0]);
	}
	
	boolean haltInstructionFound() {
		return this.haltNotFound;
	}
	
	private void preprocessInstruction(MachineInstruction instruction) {
		this.instructionsArray.add(instruction);
		this.instructionNumRegister++;
	}
	
	@Override
	public void visit(DivStandardInstruction divStandardInstruction) {
		preprocessInstruction(divStandardInstruction);
	}

	@Override
	public void visit(AddDirectInstruction addDirectInstruction) {
		preprocessInstruction(addDirectInstruction);
	}

	@Override
	public void visit(DivReferenceInstruction addDirectInstruction) {
		preprocessInstruction(addDirectInstruction);
	}

	@Override
	public void visit(DivDirectInstruction addDirectInstruction) {
		preprocessInstruction(addDirectInstruction);
	}

	@Override
	public void visit(AddStandardInstruction addStandardInstruction) {
		preprocessInstruction(addStandardInstruction);
	}

	@Override
	public void visit(AddReferenceInstruction addReferenceInstruction) {
		preprocessInstruction(addReferenceInstruction);
	}

	@Override
	public void visit(HaltInstruction haltInstruction) {
		this.haltNotFound = true;
		preprocessInstruction(haltInstruction);
	}

	@Override
	public void visit(JGEZInstruction jgezInstruction) {
		preprocessInstruction(jgezInstruction);
	}

	@Override
	public void visit(JGZInstruction jgzInstruction) {
		preprocessInstruction(jgzInstruction);
	}

	@Override
	public void visit(JLEZInstruction jlezInstruction) {
		preprocessInstruction(jlezInstruction);
	}

	@Override
	public void visit(JLZInstruction jlzInstruction) {
		preprocessInstruction(jlzInstruction);
	}

	@Override
	public void visit(JNZInstruction jnzInstruction) {
		preprocessInstruction(jnzInstruction);
	}

	@Override
	public void visit(JUMPInstruction jumpInstruction) {
		preprocessInstruction(jumpInstruction);
	}
	
	@Override
	public void visit(JUMPReferenceInstruction jumpReferenceInstruction) {
		preprocessInstruction(jumpReferenceInstruction);
	}

	@Override
	public void visit(JZInstruction jzInstruction) {
		preprocessInstruction(jzInstruction);
	}

	@Override
	public void visit(LoadDirectInstruction loadDirectInstruction) {
		preprocessInstruction(loadDirectInstruction);
	}

	@Override
	public void visit(LoadReferenceInstruction loadReferenceInstruction) {
		preprocessInstruction(loadReferenceInstruction);
	}

	@Override
	public void visit(LoadStandardInstruction loadStandardInstruction) {
		preprocessInstruction(loadStandardInstruction);
	}

	@Override
	public void visit(MulDirectInstruction mulDirectInstruction) {
		preprocessInstruction(mulDirectInstruction);
	}

	@Override
	public void visit(MulReferenceInstruction mulReferenceInstruction) {
		preprocessInstruction(mulReferenceInstruction);
	}

	@Override
	public void visit(MulStandardInstruction mulStandardInstruction) {
		preprocessInstruction(mulStandardInstruction);
	}

	@Override
	public void visit(ReadReferenceInstruction readReferenceInstruction) {
		preprocessInstruction(readReferenceInstruction);
	}

	@Override
	public void visit(ReadStandardInstruction readStandardInstruction) {
		preprocessInstruction(readStandardInstruction);
	}

	@Override
	public void visit(StoreReferenceInstruction storeReferenceInstruction) {
		preprocessInstruction(storeReferenceInstruction);
	}

	@Override
	public void visit(StoreStandardInstruction storeStandardInstruction) {
		preprocessInstruction(storeStandardInstruction);
	}

	@Override
	public void visit(SubDirectInstruction subDirectInstruction) {
		preprocessInstruction(subDirectInstruction);
	}

	@Override
	public void visit(SubReferenceInstruction subReferenceInstruction) {
		preprocessInstruction(subReferenceInstruction);
	}

	@Override
	public void visit(SubStandardInstruction subStandardInstruction) {
		preprocessInstruction(subStandardInstruction);
	}

	@Override
	public void visit(WriteDirectInstruction writeDirectInstruction) {
		preprocessInstruction(writeDirectInstruction);
	}

	@Override
	public void visit(WriteReferenceInstruction writeReferenceInstruction) {
		preprocessInstruction(writeReferenceInstruction);
	}

	@Override
	public void visit(WriteStandardInstruction writeStandardInstruction) {
		preprocessInstruction(writeStandardInstruction);
	}

	@Override
	public void visit(InstructionBlock instructionBlock) {
		String blockLabel = instructionBlock.getLabel().getIdentifier();
		
		this.machineLabelledInstruction.put(blockLabel, this.instructionNumRegister);
		
		preprocessInstruction(instructionBlock);
		
		for (MachineInstruction instruction : instructionBlock.getInstructions()) {
			instruction.accept(this);
		}
		
	}

	

}
