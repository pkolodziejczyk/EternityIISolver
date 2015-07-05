/**
 * 
 */
package org.k.eternity;

/**
 * @author EXT-PKO
 * 
 */
public class EternityBoard {

	public static final int DEFAULT_SIZE = 16;
	private int SIZE = 16;

	private Piece border;

	public Piece getBorder() {
		return border;
	}

	private OrientedPiece[][] board;

	public EternityBoard(int size) {
		this.setSIZE(size);
		board = new OrientedPiece[getSIZE()][getSIZE()];
		border = new Piece(0, 0, 0, 0, 0);
	}

	/**
	 * @param x
	 *            orientation left-right
	 * @param y
	 *            orientation top-bot
	 * @return
	 */
	public Piece getPieceAt(int x, int y) {
		if (x < 0 || y < 0 || x >= getSIZE() || y >= getSIZE()) {
			return border;
		}
		return this.board[x][y];
	}

	public boolean match(OrientedPiece piece, int x, int y) {
		if (!matchTop(piece, x, y) || !matchRight(piece, x, y)
				|| !matchBot(piece, x, y) || !matchLeft(piece, x, y)) {
			return false;
		}
		return true;
	}

	/**
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean matchLeft(OrientedPiece piece, int x, int y) {
		Piece constaintPiece = getPieceAt(x - 1, y);
		if (constaintPiece != null) {
			if (constaintPiece.getRight() != piece.getLeft()) {
				return false;
			}
		} else {
			if (0 == piece.getLeft()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean matchBot(OrientedPiece piece, int x, int y) {
		Piece constaintPiece = getPieceAt(x, y + 1);
		if (constaintPiece != null) {
			if (constaintPiece.getTop() != piece.getBot()) {
				return false;
			}
		} else {
			if (0 == piece.getBot()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean matchRight(OrientedPiece piece, int x, int y) {
		Piece constaintPiece = getPieceAt(x + 1, y);
		if (constaintPiece != null) {
			if (constaintPiece.getLeft() != piece.getRight()) {
				return false;
			}
		} else {
			if (0 == piece.getRight()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean matchTop(OrientedPiece piece, int x, int y) {
		Piece constaintPiece = getPieceAt(x, y - 1);
		if (constaintPiece != null) {
			if (constaintPiece.getBot() != piece.getTop()) {
				return false;
			}
		} else {
			if (0 == piece.getTop()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param piece
	 * @param x
	 * @param y
	 */
	public void setPieceAt(OrientedPiece piece, int x, int y) {
		this.board[x][y] = piece;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int column = 0; column < getSIZE(); column++) {
			for (int row = 0; row < getSIZE(); row++) {
				OrientedPiece piece = (OrientedPiece) this.getPieceAt(row, column);
				if (piece != null) {
					sb.append(piece.getId()+"\\"+piece.getOrientation()+ "\t");
				} else {
					sb.append("\t");
				}

			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public int getSIZE() {
		return SIZE;
	}

	public void setSIZE(int sIZE) {
		SIZE = sIZE;
	}
}
