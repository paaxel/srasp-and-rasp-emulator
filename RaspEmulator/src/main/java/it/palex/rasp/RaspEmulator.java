package it.palex.rasp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import it.palex.rasp.utils.FileUtility;

public class RaspEmulator {

	private static final Logger LOGGER = LogManager.getLogger(RaspEmulator.class);

	public static void main(String... args) throws FileNotFoundException {

		Scanner sc = new Scanner(System.in);

		String srcFileName = null;
		File sourceFile = null;

		boolean invalidFileGiven = true;

		while (invalidFileGiven) {
			invalidFileGiven = false;

			System.out.println("Enter the name of the source file >> ");
			srcFileName = sc.nextLine();
			sourceFile = new File(srcFileName);

			if (!sourceFile.exists()) {
				System.out.println("File does not exist! Please try again...");
				invalidFileGiven = true;
			} else {
				if (!FileUtility.checkIfIsAFileWithExtension(sourceFile, "rasp")) {
					System.out.println("The file must have a .rasp extension. Please try again...");
					invalidFileGiven = true;
				}
			}
		}


		
		// execution phase
		OutputObserver output = new CommandLineOutputObserver();
		InputReader reader = new SystemInReader();
		
		
		EmulatorFacade emulator = new EmulatorFacade(new FileInputStream(srcFileName), output, reader);

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

		// close Scanner after execute program otherwise you will get an error because System.in is used also by system reader
		sc.close();
	}

}
