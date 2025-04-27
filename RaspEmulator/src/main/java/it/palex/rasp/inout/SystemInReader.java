package it.palex.rasp.inout;

import java.util.Scanner;

public class SystemInReader implements InputReader {

	private Scanner sc;
	
	public SystemInReader() {
		this.sc = new Scanner(System.in);
	}

	@Override
	public int readInt() {
		return this.sc.nextInt();
	}

	@Override
	public void close() {
		this.sc.close();
	}
	
}
