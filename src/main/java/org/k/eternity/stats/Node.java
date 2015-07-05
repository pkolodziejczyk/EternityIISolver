package org.k.eternity.stats;

import java.util.List;
import java.util.ArrayList;

public class Node {
	private List<Node> children = new ArrayList<Node>();
	String type;
	public Node(String type) {
		this.type = type;
	}
	
	public void addNode(Node node){
		getChildren().add(node);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"type\":");
		sb.append("\"");
		sb.append(type);
		sb.append("\"");
		if(getChildren().size()>0){
			sb.append(",");
			sb.append("\"children\":[");
			for (Node child : getChildren()) {
				sb.append(child.toString());
				sb.append(",");
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}
}
