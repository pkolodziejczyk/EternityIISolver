package org.k.eternity;

import java.io.FileWriter;
/**
 * 
 */
import java.io.IOException;



/**
 * @author EXT-PKO
 * 
 */
public class HistoPlacing {

	protected int x;
	protected int y;
	protected int heat;
	protected int nbTry;
	protected HistoPlacing previous;
	protected boolean isReverted = false;
	protected HistoPlacing next;
	private int deep =0;
	
	public HistoPlacing(){
	}
	public void setX(int x){
		this.x=x;
	}
	public void setY(int y){
		this.y=y;
	}
	public void setNbTry(int nbTry){
		this.nbTry=nbTry;
	}
	public void setHeat(int heat){
		this.heat=heat;
	}
	
	/**
	 * 
	 */
	public HistoPlacing(int x, int y, int heat, int nbTry, HistoPlacing previous) {
		this.x = x;
		this.y = y;
		this.heat = heat;
		this.nbTry = nbTry;
		this.previous = previous;
		
		if(previous!= null){
			previous.next = this;
			this.deep = previous.getDeep()+1;
		}
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getHeat() {
		return heat;
	}

	public int getNbTry() {
		return nbTry;
	}

	public HistoPlacing getPrevious() {
		return previous;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
			StringBuilder sb = new StringBuilder();
			if (this.previous != null) {
				sb.append(this.previous.toString());
			}
			sb.append(x);
			sb.append(":");
			sb.append(y);
			sb.append("=>");
			sb.append(nbTry);
			sb.append("/");
			sb.append(heat);
			sb.append(";");
		return sb.toString();
	}

	public boolean isReverted() {
		return isReverted;
	}

	public HistoPlacing revert(OrientedPiece[] pieces, EternityBoard board) {
		OrientedPiece piece = (OrientedPiece) board.getPieceAt(x, y);
		if (piece == null) {
			throw new RuntimeException("Piece to rever not here !");
		}
		this.isReverted = true;
		board.setPieceAt(null, x, y);
		// La pièce doit être dans bon orientation et dans le bonne ordre dans la liste
		piece.resetOrientation();
		pieces[piece.getId()] = piece;
		if (nbTry >= heat) {
			return this.previous.revert(pieces, board);
		}
		return this;
	}

	public HistoPlacing setNextHisto(int targetX, int targetY, int currentHeat, int nbTry) {
		if(next != null){
			next.update(targetX, targetY, currentHeat, nbTry);
		}else{
			next = new HistoPlacing(targetX, targetY, currentHeat, nbTry, this); 
			System.out.println("Nouveau Record !");
			System.out.println(next);
			try {
				FileWriter writer = new FileWriter("histoRecord.txt", true);			
				writer.write(next.toString() + "\n");
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return next;
	}

	public void update(int targetX, int targetY, int currentHeat, int nbTry) {
		this.x = targetX;
		this.y = targetY;
		this.heat = currentHeat;
		this.nbTry = nbTry;
		isReverted = false;
	}

	public void tickTry() {
		nbTry++;
		isReverted = false;
	}

	public int getDeep() {
		return deep;
	}

	public void setDeep(int deep) {
		this.deep = deep;
	}
}
