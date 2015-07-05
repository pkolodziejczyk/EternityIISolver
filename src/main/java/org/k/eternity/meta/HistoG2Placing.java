package org.k.eternity.meta;

import java.util.ArrayList;
import java.util.List;

import org.k.eternity.EternityBoard;
import org.k.eternity.HistoPlacing;
import org.k.eternity.OrientedPiece;

public class HistoG2Placing extends HistoPlacing {
	
	private  MetaHashArrayList[][] g2Possible = new MetaHashArrayList[8][8];

	private List<G2> g2List = new ArrayList<G2>();
	private boolean isNew = true;
	public HistoG2Placing() {
	}

	public HistoG2Placing(int x, int y, int heat, int nbTry, HistoPlacing previous) {
		super(x, y, heat, nbTry, previous);
	}

	public HistoPlacing revert(OrientedPiece[] pieces, EternityBoard board) {
		resetPiece(pieces, board, x * 2, y * 2);
		resetPiece(pieces, board, x * 2 + 1, y * 2);
		resetPiece(pieces, board, x * 2 + 1, y * 2 + 1);
		resetPiece(pieces, board, x * 2, y * 2 + 1);
		if (nbTry + 1 >= heat) {
			for (int i = 0; i < board.getSIZE()/2; i++) {
				for (int j = 0; j < board.getSIZE()/2; j++) {
					if(this.g2Possible[i][j]!= null){
						this.g2Possible[i][j].clear();
					}
				}
			}
			return this.previous.revert(pieces, board);
		}
		this.isReverted = true;
		return this;
	}

	private void resetPiece(OrientedPiece[] pieces, EternityBoard board, int x, int y) {
		
		OrientedPiece piece = (OrientedPiece) board.getPieceAt(x, y);
		if (piece == null) {
			throw new RuntimeException("Piece to rever not here !");
		}
		if (!piece.isLock()) {
			board.setPieceAt(null, x, y);
			// La pièce doit être dans bon orientation et dans le bonne ordre
			// dans la liste
			piece.resetOrientation();
			pieces[piece.getId()] = piece;
		}
	}

	public HistoG2Placing setNextHisto(int targetX, int targetY, int currentHeat, int nbTry) {
		if (next != null) {
			next.update(targetX, targetY, currentHeat, nbTry);
		} else {
			setNew(true);
			next = new HistoG2Placing(targetX, targetY, currentHeat, nbTry, this);
		}
		return (HistoG2Placing) next;
	}

	public List<G2> getG2List() {
		return g2Possible[x][y];
	}

	public MetaHashArrayList[][] getG2Possible() {
		return g2Possible;
	}
	
	public List<G2> getG2PossibleAt(int x, int y){
		return g2Possible[x][y];
	}

	public void setG2Possible(MetaHashArrayList[][] g2Possible) {
		this.g2Possible = g2Possible;
	}

	public void setG2PossibleAt(int x, int y, MetaHashArrayList result) {
		this.g2Possible[x][y] =  result;
	}

	public void setNextHisto(HistoG2Placing buildingHisto) {
		this.next = buildingHisto;
	}

	public void setPrevious(HistoG2Placing currentHisto) {
		if(currentHisto!=null){
			this.setDeep(currentHisto.getDeep()+1);
		}else{
			this.setDeep(0);
		}
			this.previous =currentHisto;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isNextHisto(){
		return this.next != null;
	}
}
