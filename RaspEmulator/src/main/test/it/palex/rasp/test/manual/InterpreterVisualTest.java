package it.palex.rasp.test.manual;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.palex.rasp.executors.RaspInterpreterVisitor;
import it.palex.rasp.inout.CommandLineOutputObserver;
import it.palex.rasp.inout.InputReader;
import it.palex.rasp.inout.OutputObserver;
import it.palex.rasp.inout.SystemInReader;
import it.palex.rasp.parser.operations.DirectArgument;
import it.palex.rasp.parser.operations.Identifier;
import it.palex.rasp.parser.operations.InstructionBlock;
import it.palex.rasp.parser.operations.NumberValue;
import it.palex.rasp.parser.operations.generic.MachineInstruction;
import it.palex.rasp.parser.operations.instructions.HaltInstruction;
import it.palex.rasp.parser.operations.instructions.WriteDirectInstruction;

public class InterpreterVisualTest {
	
	private static final Logger LOGGER = LogManager.getLogger(InterpreterVisualTest.class);
	
	public static void main(String...args) {
		LOGGER.entry();
		firstTest();
	}
		
	private static void firstTest() {
		List<MachineInstruction> blockInstructions = new ArrayList<>();
		MachineInstruction halt = new HaltInstruction(0);
		blockInstructions.add(halt);
		
		InstructionBlock labeledBlock = new InstructionBlock(
				blockInstructions, new Identifier("STOP"), 0);
		
		
		List<MachineInstruction> programInstructions = new ArrayList<>();
		
		WriteDirectInstruction write10 = new WriteDirectInstruction(
				new DirectArgument(new NumberValue(10)), 0);
		
		
		programInstructions.add(write10);
		programInstructions.add(labeledBlock);
		
		OutputObserver output = new CommandLineOutputObserver();
		InputReader reader = new SystemInReader();
		
		RaspInterpreterVisitor interpreter = new RaspInterpreterVisitor(programInstructions, output, reader);
		
		interpreter.startInterpretation();
		
	}

	
}
