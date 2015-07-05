package org.k.eternity;
/**
 * 
 */


/**
 * @author EXT-PKO
 *
 */
public class OrientedPiece extends Piece{
	
	private Orientation orientation = Orientation.TOP;
	private boolean isLock = false;

	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * @param top
	 * @param right
	 * @param bot
	 * @param left
	 */
	public OrientedPiece(int top, int right, int bot, int left) {
		super(top, right, bot, left);
	}
	
	/**
	 * @param id
	 * @param top
	 * @param right
	 * @param bot
	 * @param left
	 */
	public OrientedPiece(int id, int top, int right, int bot, int left) {
		super(id, top, right, bot, left);
	}

	
	public int getTop() {
		switch (orientation) {
		case TOP:
			return top;
		case RIGHT:
			return right;
		case BOT:
			return bot;
		case LEFT:
			return left;
		default:
			throw new RuntimeException("Orientation inconnu");
		}
	}

	public int getBot() {
		switch (orientation) {
		case TOP:
			return bot;
		case RIGHT:
			return left;
		case BOT:
			return top;
		case LEFT:
			return right;
		default:
			throw new RuntimeException("Orientation inconnu");
		}
	}

	public int getLeft() {
		switch (orientation) {
		case TOP:
			return left;
		case RIGHT:
			return top;
		case BOT:
			return right;		
		case LEFT:
			return bot;
		default:
			throw new RuntimeException("Orientation inconnu");
		}
	}

	public int getRight() {
		switch (orientation) {
		case TOP:
			return right;
		case RIGHT:
			return bot;
		case BOT:
			return left;
		case LEFT:
			return top;
		default:
			throw new RuntimeException("Orientation inconnu");
		}
	}
	
	public void turnLeft(){
		switch (orientation) {
		case TOP:
			this.orientation = Orientation.RIGHT;
			break;
		case RIGHT:
			this.orientation = Orientation.BOT;
			break;
		case BOT:
			this.orientation = Orientation.LEFT;
			break;
		case LEFT:
			this.orientation = Orientation.TOP;
			break;
		}
	}
	public void setOrientation(Orientation orientation){
		this.orientation = orientation;
	}

	/**
	 * 
	 */
	public void resetOrientation() {
		this.orientation = Orientation.TOP;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId()+"/"+orientation;
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

}
