package org.k.eternity.meta;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.alcibiade.eternity.editor.model.GridModel;
import org.k.eternity.EternityBoard;
import org.k.eternity.Orientation;
import org.k.eternity.OrientedPiece;

public class G2 {

	static String filePath = "./pieces.csv";

	// Gestion des index pour G1
	public static int[][][][][] indexG1Id;
	public static Orientation[][][][][] indexG1Orientation;
	// Gestion des index pour G2
	public static MetaHashArrayList[][][][] indexG2TopLeft = new MetaHashArrayList[24][24][24][24];
	public static MetaHashArrayList[][][][] indexG2BotRight = new MetaHashArrayList[24][24][24][24];

	public static MetaHashArrayList[][][][] indexG2LeftBot = new MetaHashArrayList[24][24][24][24];
	public static MetaHashArrayList[][][][] indexG2RightTop = new MetaHashArrayList[24][24][24][24];

	public static MetaHashArrayList[][][][] indexG2TopBot = new MetaHashArrayList[24][24][24][24];
	public static MetaHashArrayList[][][][] indexG2LeftRight = new MetaHashArrayList[24][24][24][24];

	// Valeur d'auto-incrémentation pour les ID de MetaHash
	private static int AUTO_INCREMENT_ID = 1;

	public int id = 0;
	public int topLeftID, topRightId, bottomLeftId, bottomRightId;
	public Orientation topLeftOrientation, topRightOrientation, bottomLeftOrientation, bottomRightOrientation;

	public G2() {
		this.id = AUTO_INCREMENT_ID;
		AUTO_INCREMENT_ID++;
	}
	public G2(int idTopRight, int idTopLeft, int idBottomLeft, int idBottomRight, Orientation topRightOrientation, Orientation topleftOrientation, Orientation bottomLeftOrientation, Orientation bottomRightOrientation) {
		this();
		this.topRightId = idTopRight;
		this.topRightOrientation = topRightOrientation;

		this.topLeftID = idTopLeft;
		this.topLeftOrientation = topleftOrientation;

		this.bottomLeftId = idBottomLeft;
		this.bottomLeftOrientation = bottomLeftOrientation;

		this.bottomRightId = idBottomRight;
		this.bottomRightOrientation = bottomRightOrientation;
	}

	public boolean idIn(int id) {
		return id == this.topLeftID || id == this.topRightId || id == this.bottomLeftId || id == this.bottomRightId;
	}

	public boolean isCompatible(G2 other) {
		if (this.topLeftID == other.topLeftID || this.topLeftID == other.topRightId || this.topLeftID == other.bottomLeftId || this.topLeftID == other.bottomRightId || this.topRightId == other.topLeftID || this.topRightId == other.topRightId || this.topRightId == other.bottomLeftId
				|| this.topRightId == other.bottomRightId || this.bottomLeftId == other.topLeftID || this.bottomLeftId == other.topRightId || this.bottomLeftId == other.bottomLeftId || this.bottomLeftId == other.bottomRightId || this.bottomRightId == other.topLeftID
				|| this.bottomRightId == other.topRightId || this.bottomRightId == other.bottomLeftId || this.bottomRightId == other.bottomRightId) {
			return false;
		}
		return true;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof G2)){
			return false;
		}
		G2 g2 = (G2)obj;
		if(this.topLeftID == g2.topLeftID
				&& this.topRightId == g2.topRightId
				&& this.bottomLeftId == g2.bottomLeftId
				&& this.bottomRightId == g2.bottomRightId
				&& this.topLeftOrientation == g2.topLeftOrientation
				&& this.topRightOrientation == g2.topRightOrientation
				&& this.bottomLeftOrientation == g2.bottomLeftOrientation
				&& this.bottomRightOrientation == g2.bottomRightOrientation){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(topLeftID+"\t"+topRightId);
		sb.append("\n"+bottomLeftId+"\t"+bottomRightId);
		// TODO Auto-generated method stub
		return sb.toString();
	}
}
