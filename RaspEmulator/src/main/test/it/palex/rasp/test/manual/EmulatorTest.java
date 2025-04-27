package it.palex.rasp.test.manual;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.palex.rasp.EmulatorFacade;
import it.palex.rasp.executors.exception.HaltInstructionNotFoundException;
import it.palex.rasp.executors.exception.LabelNotFoundException;
import it.palex.rasp.executors.exception.ParsingException;
import it.palex.rasp.executors.exception.UnknownTokenException;
import it.palex.rasp.executors.exception.VariableNotInitializedException;
import it.palex.rasp.executors.exception.ZeroDivisionException;
import it.palex.rasp.inout.CommandLineOutputObserver;
import it.palex.rasp.inout.InputReader;
import it.palex.rasp.inout.OutputObserver;
import it.palex.rasp.inout.SystemInReader;

public class EmulatorTest {

	private static final Logger LOGGER = LogManager.getLogger(EmulatorTest.class);
	
	public static void main(String...args) throws FileNotFoundException {
		Scanner sc = new Scanner(System.in);

		InputStream srcFileInputStream = EmulatorTest.class.getResourceAsStream("/it.palex.rasp.examples/primo.rasp");

		OutputObserver output = new CommandLineOutputObserver();
		InputReader reader = new SystemInReader();

		EmulatorFacade emulator = new EmulatorFacade(srcFileInputStream, output, reader);

		try {
			emulator.executeProgram();

		}  catch (HaltInstructionNotFoundException | LabelNotFoundException | ParsingException |
				UnknownTokenException | VariableNotInitializedException | ZeroDivisionException e) {
			System.out.println(e.getMessage());
		}  catch (IOException e) {
			LOGGER.fatal(e);
			System.out.println(e.getMessage());
		}  catch (Exception e) {
			System.out.println("Emulator Error: -->"+e.getMessage());
			e.getStackTrace();
			LOGGER.fatal(e);
		}

		// close Scanner afer execute program or you will get an error because System.in
		// is
		// used also by system reader
		sc.close();
	}
	
	
}
