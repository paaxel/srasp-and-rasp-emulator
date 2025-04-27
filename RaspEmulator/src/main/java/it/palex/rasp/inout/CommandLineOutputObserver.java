package it.palex.rasp.inout;

public class CommandLineOutputObserver implements OutputObserver {

	@Override
	public void printOut(String output) {
		System.out.println(output);
	}

}
