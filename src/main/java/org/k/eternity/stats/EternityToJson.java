package org.k.eternity.stats;

public class EternityToJson {
	static String eternity = "0:0=>1/1291;0:1=>2/47;1:0=>2/50;1:1=>1/20;0:2=>1/437;1:2=>1/21;0:3=>1/476;1:3=>1/46;2:0=>1/445;2:1=>1/29;2:2=>1/14;2:3=>1/55;3:0=>1/195;3:1=>2/23;3:2=>3/15;3:3=>1/5;4:0=>1/45;4:1=>1/2;4:2=>1/1;4:3=>1/1;0:7=>25/134;0:6=>1/12;1:7=>11/19;1:6=>6/22;2:7=>22/28;2:6=>3/27;3:7=>57/63;3:6=>24/25;5:0=>49/54;";
	static int index =0;

public static void main(String[] args) {
	String[] steps = eternity.split(";");

	Node root = new Node("processing");
	Node parent = null;
	Node nextParent = root;
	for (String step : steps) {
		//String position = step.split("=>")[0];
		String match = step.split("=>")[1];
		parent = nextParent;
		int current = Integer.parseInt(match.split("/")[0]);
		int total = Integer.parseInt(match.split("/")[1]);
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
	System.out.println(root.toString().replaceAll(",]", "]"));
}

}
