package org.k.eternity.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MultyEternityToJson {
	private static final String HISTO_RECORD_TXT = "trace_g2_0_4_fail.txt";//"histoClean.txt";
	private static String jsonFile = "eternity.json";

	public static void main(String[] args) throws FileNotFoundException {
		Node root = new Node("processing");
		//String record = "0:0=>1/4;0:15=>2/3;15:0=>1/2;15:15=>1/1;14:15=>1/10;0:1=>1/11;0:14=>1/10;1:15=>2/10;1:14=>2/5;2:14=>1/1;2:15=>1/1;1:13=>1/3;0:13=>1/1;0:2=>1/11;1:2=>1/3;1:1=>2/2;1:0=>1/1;2:1=>1/3;2:0=>1/1;0:3=>1/7;1:3=>1/2;2:3=>1/4;0:4=>1/6;1:4=>1/1;2:4=>1/2;0:12=>1/8;0:11=>1/7;0:10=>1/7;0:9=>1/6;0:8=>1/5;0:7=>1/4;3:15=>1/5;15:14=>1/4;14:14=>1/2;13:14=>1/3;13:15=>1/1;14:13=>3/3;15:13=>1/1;3:14=>1/5;3:13=>1/1;15:1=>1/5;0:6=>1/6;0:5=>1/1;1:5=>2/2;1:6=>1/1;1:7=>1/3;2:5=>1/3;1:8=>1/4;1:9=>1/3;1:10=>2/3;1:11=>1/3;1:12=>1/1;2:12=>1/1;2:11=>1/2;2:6=>1/3;2:7=>1/3;2:10=>2/3;2:8=>3/4;2:9=>1/1;3:12=>2/3;3:11=>1/1;3:10=>1/1;3:9=>1/4;3:8=>1/2;3:7=>1/2;3:6=>1/4;3:5=>1/5;3:4=>1/4;3:3=>1/1;3:2=>1/3;3:1=>1/2;3:0=>1/2;4:0=>1/4;4:1=>1/3;4:2=>1/2;4:3=>1/2;4:4=>1/1;4:5=>1/2;4:6=>1/1;4:7=>1/1;4:8=>1/3;4:9=>1/1;4:10=>1/2;4:11=>4/4;4:12=>1/3;4:13=>2/3;4:14=>2/4;4:15=>1/1;15:12=>2/5;14:12=>1/1;13:12=>1/1;15:11=>2/4;14:11=>1/4;13:11=>1/3;14:0=>5/8;13:0=>1/3;12:0=>2/2;13:1=>1/2;12:1=>1/1;14:1=>1/1;14:2=>1/1;15:2=>1/2;15:3=>1/3;14:3=>4/4;13:3=>1/1;12:2=>2/5;12:3=>1/2;5:0=>5/7;5:1=>1/1;5:2=>2/3;5:3=>2/2;5:4=>1/3;5:5=>2/4;5:6=>2/2;5:7=>3/3;5:8=>4/5;6:8=>2/2;5:9=>2/3;6:9=>1/2;6:7=>3/3;7:7=>3/3;5:10=>2/4;5:11=>2/3;5:12=>1/2;5:13=>1/1;5:14=>2/2;5:15=>1/2;6:10=>1/3;6:11=>1/1;6:12=>1/2;6:13=>1/2;6:14=>2/2;6:15=>1/1;6:6=>1/3;6:5=>1/2;6:4=>1/1;6:3=>1/2;6:2=>1/1;7:6=>2/2;7:5=>2/2;7:4=>2/2;7:3=>1/1;7:2=>2/2;6:1=>3/3;6:0=>1/1;7:1=>2/2;7:0=>2/2;12:15=>3/3;11:15=>1/2;10:15=>1/2;12:14=>2/2;12:13=>1/1;12:12=>1/2;12:11=>3/3;7:9=>1/4;7:10=>1/3;7:11=>2/3;7:12=>1/1;7:13=>1/4;7:14=>1/4;7:15=>1/1;11:0=>1/3;11:1=>1/2;11:2=>1/1;11:3=>1/1;15:10=>2/2;14:10=>1/1;13:10=>1/3;12:10=>1/2;15:9=>2/3;14:9=>1/1;15:8=>1/1;14:8=>1/1;15:7=>1/1;14:7=>2/2;13:9=>3/3;12:9=>1/1;13:8=>1/2;12:8=>1/1;13:7=>1/1;12:7=>1/1;11:14=>2/4;11:13=>1/1;10:14=>1/2;10:13=>1/1;11:12=>2/2;11:11=>1/1;10:12=>2/2;10:11=>3/3;11:10=>2/3;10:10=>1/1;11:9=>1/1;11:8=>1/1;10:9=>1/2;10:8=>1/1;11:7=>1/2;10:7=>1/2;8:0=>1/5;8:1=>1/1;";
//		Scanner scanner = new Scanner(new File("recoverFile.txt"));
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			String[] currentHisto = line.split(" -- ");
//			String endOfTrace = currentHisto[1];
//			String startOfTrace = currentHisto[0];
//			addStartToRoot(root,startOfTrace);
//			addEndToRoot(root,endOfTrace);
//			
//		}
//		scanner.close();
		
		addCleanHisto(root);
		FileWriter writer;
		try {
			writer = new FileWriter(jsonFile , false);
				writer.append(root.toString().replaceAll(",]", "]"));
			writer.flush();
			writer.close();
		} catch (IOException e) {

		}
		
	}

	private static void addCleanHisto(Node root) throws FileNotFoundException {
		Scanner scannerRecord = new Scanner(new File(HISTO_RECORD_TXT));
		while (scannerRecord.hasNextLine()) {
			String line = scannerRecord.nextLine();
			addRecordToRoot(root,line);
			
		}
		scannerRecord.close();
	}
	
	private static void addRecordToRoot(Node root, String record) {
		Node parent = null;
		Node nextParent = root;
		String[] steps = record.split(";");
		for (String step : steps) {
			//String position = step.split("=>")[0];
			String match = step.split("=>")[1];
			parent = nextParent;
			int current = Integer.parseInt(match.split("/")[0]);
			int total = Integer.parseInt(match.split("/")[1]);
			if(parent.getChildren().size() >0){
				nextParent = parent.getChildren().get(current-1);
			}else {
				for (int i = 1; i <= total; i++) {
					String type = "done";
					if(i == current){
						type = "record";
					}
					if(i>current){
						type = "done";
					}
					Node currentNode = new Node(type);
					parent.addNode(currentNode);
					if(i == current){
						nextParent = currentNode;
					}
				}
			}
		}
	}

	private static void addEndToRoot(Node root, String startOfTrace) {
		Node parent = null;
		Node nextParent = root;
		String[] steps = startOfTrace.split(";");
		for (String step : steps) {
			//String position = step.split("=>")[0];
			String match = step.split("=>")[1];
			parent = nextParent;
			int current = Integer.parseInt(match.split("/")[0]);
			int total = Integer.parseInt(match.split("/")[1]);
			if(parent.getChildren().size() >0){
				nextParent = parent.getChildren().get(current-1);
			}else {
				for (int i = 1; i <= total; i++) {
					String type = "done";
					if(i == current){
						type = "processing";
					}
					if(i>current){
						type = "todo";
					}
					Node currentNode = new Node(type);
					parent.addNode(currentNode);
					if(i == current){
						nextParent = currentNode;
					}
				}
			}
		}
	}

	private static void addStartToRoot(Node root, String startOfTrace) {
		Node parent = null;
		Node nextParent = root;
		String[] steps = startOfTrace.split(";");
		for (String step : steps) {
			//String position = step.split("=>")[0];
			String match = step.split("=>")[1];
			parent = nextParent;
			int current = Integer.parseInt(match.split("/")[0]);
			int total = Integer.parseInt(match.split("/")[1]);
			if(parent.getChildren().size() >0){
				nextParent = parent.getChildren().get(current-1);
			}else {
				for (int i = 1; i <= total; i++) {
					String type = "processing";
					if(i == current){
						type = "processing";
					}
					if(i>current){
						type = "processing";
					}
					Node currentNode = new Node(type);
					parent.addNode(currentNode);
					if(i == current){
						nextParent = currentNode;
					}
				}
			}
		}
	}
}
