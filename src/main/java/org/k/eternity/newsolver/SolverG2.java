package org.k.eternity.newsolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

import org.alcibiade.eternity.editor.model.GridModel;
import org.alcibiade.eternity.editor.model.Pattern;
import org.alcibiade.eternity.editor.model.QuadModel;
import org.alcibiade.eternity.editor.solver.ClusterListener;
import org.alcibiade.eternity.editor.solver.ClusterManager;
import org.alcibiade.eternity.editor.solver.EternitySolver;
import org.k.eternity.EternityBoard;
import org.k.eternity.Orientation;
import org.k.eternity.OrientedPiece;
import org.k.eternity.meta.G2;
import org.k.eternity.meta.HistoG2Placing;
import org.k.eternity.meta.MetaHashArrayList;

public class SolverG2 extends EternitySolver implements ClusterListener {
	private static final int PATTERN_NUMBER = 40;

	private long iterations;

	private int[][][][][] indexG1Id;
	private Orientation[][][][][] indexG1Orientation;

	// Gestion des index pour G2
	public MetaHashArrayList[][][][] indexG2TopLeft = new MetaHashArrayList[24][24][24][24];
	public MetaHashArrayList[][][][] indexG2BotRight = new MetaHashArrayList[24][24][24][24];

	public MetaHashArrayList[][][][] indexG2LeftBot = new MetaHashArrayList[24][24][24][24];
	public MetaHashArrayList[][][][] indexG2RightTop = new MetaHashArrayList[24][24][24][24];

	public MetaHashArrayList[][][][] indexG2TopBot = new MetaHashArrayList[24][24][24][24];
	public MetaHashArrayList[][][][] indexG2LeftRight = new MetaHashArrayList[24][24][24][24];

	private static final G2[] EMPTY_G2_ARRAY = new G2[0];

	protected GridModel solutionGrid;
	public int FREE = 23;
	protected GridModel originGrid;
	private EternityBoard board;
	private HistoG2Placing currentHisto;
	private HistoG2Placing buildingHisto;
	private List<G2> toRemoveList = new ArrayList<G2>();
	private boolean isSolve = false;
	private int maxDeep = 0;
	private int internalID[];
	private int externalID[];
	private int maxAllocatedInternalId;
	private OrientedPiece[] internalBorderPiece;
	// Compute the numb

	private OrientedPiece[] pieces;

	public SolverG2(GridModel grid, GridModel solutionGrid, ClusterManager clusterManager) {
		super(clusterManager);
		this.solutionGrid = solutionGrid;

		originGrid = grid.clone();
		solutionGrid.reset();
		solutionGrid.setSize(grid.getSize());
		isSolve = false;
		board = new EternityBoard(originGrid.getSize());
		clusterManager.logMessage("Analyzing pieces...");
		loadPieces();
		clusterManager.logMessage("Initializing Hash G1...");
		initHashG1();
		clusterManager.logMessage("Initializing Hash G1...");
		initIndexG2(pieces);
		placeLockedPiece();

	}

	private void placeLockedPiece() {
		for (int row = 0; row < originGrid.getSize(); row++) {
			for (int column = 0; column < originGrid.getSize(); column++) {
				QuadModel quad = originGrid.getQuad(row, column);
				if (quad.isLocked()) {
					board.setPieceAt(pieces[quad.getId()], row, column);
					pieces[quad.getId()].setLock(true);
					pieces[quad.getId()] = null;
				}
			}
		}
	}

	private void loadPieces() {
		int id = 1;
		internalID = IntStream.iterate(0, i -> -1).limit(PATTERN_NUMBER).boxed().mapToInt(e -> e).toArray();
		externalID = IntStream.iterate(0, i -> -1).limit(PATTERN_NUMBER).boxed().mapToInt(e -> e).toArray();
		maxAllocatedInternalId = 1;
		pieces = new OrientedPiece[originGrid.getSize() * originGrid.getSize() + 1];
		internalBorderPiece = new OrientedPiece[originGrid.getSize() * originGrid.getSize() + 1];
		for (int row = 0; row < originGrid.getSize(); row++) {
			for (int column = 0; column < originGrid.getSize(); column++) {
				QuadModel quad = originGrid.getQuad(row, column);
				if (quad.getId() != 0) {
				 id = quad.getId();
				}
				OrientedPiece piece = new OrientedPiece(id, 
						mapToInternalID(quad.getPattern(QuadModel.DIR_NORTH).getCode()),
						mapToInternalID(quad.getPattern(QuadModel.DIR_EAST).getCode()),
						mapToInternalID(quad.getPattern(QuadModel.DIR_SOUTH).getCode()),
						mapToInternalID(quad.getPattern(QuadModel.DIR_WEST).getCode()));
				pieces[piece.getId()] = piece;
				if (piece.getTop() == 0 || piece.getRight() == 0 || piece.getBot() == 0 || piece.getLeft() == 0) {
					internalBorderPiece[piece.getId()] = piece;
				}
				id++;
			}
		}
	}

	private int mapToInternalID(int code) {
		if (code == 0) {
			return 0;
		}
		if (internalID[code] == -1) {
			internalID[code] = maxAllocatedInternalId++;
			externalID[internalID[code]] = code;
		}
		return internalID[code];
	}

	private int mapToExternalID(int code) {
		if (code == 0) {
			return 0;
		}
		return externalID[code];
	}

	public void initHashG1() {
		FREE = maxAllocatedInternalId++;

		indexG1Id = new int[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][0];
		indexG1Orientation = new Orientation[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][0];
		for (OrientedPiece piece : pieces) {
			if (piece == null) {
				continue;
			}
			int top, left, bottom, right, id;
			// Sens anti-horaire !!! (top,left, bot,right)
			id = piece.getId();
			top = piece.getTop();
			left = piece.getLeft();
			bottom = piece.getBot();
			right = piece.getRight();
			addPieceToSolutions(top, left, bottom, right, id);
			// 1 Free Border
			addPieceToSolutions(FREE, left, bottom, right, id);
			addPieceToSolutions(top, FREE, bottom, right, id);
			addPieceToSolutions(top, left, FREE, right, id);
			addPieceToSolutions(top, left, bottom, FREE, id);
			// 2 Free Border
			addPieceToSolutions(FREE, FREE, bottom, right, id);
			addPieceToSolutions(FREE, left, FREE, right, id);
			addPieceToSolutions(FREE, left, bottom, FREE, id);
			addPieceToSolutions(top, FREE, FREE, right, id);
			addPieceToSolutions(top, FREE, bottom, FREE, id);
			addPieceToSolutions(top, left, FREE, FREE, id);
			// 3 Free Border
			addPieceToSolutions(FREE, FREE, FREE, right, id);
			addPieceToSolutions(top, FREE, FREE, FREE, id);
			addPieceToSolutions(FREE, left, FREE, FREE, id);
			addPieceToSolutions(FREE, FREE, bottom, FREE, id);
		}
	}

	public void initIndexG2(OrientedPiece[] pieces) {
		List<G2> listG2 = new ArrayList<G2>();
		// On fait la réservation mémoire seulement ici, car si on ne fait pas
		// l'initilisation pas besoin de polué la mémoire.
		indexG2TopLeft = new MetaHashArrayList[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId];
		indexG2BotRight = new MetaHashArrayList[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId];
		indexG2LeftBot = new MetaHashArrayList[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId];
		indexG2RightTop = new MetaHashArrayList[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId];
		indexG2TopBot = new MetaHashArrayList[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId];
		indexG2LeftRight = new MetaHashArrayList[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId];
		for (int idTopLeft = 1; idTopLeft < pieces.length; idTopLeft++) {
			OrientedPiece topleftPiece = pieces[idTopLeft];
			if (topleftPiece == null) {
				continue;
			}
			topleftPiece.setOrientation(Orientation.TOP);
			// Top / Left / Bottom / Right / Solution number [0..n]
			forOnePieceTopLeft(pieces, listG2, topleftPiece);
			topleftPiece.setOrientation(Orientation.LEFT);
			// Top / Left / Bottom / Right / Solution number [0..n]
			forOnePieceTopLeft(pieces, listG2, topleftPiece);
			topleftPiece.setOrientation(Orientation.BOT);
			forOnePieceTopLeft(pieces, listG2, topleftPiece);
			topleftPiece.setOrientation(Orientation.RIGHT);
			forOnePieceTopLeft(pieces, listG2, topleftPiece);
			clusterManager.logMessage("Indexing : " + idTopLeft + " / " + (pieces.length - 1));
		}
		
		
	}

	private void forOnePieceTopLeft(OrientedPiece[] pieces, List<G2> listG2, OrientedPiece topleftPiece) {
		int idTopLeft = topleftPiece.getId();
		for (int i = 0; i < indexG1Id[FREE][topleftPiece.getRight()][FREE][FREE].length; i++) {
			int idTopRight = indexG1Id[FREE][topleftPiece.getRight()][FREE][FREE][i];
			if (idTopLeft != idTopRight) {
				OrientedPiece topRightPiece = pieces[idTopRight];
				topRightPiece.setOrientation(indexG1Orientation[FREE][topleftPiece.getRight()][FREE][FREE][i]);
				for (int j = 0; j < indexG1Id[topleftPiece.getBot()][FREE][FREE][FREE].length; j++) {
					int idBottomLeft = indexG1Id[topleftPiece.getBot()][FREE][FREE][FREE][j];
					if (idTopLeft != idBottomLeft && idTopRight != idBottomLeft) {
						OrientedPiece bottomLeftPiece = pieces[idBottomLeft];
						bottomLeftPiece.setOrientation(indexG1Orientation[topleftPiece.getBot()][FREE][FREE][FREE][j]);
						for (int k = 0; k < indexG1Id[topRightPiece.getBot()][bottomLeftPiece.getRight()][FREE][FREE].length; k++) {
							int idBottomRight = indexG1Id[topRightPiece.getBot()][bottomLeftPiece.getRight()][FREE][FREE][k];
							if (idTopLeft != idBottomRight && idTopRight != idBottomRight && idBottomLeft != idBottomRight) {
								OrientedPiece bottomRightPiece = pieces[idBottomRight];

								bottomRightPiece.setOrientation(indexG1Orientation[topRightPiece.getBot()][bottomLeftPiece.getRight()][FREE][FREE][k]);
								if (topleftPiece.getBot() != 0 && topleftPiece.getRight() != 0 && topRightPiece.getBot() != 0 && topRightPiece.getLeft() != 0 && bottomLeftPiece.getTop() != 0 && bottomLeftPiece.getRight() != 0 && bottomRightPiece.getLeft() != 0 && bottomRightPiece.getTop() != 0 && topleftPiece.getTop() + bottomLeftPiece.getBot() != 0 && topRightPiece.getTop() + bottomRightPiece.getBot() != 0 && topleftPiece.getLeft() + topRightPiece.getRight() != 0 && bottomLeftPiece.getLeft() + bottomRightPiece.getRight() != 0) {
									G2 g2 = new G2(idTopRight, idTopLeft, idBottomLeft, idBottomRight, topRightPiece.getOrientation(), topleftPiece.getOrientation(), bottomLeftPiece.getOrientation(), bottomRightPiece.getOrientation());
									listG2.add(g2);
									addMetaHash(indexG2TopLeft, topRightPiece.getTop(), topleftPiece.getTop(), topleftPiece.getLeft(), bottomLeftPiece.getLeft(), g2);
									addMetaHash(indexG2BotRight, bottomLeftPiece.getBot(), bottomRightPiece.getBot(), bottomRightPiece.getRight(), topRightPiece.getRight(), g2);
									addMetaHash(indexG2LeftBot, topleftPiece.getLeft(), bottomLeftPiece.getLeft(), bottomLeftPiece.getBot(), bottomRightPiece.getBot(), g2);
									addMetaHash(indexG2RightTop, bottomRightPiece.getRight(), topRightPiece.getRight(), topRightPiece.getTop(), topleftPiece.getTop(), g2);
									addMetaHash(indexG2TopBot, topRightPiece.getTop(), topleftPiece.getTop(), bottomLeftPiece.getBot(), bottomRightPiece.getBot(), g2);
									addMetaHash(indexG2LeftRight, topleftPiece.getLeft(), bottomLeftPiece.getLeft(), bottomRightPiece.getRight(), topRightPiece.getRight(), g2);
								}
							}
						}
					}
				}
			}
		}
	}

	private void addMetaHash(MetaHashArrayList[][][][] indexG2Part, int index1, int index2, int index3, int index4, G2 g2) {
		addMetaHashCombinaison(indexG2Part, index1, index2, index3, index4, g2);
		// 1 Free Border
		addMetaHashCombinaison(indexG2Part, FREE, index2, index3, index4, g2);
		addMetaHashCombinaison(indexG2Part, index1, FREE, index3, index4, g2);
		addMetaHashCombinaison(indexG2Part, index1, index2, FREE, index4, g2);
		addMetaHashCombinaison(indexG2Part, index1, index2, index3, FREE, g2);
		// 2 Free Border
		// addMetaHashCombinaison(indexG2Part, FREE, FREE, index3, index4, g2);
		// addMetaHashCombinaison(indexG2Part, FREE, index2, FREE, index4, g2);
		// addMetaHashCombinaison(indexG2Part, FREE, index2, index3, FREE, g2);
		// addMetaHashCombinaison(indexG2Part, index1, FREE, FREE, index4, g2);
		// addMetaHashCombinaison(indexG2Part, index1, FREE, index3, FREE, g2);
		// addMetaHashCombinaison(indexG2Part, index1, index2, FREE, FREE, g2);
		// // 3 Free Border
		// addMetaHashCombinaison(indexG2Part, FREE, FREE, FREE, index4, g2);
		// addMetaHashCombinaison(indexG2Part, index1, FREE, FREE, FREE, g2);
		// addMetaHashCombinaison(indexG2Part, FREE, index2, FREE, FREE, g2);
		// addMetaHashCombinaison(indexG2Part, FREE, FREE, index3, FREE, g2);
	}

	private void addMetaHashCombinaison(MetaHashArrayList[][][][] indexG2Part, int index1, int index2, int index3, int index4, G2 g2) {
		MetaHashArrayList solutions = indexG2Part[index1][index2][index3][index4];
		if (solutions == null) {
			solutions = new MetaHashArrayList();
			indexG2Part[index1][index2][index3][index4] = solutions;
		}
		// for(G2 present : solutions){
		// if(g2.equals(present)){
		// return;
		// }
		// }
		solutions.add(g2);
	}

	private void addPieceToSolutions(int top, int left, int bottom, int right, int id) {
		addMatchTosolutions(top, left, bottom, right, id, Orientation.TOP);
		addMatchTosolutions(left, bottom, right, top, id, Orientation.LEFT);
		addMatchTosolutions(bottom, right, top, left, id, Orientation.BOT);
		addMatchTosolutions(right, top, left, bottom, id, Orientation.RIGHT);
	}

	private void addMatchTosolutions(int top, int left, int bottom, int right, int id, Orientation orientation) {
		addIdToSolutions(top, left, bottom, right, id);
		addOrientationToSolutions(top, left, bottom, right, orientation);
	}

	private void addOrientationToSolutions(int top, int left, int bottom, int right, Orientation orientation) {
		{
			Orientation[] solutions = indexG1Orientation[top][left][bottom][right];
			Orientation[] retour = Arrays.copyOf(solutions, solutions.length + 1);
			retour[solutions.length] = orientation;
			indexG1Orientation[top][left][bottom][right] = retour;
		}
	}

	private void addIdToSolutions(int top, int left, int bottom, int right, int id) {
		{
			int[] solutions = indexG1Id[top][left][bottom][right];
			int[] retour = Arrays.copyOf(solutions, solutions.length + 1);
			retour[solutions.length] = id;
			indexG1Id[top][left][bottom][right] = retour;
		}
	}

	@Override
	public void run() {
		notifyStart();
		clusterManager.showStartMessage();

		boolean solved = solve();

		if (solved) {
			updatedGridWithBoard(board, solutionGrid);
			clusterManager.submitSolution(solutionGrid);
			clusterManager.showStats(iterations);
		}

		notifyEnd(solved);
	}

	boolean displaystats = false;

	private boolean solve() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				displaystats = true;
			}

		}, 60_000, 60_000);
		while (!isSolve) {
			iterations++;
			if (placePieceByHeatmap(pieces, getBoard())) {
				iterations++;
				if (currentHisto.getDeep() > maxDeep) {
					maxDeep = currentHisto.getDeep();
					updatedGridWithBoard(board, solutionGrid);
					clusterManager.submitSolution(solutionGrid);
				}
				currentHisto = (HistoG2Placing) currentHisto.revert(pieces, getBoard());
			}
			if (displaystats) {
				displaystats = false;
				clusterManager.showStats(iterations);
			}
			if(slowmotion) {
				updatedGridWithBoard(board, solutionGrid);
				clusterManager.submitSolution(solutionGrid);
			}
			if (interrupted) {
				timer.cancel();
				return isSolve;
			}
		}
		timer.cancel();
		return isSolve;
	}

	public boolean placePieceByHeatmap(OrientedPiece[] pieces, EternityBoard board) {
		int targetX = -1;
		int targetY = -1;
		int currentHeat = Integer.MAX_VALUE;
		if (currentHisto == null || !currentHisto.isReverted()) {
			buildingHisto = new HistoG2Placing();
			buildingHisto.setPrevious(currentHisto);
			for (int x = 0; x < board.getSIZE() / 2; x++) {
				for (int y = 0; y < board.getSIZE() / 2; y++) {

					int heat = countMatchAt(x, y, board, pieces);
					if (heat == 0) {
						return true;
					}
					if (heat < currentHeat && heat > 0) {
						currentHeat = heat;
						targetX = x;
						targetY = y;
					}
				}
			}
			if (currentHeat == Integer.MAX_VALUE) {
				isSolve = true;
				return false;
			}
			if (currentHisto != null) {
				if (currentHisto.isNextHisto()) {
					buildingHisto.setNew(false);
				}
				currentHisto.setNextHisto(buildingHisto);

				currentHisto = (HistoG2Placing) currentHisto.setNextHisto(targetX, targetY, currentHeat, 0);
			} else {
				currentHisto = buildingHisto;
				buildingHisto.setX(targetX);
				buildingHisto.setY(targetY);
				buildingHisto.setHeat(currentHeat);
				buildingHisto.setNbTry(1);
			}
			return placePiece(currentHisto.getX(), currentHisto.getY(), board, pieces, 0, currentHisto.getG2List());
		} else {
			currentHisto.tickTry();
			// On place une pièce :
			return placePiece(currentHisto.getX(), currentHisto.getY(), board, pieces, currentHisto.getNbTry(), currentHisto.getG2List());
		}
	}

	private boolean placePiece(int x, int y, EternityBoard board, OrientedPiece[] pieces, int occurence, List<G2> g2List) {
		G2 toPlace = g2List.get(occurence);
		if (board.getPieceAt(x * 2 + 1, y * 2) == null) {
			OrientedPiece topRight = pieces[toPlace.topRightId];
			topRight.setOrientation(toPlace.topRightOrientation);
			board.setPieceAt(topRight, x * 2 + 1, y * 2);
			pieces[toPlace.topRightId] = null;
		}
		if (board.getPieceAt(x * 2, y * 2) == null) {
			OrientedPiece topLeft = pieces[toPlace.topLeftID];
			topLeft.setOrientation(toPlace.topLeftOrientation);
			board.setPieceAt(topLeft, x * 2, y * 2);
			pieces[toPlace.topLeftID] = null;
		}
		if (board.getPieceAt(x * 2, y * 2 + 1) == null) {
			OrientedPiece bottomLeft = pieces[toPlace.bottomLeftId];
			bottomLeft.setOrientation(toPlace.bottomLeftOrientation);
			board.setPieceAt(bottomLeft, x * 2, y * 2 + 1);
			pieces[toPlace.bottomLeftId] = null;
		}
		if (board.getPieceAt(x * 2 + 1, y * 2 + 1) == null) {
			OrientedPiece bottomRight = pieces[toPlace.bottomRightId];
			bottomRight.setOrientation(toPlace.bottomRightOrientation);

			board.setPieceAt(bottomRight, x * 2 + 1, y * 2 + 1);
			pieces[toPlace.bottomRightId] = null;
		}
		// Si on est au centre et qu'on a un pièce du bord, on a une erreur;
		if (!(x == 0 || x == this.getBoard().getSIZE() / 2 - 1 || y == 0 || y == this.getBoard().getSIZE() / 2 - 1)) {
			if (internalBorderPiece[toPlace.topRightId] != null || internalBorderPiece[toPlace.topLeftID] != null || internalBorderPiece[toPlace.bottomLeftId] != null || internalBorderPiece[toPlace.bottomRightId] != null) {
				return true;
			}
		}
		return false;

	}

	private OrientedPiece[] posedPieces = new OrientedPiece[4];

	private int countMatchAt(int x, int y, EternityBoard board, OrientedPiece[] pieces) {
		updatePosedPieces(x, y, board);
		// Un G2 a été posé à cet endroit !
		if (posedPieces[POSED_INDEX_TOP_RIGHT] != null && posedPieces[POSED_INDEX_TOP_LEFT] != null && posedPieces[POSED_INDEX_BOT_LEFT] != null && posedPieces[POSED_INDEX_BOT_RIGHT] != null) {
			return -1;
		}

		int constTopRight = board.getPieceAt(x * 2 + 1, y * 2 - 1) != null ? board.getPieceAt(x * 2 + 1, y * 2 - 1).getBot() : FREE;
		int constTopLeft = board.getPieceAt(x * 2, y * 2 - 1) != null ? board.getPieceAt(x * 2, y * 2 - 1).getBot() : FREE;
		int constLeftTop = board.getPieceAt(x * 2 - 1, y * 2) != null ? board.getPieceAt(x * 2 - 1, y * 2).getRight() : FREE;
		int constLeftBot = board.getPieceAt(x * 2 - 1, y * 2 + 1) != null ? board.getPieceAt(x * 2 - 1, y * 2 + 1).getRight() : FREE;
		int constBotLeft = board.getPieceAt(x * 2, y * 2 + 2) != null ? board.getPieceAt(x * 2, y * 2 + 2).getTop() : FREE;
		int constBotRight = board.getPieceAt(x * 2 + 1, y * 2 + 2) != null ? board.getPieceAt(x * 2 + 1, y * 2 + 2).getTop() : FREE;
		int constRightBot = board.getPieceAt(x * 2 + 2, y * 2 + 1) != null ? board.getPieceAt(x * 2 + 2, y * 2 + 1).getLeft() : FREE;
		int constRightTop = board.getPieceAt(x * 2 + 2, y * 2) != null ? board.getPieceAt(x * 2 + 2, y * 2).getLeft() : FREE;
		MetaHashArrayList result = null;
		if (buildingHisto != null && buildingHisto.getPrevious() != null && ((HistoG2Placing) buildingHisto.getPrevious()).getG2PossibleAt(x, y) != null && ((HistoG2Placing) buildingHisto.getPrevious()).getG2PossibleAt(x, y).size() != 0
		// On recalcul quand les variations on changé !
				&& (buildingHisto.getPrevious().getX() + 1 < x || buildingHisto.getPrevious().getX() - 1 > x || buildingHisto.getPrevious().getY() + 1 < y || buildingHisto.getPrevious().getY() - 1 > y)) {
			result = updateResult(x, y, pieces);
		} else {
			result = countG2For(pieces, posedPieces, constTopRight, constTopLeft, constLeftTop, constLeftBot, constBotLeft, constBotRight, constRightBot, constRightTop);
			if (result == null) {
				return Integer.MAX_VALUE; // Déjà posé ou trop de réponse !
			}
		}
		if (buildingHisto != null) {
			buildingHisto.setG2PossibleAt(x, y, result);
		}
		return result.size();
	}

	public MetaHashArrayList countG2For(OrientedPiece[] freePieces, OrientedPiece[] posedPieces, int topRight, int topLeft, int leftTop, int leftBot, int botLeft, int botRight, int rightBot, int rightTop) {
		if (topRight == FREE && topLeft == FREE && leftTop == FREE && leftBot == FREE && botLeft == FREE && botRight == FREE && rightBot == FREE && rightTop == FREE) {
			// TODO ceci est un bouchon, il faut prévoir une version plus
			// élégante pour une version propre.
			return null;
		}
		try {
			MetaHashArrayList toReturn = new MetaHashArrayList(countG2For(topRight, topLeft, leftTop, leftBot, botLeft, botRight, rightBot, rightTop));
			if (toReturn == null) {
				return (MetaHashArrayList) Collections.EMPTY_LIST;
			}
			toRemoveList.clear();
			for (G2 g2 : toReturn) {
				if (dontMatchPlacedPieces(posedPieces, g2)) {
					toRemoveList.add(g2);
				} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.topRightId)) {
					toRemoveList.add(g2);
				} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.topLeftID)) {
					toRemoveList.add(g2);
				} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.bottomLeftId)) {
					toRemoveList.add(g2);
				} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.bottomRightId)) {
					toRemoveList.add(g2);
				}
			}
			toReturn.removeAll(toRemoveList);
			return toReturn;
		} catch (Exception e) {
			// Pas de contrait exception !
			return null;
		}
	}

	private List<G2> mergeMetaHash(MetaHashArrayList a, MetaHashArrayList b) {
		if (a == null || b == null) {
			return null;
		}
		return mergeMetaHash(a.toArray(EMPTY_G2_ARRAY), b.toArray(EMPTY_G2_ARRAY));
	}

	private List<G2> mergedG2 = new ArrayList<G2>();

	public List<G2> mergeMetaHash(G2[] a, G2[] b) {
		int i = 0, j = 0;
		int sizeA = a.length;
		int sizeB = b.length;
		mergedG2.clear();
		while (i < sizeA && j < sizeB) {
			if (a[i].id < b[j].id) {
				i++;
			} else if (b[j].id < a[i].id) {
				j++;
			} else {
				mergedG2.add(a[i]);
				i++;
				j++;
			}
		}
		return mergedG2;
	}

	public List<G2> countG2For(int topRight, int topLeft, int leftTop, int leftBot, int botLeft, int botRight, int rightBot, int rightTop) {
		if (topRight != FREE && topLeft != FREE && leftTop != FREE && leftBot != FREE) {
			if (botLeft == FREE && botRight == FREE && rightBot == FREE && rightTop == FREE) {
				return indexG2TopLeft[topRight][topLeft][leftTop][leftBot];
			}
			if (botLeft == FREE && botRight == FREE) {
				return mergeMetaHash(indexG2TopLeft[topRight][topLeft][leftTop][leftBot], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
			}
			if (rightBot == FREE && rightTop == FREE) {
				return mergeMetaHash(indexG2TopLeft[topRight][topLeft][leftTop][leftBot], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
			}
			return mergeMetaHash(indexG2TopLeft[topRight][topLeft][leftTop][leftBot], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
		}
		if (botLeft != FREE && botRight != FREE && rightBot != FREE && rightTop != FREE) {
			if (topRight == FREE && topLeft == FREE && leftTop == FREE && leftBot == FREE) {
				return indexG2BotRight[botLeft][botRight][rightBot][rightTop];
			}
			if (topRight == FREE && topLeft == FREE) {
				return mergeMetaHash(indexG2BotRight[botLeft][botRight][rightBot][rightTop], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
			}
			if (leftTop == FREE && leftBot == FREE) {
				return mergeMetaHash(indexG2BotRight[botLeft][botRight][rightBot][rightTop], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
			}
			return mergeMetaHash(indexG2BotRight[botLeft][botRight][rightBot][rightTop], indexG2TopLeft[topRight][topLeft][leftTop][leftBot]);
		}

		if (leftTop != FREE && leftBot != FREE && botLeft != FREE && botRight != FREE) {
			if (rightBot == FREE && rightTop == FREE && topRight == FREE && topLeft == FREE) {
				return indexG2LeftBot[leftTop][leftBot][botLeft][botRight];
			}
			if (rightBot == FREE && rightTop == FREE) {
				return mergeMetaHash(indexG2LeftBot[leftTop][leftBot][botLeft][botRight], indexG2TopBot[topRight][topLeft][botLeft][botRight]);
			}
			if (topRight == FREE && topLeft == FREE) {
				return mergeMetaHash(indexG2LeftBot[leftTop][leftBot][botLeft][botRight], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
			}
			return mergeMetaHash(indexG2LeftBot[leftTop][leftBot][botLeft][botRight], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
		}
		if (rightBot != FREE && rightTop != FREE && topRight != FREE && topLeft != FREE) {
			if (leftTop == FREE && leftBot == FREE && botLeft == FREE && botRight == FREE) {
				return indexG2RightTop[rightBot][rightTop][topRight][topLeft];
			}
			if (leftTop == FREE && leftBot == FREE) {
				return mergeMetaHash(indexG2RightTop[rightBot][rightTop][topRight][topLeft], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
			}
			if (botLeft == FREE && botRight == FREE) {
				return mergeMetaHash(indexG2RightTop[rightBot][rightTop][topRight][topLeft], indexG2LeftRight[leftTop][leftBot][rightBot][rightTop]);
			}
			return mergeMetaHash(indexG2RightTop[rightBot][rightTop][topRight][topLeft], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
		}

		if (topRight != FREE && topLeft != FREE && botLeft != FREE && botRight != FREE) {
			if (leftTop == FREE && leftBot == FREE && rightBot == FREE && rightTop == FREE) {
				return indexG2TopBot[topRight][topLeft][botLeft][botRight];
			}
			if (leftTop == FREE && leftBot == FREE) {
				return mergeMetaHash(indexG2TopBot[topRight][topLeft][botLeft][botRight], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
			}
			if (rightBot == FREE && rightTop == FREE) {
				return mergeMetaHash(indexG2TopBot[topRight][topLeft][botLeft][botRight], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
			}
			return mergeMetaHash(indexG2TopBot[topRight][topLeft][botLeft][botRight], indexG2LeftRight[leftTop][leftBot][rightBot][rightTop]);
		}
		if (leftTop != FREE && leftBot != FREE && rightBot != FREE && rightTop != FREE) {
			if (topRight == FREE && topLeft == FREE && botLeft == FREE && botRight == FREE) {
				return indexG2LeftRight[leftTop][leftBot][rightBot][rightTop];
			}
			if (topRight == FREE && topLeft == FREE) {
				return mergeMetaHash(indexG2LeftRight[leftTop][leftBot][rightBot][rightTop], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
			}
			if (botLeft == FREE && botRight == FREE) {
				return mergeMetaHash(indexG2LeftRight[leftTop][leftBot][rightBot][rightTop], indexG2TopLeft[topRight][topLeft][leftTop][leftBot]);
			}
			return mergeMetaHash(indexG2LeftRight[leftTop][leftBot][rightBot][rightTop], indexG2TopBot[topRight][topLeft][botLeft][botRight]);
		}

		// Pas assez de contrainte va te faire mettre !
		throw new RuntimeException("Aucune contrainte donnée à la méthode !");
	}

	private MetaHashArrayList updateResult(int x, int y, OrientedPiece[] pieces) {
		MetaHashArrayList result;
		if (buildingHisto.getG2PossibleAt(x, y) == null) {
			result = new MetaHashArrayList(((HistoG2Placing) buildingHisto.getPrevious()).getG2PossibleAt(x, y));
		} else {
			result = (MetaHashArrayList) buildingHisto.getG2PossibleAt(x, y);
			result.addAll(((HistoG2Placing) buildingHisto.getPrevious()).getG2PossibleAt(x, y));
		}

		toRemoveList.clear();
		for (G2 g2 : result) {
			if (dontMatchPlacedPieces(posedPieces, g2)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(pieces, posedPieces, g2.topRightId)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(pieces, posedPieces, g2.topLeftID)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(pieces, posedPieces, g2.bottomLeftId)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(pieces, posedPieces, g2.bottomRightId)) {
				toRemoveList.add(g2);
			}
		}
		result.removeAll(toRemoveList);
		return result;
	}

	private static boolean isToRemoveOfG2Possible(OrientedPiece[] freePieces, OrientedPiece[] posedPieces, int idPiece) {
		return freePieces[idPiece] == null && !isIdInPieces(idPiece, posedPieces);
	}

	private static boolean isIdInPieces(int idPiece, OrientedPiece[] posedPieces) {
		for (OrientedPiece piece : posedPieces) {
			if (piece != null && piece.getId() == idPiece) {
				return true;
			}
		}
		return false;
	}

	private boolean dontMatchPlacedPieces(OrientedPiece[] posedPieces2, G2 g2) {
		if (posedPieces[POSED_INDEX_TOP_RIGHT] != null) {
			if (g2.topRightId != posedPieces[POSED_INDEX_TOP_RIGHT].getId() || g2.topRightOrientation != posedPieces[POSED_INDEX_TOP_RIGHT].getOrientation()) {
				return true;
			}
		}
		if (posedPieces[POSED_INDEX_TOP_LEFT] != null) {
			if (g2.topLeftID != posedPieces[POSED_INDEX_TOP_LEFT].getId() || g2.topLeftOrientation != posedPieces[POSED_INDEX_TOP_LEFT].getOrientation()) {
				return true;
			}
		}
		if (posedPieces[POSED_INDEX_BOT_LEFT] != null) {
			if (g2.bottomLeftId != posedPieces[POSED_INDEX_BOT_LEFT].getId() || g2.bottomLeftOrientation != posedPieces[POSED_INDEX_BOT_LEFT].getOrientation()) {
				return true;
			}
		}
		if (posedPieces[POSED_INDEX_BOT_RIGHT] != null) {
			if (g2.bottomRightId != posedPieces[POSED_INDEX_BOT_RIGHT].getId() || g2.bottomRightOrientation != posedPieces[POSED_INDEX_BOT_RIGHT].getOrientation()) {
				return true;
			}
		}
		return false;
	}

	public static final int POSED_INDEX_TOP_RIGHT = 0;
	public static final int POSED_INDEX_TOP_LEFT = 1;

	public static final int POSED_INDEX_BOT_LEFT = 2;
	public static final int POSED_INDEX_BOT_RIGHT = 3;

	public List<G2> getG2(int x, int y, EternityBoard board, OrientedPiece[] pieces, List<G2> toReturn) {
		updatePosedPieces(x, y, board);

		int constTopRight = board.getPieceAt(x * 2 + 1, y * 2 - 1) != null ? board.getPieceAt(x * 2 + 1, y * 2 - 1).getBot() : FREE;
		int constTopLeft = board.getPieceAt(x * 2, y * 2 - 1) != null ? board.getPieceAt(x * 2, y * 2 - 1).getBot() : FREE;
		int constLeftTop = board.getPieceAt(x * 2 - 1, y * 2) != null ? board.getPieceAt(x * 2 - 1, y * 2).getRight() : FREE;
		int constLeftBot = board.getPieceAt(x * 2 - 1, y * 2 + 1) != null ? board.getPieceAt(x * 2 - 1, y * 2 + 1).getRight() : FREE;
		int constBotLeft = board.getPieceAt(x * 2, y * 2 + 2) != null ? board.getPieceAt(x * 2, y * 2 + 2).getTop() : FREE;
		int constBotRight = board.getPieceAt(x * 2 + 1, y * 2 + 2) != null ? board.getPieceAt(x * 2 + 1, y * 2 + 2).getTop() : FREE;
		int constRightBot = board.getPieceAt(x * 2 + 2, y * 2 + 1) != null ? board.getPieceAt(x * 2 + 2, y * 2 + 1).getLeft() : FREE;
		int constRightTop = board.getPieceAt(x * 2 + 2, y * 2) != null ? board.getPieceAt(x * 2 + 2, y * 2).getLeft() : FREE;
		return getG2For(pieces, posedPieces, constTopRight, constTopLeft, constLeftTop, constLeftBot, constBotLeft, constBotRight, constRightBot, constRightTop, toReturn);

	}

	public List<G2> getG2For(OrientedPiece[] freePieces, OrientedPiece[] posedPieces, int topRight, int topLeft, int leftTop, int leftBot, int botLeft, int botRight, int rightBot, int rightTop, List<G2> toReturn) {
		if (topRight == FREE && topLeft == FREE && leftTop == FREE && leftBot == FREE && botLeft == FREE && botRight == FREE && rightBot == FREE && rightTop == FREE) {
			// TODO ceci est un bouchon, il faut prévoir une version plus
			// élégante pour une version propre.
			return null;
		}
		toReturn.clear();
		toRemoveList.clear();
		toReturn.addAll(getG2For(topRight, topLeft, leftTop, leftBot, botLeft, botRight, rightBot, rightTop));
		for (G2 g2 : toReturn) {
			if (dontMatchPlacedPieces(posedPieces, g2)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.topRightId)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.topLeftID)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.bottomLeftId)) {
				toRemoveList.add(g2);
			} else if (isToRemoveOfG2Possible(freePieces, posedPieces, g2.bottomRightId)) {
				toRemoveList.add(g2);
			} else {
			}
		}
		toReturn.removeAll(toRemoveList);
		return toReturn;
	}

	public List<G2> getG2For(int topRight, int topLeft, int leftTop, int leftBot, int botLeft, int botRight, int rightBot, int rightTop) {
		if (topRight == FREE && topLeft == FREE && leftTop == FREE && leftBot == FREE && botLeft == FREE && botRight == FREE && rightBot == FREE && rightTop == FREE) {
			throw new RuntimeException("Aucune contrainte donnée à la méthode !");
		}

		if (topRight != FREE && topLeft != FREE && leftTop != FREE && leftBot != FREE) {
			if (botLeft == FREE && botRight == FREE && rightBot == FREE && rightTop == FREE) {
				return indexG2TopLeft[topRight][topLeft][leftTop][leftBot];
			}
			if (botLeft == FREE && botRight == FREE) {
				return mergeMetaHash(indexG2TopLeft[topRight][topLeft][leftTop][leftBot], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
			}
			if (rightBot == FREE && rightTop == FREE) {
				return mergeMetaHash(indexG2TopLeft[topRight][topLeft][leftTop][leftBot], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
			}
			return mergeMetaHash(indexG2TopLeft[topRight][topLeft][leftTop][leftBot], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
		}
		if (botLeft != FREE && botRight != FREE && rightBot != FREE && rightTop != FREE) {
			if (topRight == FREE && topLeft == FREE && leftTop == FREE && leftBot == FREE) {
				return indexG2BotRight[botLeft][botRight][rightBot][rightTop];
			}
			if (topRight == FREE && topLeft == FREE) {
				return mergeMetaHash(indexG2BotRight[botLeft][botRight][rightBot][rightTop], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
			}
			if (leftTop == FREE && leftBot == FREE) {
				return mergeMetaHash(indexG2BotRight[botLeft][botRight][rightBot][rightTop], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
			}
			return mergeMetaHash(indexG2BotRight[botLeft][botRight][rightBot][rightTop], indexG2TopLeft[topRight][topLeft][leftTop][leftBot]);
		}

		if (leftTop != FREE && leftBot != FREE && botLeft != FREE && botRight != FREE) {
			if (rightBot == FREE && rightTop == FREE && topRight == FREE && topLeft == FREE) {
				return indexG2LeftBot[leftTop][leftBot][botLeft][botRight];
			}
			if (rightBot == FREE && rightTop == FREE) {
				return mergeMetaHash(indexG2LeftBot[leftTop][leftBot][botLeft][botRight], indexG2TopBot[topRight][topLeft][botLeft][botRight]);
			}
			if (topRight == FREE && topLeft == FREE) {
				return mergeMetaHash(indexG2LeftBot[leftTop][leftBot][botLeft][botRight], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
			}
			return mergeMetaHash(indexG2LeftBot[leftTop][leftBot][botLeft][botRight], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
		}
		if (rightBot != FREE && rightTop != FREE && topRight != FREE && topLeft != FREE) {
			if (leftTop == FREE && leftBot == FREE && botLeft == FREE && botRight == FREE) {
				return indexG2RightTop[rightBot][rightTop][topRight][topLeft];
			}
			if (leftTop == FREE && leftBot == FREE) {
				return mergeMetaHash(indexG2RightTop[rightBot][rightTop][topRight][topLeft], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
			}
			if (botLeft == FREE && botRight == FREE) {
				return mergeMetaHash(indexG2RightTop[rightBot][rightTop][topRight][topLeft], indexG2LeftRight[leftTop][leftBot][rightBot][rightTop]);
			}
			return mergeMetaHash(indexG2RightTop[rightBot][rightTop][topRight][topLeft], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
		}

		if (topRight != FREE && topLeft != FREE && botLeft != FREE && botRight != FREE) {
			if (leftTop == FREE && leftBot == FREE && rightBot == FREE && rightTop == FREE) {
				return indexG2TopBot[topRight][topLeft][botLeft][botRight];
			}
			if (leftTop == FREE && leftBot == FREE) {
				return mergeMetaHash(indexG2TopBot[topRight][topLeft][botLeft][botRight], indexG2RightTop[rightBot][rightTop][topRight][topLeft]);
			}
			if (rightBot == FREE && rightTop == FREE) {
				return mergeMetaHash(indexG2TopBot[topRight][topLeft][botLeft][botRight], indexG2LeftBot[leftTop][leftBot][botLeft][botRight]);
			}
			return mergeMetaHash(indexG2TopBot[topRight][topLeft][botLeft][botRight], indexG2LeftRight[leftTop][leftBot][rightBot][rightTop]);
		}
		if (leftTop != FREE && leftBot != FREE && rightBot != FREE && rightTop != FREE) {
			if (topRight == FREE && topLeft == FREE && botLeft == FREE && botRight == FREE) {
				return indexG2LeftRight[leftTop][leftBot][rightBot][rightTop];
			}
			if (topRight == FREE && topLeft == FREE) {
				return mergeMetaHash(indexG2LeftRight[leftTop][leftBot][rightBot][rightTop], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
			}
			if (botLeft == FREE && botRight == FREE) {
				return mergeMetaHash(indexG2LeftRight[leftTop][leftBot][rightBot][rightTop], indexG2TopLeft[topRight][topLeft][leftTop][leftBot]);
			}
			return mergeMetaHash(indexG2LeftRight[leftTop][leftBot][rightBot][rightTop], indexG2TopBot[topRight][topLeft][botLeft][botRight]);
		}

		// Si on est dans ce cas, ça va être long !
		return mergeMetaHash(indexG2TopLeft[topRight][topLeft][leftTop][leftBot], indexG2BotRight[botLeft][botRight][rightBot][rightTop]);
	}

	private void updatePosedPieces(int x, int y, EternityBoard board) {
		posedPieces[POSED_INDEX_TOP_RIGHT] = (OrientedPiece) board.getPieceAt(x * 2 + 1, y * 2);
		posedPieces[POSED_INDEX_TOP_LEFT] = (OrientedPiece) board.getPieceAt(x * 2, y * 2);
		posedPieces[POSED_INDEX_BOT_LEFT] = (OrientedPiece) board.getPieceAt(x * 2, y * 2 + 1);
		posedPieces[POSED_INDEX_BOT_RIGHT] = (OrientedPiece) board.getPieceAt(x * 2 + 1, y * 2 + 1);
	}

	@Override
	public void bestSolutionUpdated(int bestScore) {
		if (clusterManager.isSolutionFound()) {
			this.interrupt();
		}
	}

	@Override
	public String getSolverName() {
		return "Simple Hash & Heatmap Solver";
	}

	@Override
	public long getIterations() {
		return iterations;
	}

	public EternityBoard getBoard() {
		return board;
	}

	public void setBoard(EternityBoard board) {
		this.board = board;
	}

	public void updatedGridWithBoard(EternityBoard board, GridModel gridmodel) {
		for (int column = 0; column < board.getSIZE(); column++) {
			for (int row = 0; row < board.getSIZE(); row++) {
				OrientedPiece piece = (OrientedPiece) board.getPieceAt(row, column);
				if (piece != null) {
					QuadModel quad = gridmodel.getQuad(row, column);
					quad.setId(piece.getId());
					quad.setPattern(QuadModel.DIR_NORTH, Pattern.getPatternByCode(mapToExternalID(piece.getTop())));
					quad.setPattern(QuadModel.DIR_WEST, Pattern.getPatternByCode(mapToExternalID(piece.getLeft())));
					quad.setPattern(QuadModel.DIR_SOUTH, Pattern.getPatternByCode(mapToExternalID(piece.getBot())));
					quad.setPattern(QuadModel.DIR_EAST, Pattern.getPatternByCode(mapToExternalID(piece.getRight())));
					if(piece.isLock()){
						quad.setLocked(true);
					}
				} else {
					QuadModel quad = gridmodel.getQuad(row, column);
					quad.setId(0);
					quad.setPattern(QuadModel.DIR_NORTH, Pattern.PAT_00);
					quad.setPattern(QuadModel.DIR_WEST, Pattern.PAT_00);
					quad.setPattern(QuadModel.DIR_SOUTH, Pattern.PAT_00);
					quad.setPattern(QuadModel.DIR_EAST, Pattern.PAT_00);
				}
			}
		}
		gridmodel.notifyGridUpdated();
	}
}
