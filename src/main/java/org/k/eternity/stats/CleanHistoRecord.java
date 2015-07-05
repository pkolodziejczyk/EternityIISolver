package org.k.eternity.stats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class CleanHistoRecord {

	static String cleanHisto ="histoClean.txt";
	public static void main(String[] args) throws IOException {
		FileWriter writer;
		
		writer = new FileWriter(cleanHisto , false);
		Scanner scannerRecord = new Scanner(new File("histoRecord.txt"));
		while (scannerRecord.hasNextLine()) {
			String line = scannerRecord.nextLine();
			if(countMatches(line, ';')>=209){
				writer.append(line+"\n");
			}
			
		}
		writer.flush();
		writer.close();
		scannerRecord.close();
	}
	private static int countMatches(String line, char toMatch) {
		int toReturn =0;
		for (int i = 0; i < line.length(); i++) {
			if(line.charAt(i) == toMatch){
				toReturn++;
			}
		}
		return toReturn;
	}
}
