/**
 * 
 */
package org.k.eternity;

/**
 * @author EXT-PKO
 * 
 */
public class Piece {
	
	private int id;
	protected int top, right, left, bot;

	

	public int getTop() {
		return top;
	}

	public int getRight() {
		return right;
	}

	public int getLeft() {
		return left;
	}

	public int getBot() {
		return bot;
	}

	public Piece(int top, int right, int bot, int left) {
		this.top = top;
		this.right = right;
		this.bot = bot;
		this.left = left;
	}

	public Piece(int id, int top, int right, int bot, int left) {
		this.top = top;
		this.right = right;
		this.bot = bot;
		this.left = left;
		this.id = id;
	}

	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
