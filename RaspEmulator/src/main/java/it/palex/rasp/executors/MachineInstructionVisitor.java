package it.palex.rasp.executors;

import it.palex.rasp.parser.operations.InstructionBlock;
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

public interface MachineInstructionVisitor {

	public void visit(DivStandardInstruction divStandardInstruction);

	public void visit(AddDirectInstruction addDirectInstruction);

	public void visit(DivReferenceInstruction divReferenceInstruction);

	public void visit(DivDirectInstruction divDirectInstruction);

	public void visit(AddStandardInstruction addStandardInstruction);

	public void visit(AddReferenceInstruction addReferenceInstruction);

	public void visit(HaltInstruction haltInstruction);

	public void visit(JGEZInstruction jgezInstruction);

	public void visit(JGZInstruction jgzInstruction);

	public void visit(JLEZInstruction jlezInstruction);

	public void visit(JLZInstruction jlzInstruction);

	public void visit(JNZInstruction jnzInstruction);

	public void visit(JUMPInstruction jumpInstruction);

	public void visit(JZInstruction jzInstruction);

	public void visit(LoadDirectInstruction loadDirectInstruction);

	public void visit(LoadReferenceInstruction loadReferenceInstruction);

	public void visit(LoadStandardInstruction loadStandardInstruction);

	public void visit(MulDirectInstruction mulDirectInstruction);

	public void visit(MulReferenceInstruction mulReferenceInstruction);

	public void visit(MulStandardInstruction mulStandardInstruction);

	public void visit(ReadReferenceInstruction readReferenceInstruction);

	public void visit(ReadStandardInstruction readStandardInstruction);

	public void visit(StoreReferenceInstruction storeReferenceInstruction);

	public void visit(StoreStandardInstruction storeStandardInstruction);

	public void visit(SubDirectInstruction subDirectInstruction);

	public void visit(SubReferenceInstruction subReferenceInstruction);

	public void visit(SubStandardInstruction subStandardInstruction);

	public void visit(WriteDirectInstruction writeDirectInstruction);

	public void visit(WriteReferenceInstruction writeReferenceInstruction);

	public void visit(WriteStandardInstruction writeStandardInstruction);

	public void visit(InstructionBlock instructionBlock);

	public void visit(JUMPReferenceInstruction jumpReferenceInstruction);

}
