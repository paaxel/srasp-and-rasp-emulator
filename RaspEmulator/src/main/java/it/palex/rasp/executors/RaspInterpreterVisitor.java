package it.palex.rasp.executors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.palex.rasp.executors.exception.HaltInstructionNotFoundException;
import it.palex.rasp.executors.exception.LabelNotFoundException;
import it.palex.rasp.executors.exception.VariableNotInitializedException;
import it.palex.rasp.executors.exception.ZeroDivisionException;
import it.palex.rasp.inout.InputReader;
import it.palex.rasp.inout.OutputObserver;
import it.palex.rasp.parser.operations.DirectArgument;
import it.palex.rasp.parser.operations.Identifier;
import it.palex.rasp.parser.operations.InstructionBlock;
import it.palex.rasp.parser.operations.ReferenceArgument;
import it.palex.rasp.parser.operations.StandardArgument;
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

public class RaspInterpreterVisitor implements MachineInstructionVisitor {

	private static final Logger LOGGER = LogManager.getLogger(RaspInterpreterVisitor.class);
	
	private HashMap<String, Integer> environment;
	private HashMap<String, Integer> machineLabelledInstruction;
	private MachineInstruction[] instructionsArray;
	
	
	private List<MachineInstruction> instructions;
	private OutputObserver output;
	private InputReader reader;
	
	private int inputNum = 0;
	private Integer accumulator;
	private Integer instructionPointer;
	private boolean systemHalted = false;
	
	public RaspInterpreterVisitor(List<MachineInstruction> instructions, OutputObserver output,
									InputReader reader) {
		this.environment = new HashMap<>();
		this.machineLabelledInstruction = new HashMap<>();
		this.accumulator = 0;
		this.instructions = instructions;
		this.inputNum = 0;
		this.output = output;
		this.reader = reader;
		this.instructionPointer = 0;
		this.instructionsArray = new MachineInstruction[0];
		this.systemHalted = false;
	}
	
	public void startInterpretation() throws InterruptedException {
		RaspInterpreterPreprocessor preprocessor = new RaspInterpreterPreprocessor(
				this.instructions);
		preprocessor.executePreprocessing();
		
		this.machineLabelledInstruction = preprocessor.getLabelledInstructions();
		this.instructionsArray = preprocessor.getInstructionsArray();
		boolean haltInstructionFound = preprocessor.haltInstructionFound();
		
		if(!haltInstructionFound) {
			throw new HaltInstructionNotFoundException("No HALT instruction found");
		}
		
		LOGGER.info("program started...");
		
		int totalInstructionToProcess = instructionsArray.length;
		
		while(this.instructionPointer<totalInstructionToProcess && !this.systemHalted) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Program interrupted");
			}

			instructionsArray[this.instructionPointer].accept(this);
			this.instructionPointer++;
		}
		
		LOGGER.info("program completed...");
	}
	
	public void reset() {
		this.environment.clear();
		LOGGER.info("cleared interpreter environment");
		
		this.machineLabelledInstruction.clear();
		LOGGER.info("cleared machine labelled instruction");
		
		this.accumulator = 0;
		LOGGER.info("reset accumulator");
		
		this.inputNum = 0;
		LOGGER.info("reset input num");
		
		this.instructionPointer = 0;
		LOGGER.info("reset instruction pointer");
		
		this.instructionsArray = new MachineInstruction[0];
		LOGGER.info("instruction array cleared");
	}
	
	/**
	 * The memory address are converted in string in this interpreter
	 * @param address
	 * @return memory address
	 */
	private String convertMemoryAddress(Integer address) {
		return address.toString();
	}
	
	private void assertVariableInitialized(String id, int line) {
		if(!environment.containsKey(id)) {
			throw new VariableNotInitializedException("Trying to use a variable "+id+" not "
					+ "initialized at line "+line);
		}

		this.debugStack();
	}
	
	private void debugStack() {
		if(LOGGER.isDebugEnabled()) {
			ArrayList<String> list = new ArrayList<>();
			boolean nonTrovato = false;
			for(int i=0; !nonTrovato; i++) {
				if(!environment.containsKey(i+"")) {
					nonTrovato = true;
				}else {
					list.add("<"+i+", "+environment.get(""+i)+">");
				}
				
			}
			LOGGER.debug("Stack:"+list);
		}
	}
	
	
	private Integer getStandardValue(StandardArgument arg, int line) {
		String id = arg.getIdentifier().getIdentifier();
		this.assertVariableInitialized(id, line);
				
		return environment.get(id);
	}
	
	private Integer getReferencedValue(ReferenceArgument arg, int line) {
		String id = arg.getIdentifier().getIdentifier();
		
		this.assertVariableInitialized(id, line);
		
		Integer address = environment.get(id);
		String memoryAddress = this.convertMemoryAddress(address);
		
		this.assertVariableInitialized(memoryAddress, line);
				
		return environment.get(memoryAddress);
	}
	
	
	@Override
	public void visit(DivStandardInstruction divStandardInstruction) {
		StandardArgument arg = divStandardInstruction.getArgument();

		Integer value = this.getStandardValue(arg, divStandardInstruction.getLine());
		
		if(value==0) {
			throw new ZeroDivisionException("Zero division at line "+divStandardInstruction.getLine());
		}
		
		this.accumulator = (int) this.accumulator/value;
	}

	@Override
	public void visit(DivReferenceInstruction divReferenceInstruction) {
		ReferenceArgument arg = divReferenceInstruction.getArgument();
		
		Integer value = this.getReferencedValue(arg, divReferenceInstruction.getLine());
		
		if(value==0) {
			throw new ZeroDivisionException("Zero division at line "+divReferenceInstruction.getLine());
		}
		
		this.accumulator = (int) this.accumulator/value;
	}

	@Override
	public void visit(DivDirectInstruction divDirectInstruction) {
		DirectArgument arg = divDirectInstruction.getArgument();
		
		Integer value = arg.getValue().getIntValue();
		
		if(value==0) {
			throw new ZeroDivisionException("Zero division at line "+divDirectInstruction.getLine());
		}
		
		this.accumulator = (int) this.accumulator/value;
	}
	
	@Override
	public void visit(AddDirectInstruction addDirectInstruction) {
		DirectArgument arg = addDirectInstruction.getArgument();
		
		Integer value = arg.getValue().getIntValue();
		
		this.accumulator = this.accumulator + value;
	}
	
	@Override
	public void visit(AddStandardInstruction addStandardInstruction) {
		StandardArgument arg = addStandardInstruction.getArgument();
		
		Integer value = this.getStandardValue(arg, addStandardInstruction.getLine());
		
		this.accumulator = this.accumulator + value;
	}

	@Override
	public void visit(AddReferenceInstruction addReferenceInstruction) {
		ReferenceArgument arg = addReferenceInstruction.getArgument();
		
		Integer value = this.getReferencedValue(arg, addReferenceInstruction.getLine());
		
		this.accumulator = this.accumulator + value;
	}

	
	@Override
	public void visit(HaltInstruction haltInstruction) {
		LOGGER.info("HALT reached. The Interpreter is shutting down...");
		this.systemHalted = true;
	}

	private void assertLabelledBlockExists(int instructionLine, Identifier label) {
		if(!this.machineLabelledInstruction.containsKey(label.getIdentifier())) {
			throw new LabelNotFoundException("Label "+label.getIdentifier()+" not found. Error line: "
					+ ""+instructionLine);
		}
	}
	
	private void assertLabelledBlockExists(int instructionLine, String label) {
		if(!this.machineLabelledInstruction.containsKey(label)) {
			throw new LabelNotFoundException("Label "+label+" not found. Error line: "
					+ ""+instructionLine);
		}
	}
	
	private void jumpToInstruction(String label) {
		Integer elem = this.machineLabelledInstruction.get(label);
				
		this.instructionPointer = elem;
	}
	
	@Override
	public void visit(JGEZInstruction jgezInstruction) {
		Identifier label = jgezInstruction.getArgument().getIdentifier();
		
		assertLabelledBlockExists(jgezInstruction.getLine(), label);
		
		if(this.accumulator>=0) {
			this.jumpToInstruction(label.getIdentifier());
		}
	}

	@Override
	public void visit(JGZInstruction jgzInstruction) {
		Identifier label = jgzInstruction.getArgument().getIdentifier();
		
		assertLabelledBlockExists(jgzInstruction.getLine(), label);
		
		if(this.accumulator>0) {
			this.jumpToInstruction(label.getIdentifier());
		}
	}

	@Override
	public void visit(JLEZInstruction jlezInstruction) {
		Identifier label = jlezInstruction.getArgument().getIdentifier();
		
		assertLabelledBlockExists(jlezInstruction.getLine(), label);
		
		if(this.accumulator<=0) {
			this.jumpToInstruction(label.getIdentifier());
		}
	}

	@Override
	public void visit(JLZInstruction jlzInstruction) {
		Identifier label = jlzInstruction.getArgument().getIdentifier();
		
		assertLabelledBlockExists(jlzInstruction.getLine(), label);
		
		if(this.accumulator<0) {
			this.jumpToInstruction(label.getIdentifier());
		}
	}

	@Override
	public void visit(JNZInstruction jnzInstruction) {
		Identifier label = jnzInstruction.getArgument().getIdentifier();
		
		assertLabelledBlockExists(jnzInstruction.getLine(), label);
		
		if(this.accumulator!=0) {
			this.jumpToInstruction(label.getIdentifier());
		}
	}

	@Override
	public void visit(JUMPInstruction jumpInstruction) {
		Identifier label = jumpInstruction.getArgument().getIdentifier();
		
		assertLabelledBlockExists(jumpInstruction.getLine(), label);
		
		this.jumpToInstruction(label.getIdentifier());
	}
	
	@Override
	public void visit(JUMPReferenceInstruction jumpReferenceInstruction) {
		ReferenceArgument arg = jumpReferenceInstruction.getArgument();

		Integer labelToJump = this.getReferencedValue(arg, jumpReferenceInstruction.getLine());
		
		assertLabelledBlockExists(jumpReferenceInstruction.getLine(), labelToJump.toString());
		
		this.jumpToInstruction(labelToJump.toString());
	}

	@Override
	public void visit(JZInstruction jzInstruction) {
		Identifier label = jzInstruction.getArgument().getIdentifier();
		
		assertLabelledBlockExists(jzInstruction.getLine(), label);
		
		if(this.accumulator==0) {
			this.jumpToInstruction(label.getIdentifier());
		}
		
	}
	
	
	@Override
	public void visit(LoadDirectInstruction loadDirectInstruction) {
		Integer value = loadDirectInstruction.getArgument().getValue().getIntValue();
		
		this.accumulator = value;		
	}

	@Override
	public void visit(LoadReferenceInstruction loadReferenceInstruction) {
		ReferenceArgument arg = loadReferenceInstruction.getArgument();

		Integer valueToLoad = this.getReferencedValue(arg, loadReferenceInstruction.getLine());
		
		this.accumulator = valueToLoad;
	}

	@Override
	public void visit(LoadStandardInstruction loadStandardInstruction) {
		StandardArgument arg = loadStandardInstruction.getArgument();
		
		Integer valueToLoad = this.getStandardValue(arg, loadStandardInstruction.getLine());
		
		this.accumulator = valueToLoad;
	}

	@Override
	public void visit(MulDirectInstruction mulDirectInstruction) {
		Integer value = mulDirectInstruction.getArgument().getValue().getIntValue();
		
		this.accumulator = this.accumulator * value;
	}

	@Override
	public void visit(MulReferenceInstruction mulReferenceInstruction) {
		ReferenceArgument arg = mulReferenceInstruction.getArgument();
		
		Integer value = this.getReferencedValue(arg, mulReferenceInstruction.getLine());
		
		this.accumulator = this.accumulator * value;
	}

	@Override
	public void visit(MulStandardInstruction mulStandardInstruction) {
		StandardArgument arg = mulStandardInstruction.getArgument();
		
		Integer value = this.getStandardValue(arg, mulStandardInstruction.getLine());
		
		this.accumulator = this.accumulator * value;
	}

	@Override
	public void visit(SubDirectInstruction subDirectInstruction) {
		Integer value = subDirectInstruction.getArgument().getValue().getIntValue();
		
		this.accumulator = this.accumulator - value;
	}

	@Override
	public void visit(SubReferenceInstruction subReferenceInstruction) {
		ReferenceArgument arg = subReferenceInstruction.getArgument();
		
		Integer value = this.getReferencedValue(arg, subReferenceInstruction.getLine());
		
		this.accumulator = this.accumulator - value;
		
	}

	@Override
	public void visit(SubStandardInstruction subStandardInstruction) {
		StandardArgument arg = subStandardInstruction.getArgument();
		
		Integer value = this.getStandardValue(arg, subStandardInstruction.getLine());
		
		this.accumulator = this.accumulator - value;
	}
	
	@Override
	public void visit(WriteDirectInstruction writeDirectInstruction) {
		Integer value = writeDirectInstruction.getArgument().getValue().getIntValue();
		
		this.output.printOut(value.toString());
	}

	@Override
	public void visit(WriteReferenceInstruction writeReferenceInstruction) {
		ReferenceArgument arg = writeReferenceInstruction.getArgument();
		
		Integer value = this.getReferencedValue(arg, writeReferenceInstruction.getLine());
		
		this.output.printOut(value.toString());
	}

	@Override
	public void visit(WriteStandardInstruction writeStandardInstruction) {
		StandardArgument arg = writeStandardInstruction.getArgument();
		
		Integer value = this.getStandardValue(arg, writeStandardInstruction.getLine());
		
		this.output.printOut(value.toString());
	}
	
	@Override
	public void visit(ReadReferenceInstruction readReferenceInstruction) {
		ReferenceArgument arg = readReferenceInstruction.getArgument();
		String id = arg.getIdentifier().getIdentifier();
		
		//assert that the variable exists in the env
		this.assertVariableInitialized(id, readReferenceInstruction.getLine());
		
		// get the address where the variable will be stored
		Integer address = this.environment.get(id);
		
		String addressToPutNewVariable = this.convertMemoryAddress(address);
		
		//read value from input
		this.output.printOut("Waiting for input ("+this.inputNum+") >>>");
		Integer value = this.reader.readInt();
		
		//add or update the value
		this.environment.put(addressToPutNewVariable, value);
		
		this.inputNum++; 
	}

	@Override
	public void visit(ReadStandardInstruction readStandardInstruction) {
		StandardArgument arg = readStandardInstruction.getArgument();
		String id = arg.getIdentifier().getIdentifier();
		
		//read value from input
		this.output.printOut("Waiting for input ("+this.inputNum+") >>>");
		Integer value = this.reader.readInt();
		
		//add or update the value
		this.environment.put(id, value);
		
				
		this.inputNum++; 
	}

	@Override
	public void visit(StoreReferenceInstruction storeReferenceInstruction) {
		ReferenceArgument arg = storeReferenceInstruction.getArgument();
		String id = arg.getIdentifier().getIdentifier();
		
		//assert that the variable exists in the env
		this.assertVariableInitialized(id, storeReferenceInstruction.getLine());
		
		// get the address where the variable will be stored
		Integer address = this.environment.get(id);
		
		String addressToPutNewVariable = this.convertMemoryAddress(address);

		//add or update the value
		this.environment.put(addressToPutNewVariable, this.accumulator);
	}

	@Override
	public void visit(StoreStandardInstruction storeStandardInstruction) {
		StandardArgument arg = storeStandardInstruction.getArgument();
		String id = arg.getIdentifier().getIdentifier();
		
		//add or update the value
		this.environment.put(id, this.accumulator);
	}
	
	@Override
	public void visit(InstructionBlock instructionBlock) {
		//do nothing
	}
	

}
