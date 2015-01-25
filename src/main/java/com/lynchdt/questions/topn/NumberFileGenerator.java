package com.lynchdt.questions.topn;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

public class NumberFileGenerator {
	
	private final Random rand = new Random();
	private final String fileName;
	private final long numLines;
	
	private FileWriter fileWriter;
	private PrintWriter printWriter;
	
	private NumberFileGenerator(String fileName, long numLines) {
		this.fileName=fileName;
		this.numLines=numLines;
	}
	
	public void execute() {
		try {
			openForWriting();
			write();
			cleanUp();
		} catch(Exception ex) {
			System.err.print(ex.getStackTrace());
		}
	}
	
	private void openForWriting() throws Exception {
		fileWriter = new FileWriter(fileName, true); /** Append */
		printWriter = new PrintWriter(fileWriter);
	}
	
	private void write() throws Exception {
		for(int i = 0; i < numLines; i++) {
			printWriter.println(Long.toString(rand.nextLong()));
		}
	}
	
	private void cleanUp() throws Exception {
		printWriter.close();
	}
	
	public static void main(String argsv[]) {
		if(argsv.length!=2) {
			System.out.println("java NumberFileGenerator file lineCount");
			System.exit(-1);
		}
		String file = argsv[0];
		long lines = Long.parseLong(argsv[1]);
		NumberFileGenerator generator = new NumberFileGenerator(file, lines);
		generator.execute();
	}
}	

