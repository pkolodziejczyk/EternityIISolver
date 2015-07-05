package org.k.eternity.newsolver;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.alcibiade.eternity.editor.model.GridModel;
import org.alcibiade.eternity.editor.model.Pattern;
import org.alcibiade.eternity.editor.model.QuadModel;
import org.alcibiade.eternity.editor.solver.ClusterListener;
import org.alcibiade.eternity.editor.solver.ClusterManager;
import org.alcibiade.eternity.editor.solver.EternitySolver;
import org.k.eternity.EternityBoard;
import org.k.eternity.HistoPlacing;
import org.k.eternity.Orientation;
import org.k.eternity.OrientedPiece;
import org.k.eternity.Piece;

public class Solver extends EternitySolver implements ClusterListener {
	private static final int PATTERN_NUMBER = 40;

	private long iterations;

	private int[][][][][] cacheIdSolutions;
	private Orientation[][][][][] cacheOrientationSolutions;
	protected GridModel solutionGrid;
	public int FREE = 23;
	protected GridModel originGrid;
	private EternityBoard board;
	private HistoPlacing currentHisto;
	private Piece constaintPieceTop, constaintPieceBottom, constaintPieceLeft, constaintPieceRight;
	private int constaintTop, constaintBottom, constaintLeft, constaintRight;
	private int[] listeMatchPossible;
	private boolean isSolve = false;
	private int maxDeep = 0;
	private int internalID[];
	private int externalID[];
	private int maxAllocatedInternalId;
	// Compute the numb

	private OrientedPiece[] pieces;

	public Solver(GridModel grid, GridModel solutionGrid, ClusterManager clusterManager) {
		super(clusterManager);
		this.solutionGrid = solutionGrid;

		originGrid = grid.clone();
		solutionGrid.reset();
		solutionGrid.setSize(grid.getSize());
		isSolve = false;
		board = new EternityBoard(originGrid.getSize());
		clusterManager.logMessage("Analyzing pieces...");
		loadPieces();
		clusterManager.logMessage("Initializing Hash...");
		initHash();

	}

	private void loadPieces() {
		int id = 1;
		internalID = IntStream.iterate(0, i -> -1).limit(PATTERN_NUMBER).boxed().mapToInt(e -> e)
				.toArray();
		externalID = IntStream.iterate(0, i -> -1).limit(PATTERN_NUMBER).boxed().mapToInt(e -> e)
				.toArray();
		maxAllocatedInternalId = 1;
		pieces = new OrientedPiece[originGrid.getSize() * originGrid.getSize() + 1];
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
				if (quad.isLocked()) {
					board.setPieceAt(piece, row, column);
				} else {
					pieces[piece.getId()] = piece;
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

	public void initHash() {
		System.out.println("Init interne avec " + maxAllocatedInternalId);
		FREE = maxAllocatedInternalId++;

		cacheIdSolutions = new int[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][0];
		cacheOrientationSolutions = new Orientation[maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][maxAllocatedInternalId][0];
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

	private void addPieceToSolutions(int top, int left, int bottom, int right, int id) {
		addMatchTosolutions(top, left, bottom, right, id, Orientation.TOP);
		addMatchTosolutions(left, bottom, right, top, id, Orientation.LEFT);
		addMatchTosolutions(bottom, right, top, left, id, Orientation.BOT);
		addMatchTosolutions(right, top, left, bottom, id, Orientation.RIGHT);
	}

	private void addMatchTosolutions(int top, int left, int bottom, int right, int id,
			Orientation orientation) {
		addIdToSolutions(top, left, bottom, right, id);
		addOrientationToSolutions(top, left, bottom, right, orientation);
	}

	private void addOrientationToSolutions(int top, int left, int bottom, int right,
			Orientation orientation) {
		{
			Orientation[] solutions = cacheOrientationSolutions[top][left][bottom][right];
			Orientation[] retour = Arrays.copyOf(solutions, solutions.length + 1);
			retour[solutions.length] = orientation;
			cacheOrientationSolutions[top][left][bottom][right] = retour;
		}
	}

	private void addIdToSolutions(int top, int left, int bottom, int right, int id) {
		{
			int[] solutions = cacheIdSolutions[top][left][bottom][right];
			int[] retour = Arrays.copyOf(solutions, solutions.length + 1);
			retour[solutions.length] = id;
			cacheIdSolutions[top][left][bottom][right] = retour;
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

	private boolean solve() {
		while (!isSolve) {
			if (placePieceByHeatmap(pieces, getBoard())) {
				iterations++;
				if (currentHisto.getDeep() > maxDeep || slowmotion) {
					maxDeep = currentHisto.getDeep();
					updatedGridWithBoard(board, solutionGrid);
					clusterManager.submitSolution(solutionGrid);
				}
				if (interrupted) {
					return isSolve;
				}
				currentHisto = currentHisto.revert(pieces, getBoard());
			}
		}
		return isSolve;
	}

	/**
	 * Retourne true si il y a une erreur dans la grille.
	 * 
	 * @param pieces
	 * @param board
	 * @return
	 * @throws Exception
	 */
	private boolean placePieceByHeatmap(OrientedPiece[] pieces, EternityBoard board) {
		int targetX = -1;
		int targetY = -1;
		int currentHeat = Integer.MAX_VALUE;
		if (currentHisto == null || !currentHisto.isReverted()) {
			for (int x = 0; x < board.getSIZE(); x++) {
				for (int y = 0; y < board.getSIZE(); y++) {
					int heat = countMatchAt(x, y);
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
				currentHisto = currentHisto.setNextHisto(targetX, targetY, currentHeat, 1);
			} else {
				currentHisto = new HistoPlacing(targetX, targetY, currentHeat, 1, null);
			}
			// On place une pièce :
			return placePiece(targetX, targetY, 1);
		} else {
			currentHisto.tickTry();
			// On place une pièce :
			return placePiece(currentHisto.getX(), currentHisto.getY(), currentHisto.getNbTry());
		}
	}

	private boolean placePiece(int targetX, int targetY, int nbMatch) {
		constaintPieceTop = getBoard().getPieceAt(targetX, targetY - 1);
		constaintPieceBottom = getBoard().getPieceAt(targetX, targetY + 1);
		constaintPieceLeft = getBoard().getPieceAt(targetX - 1, targetY);
		constaintPieceRight = getBoard().getPieceAt(targetX + 1, targetY);
		constaintTop = constaintPieceTop != null ? constaintPieceTop.getBot() : FREE;
		constaintBottom = constaintPieceBottom != null ? constaintPieceBottom.getTop() : FREE;
		constaintLeft = constaintPieceLeft != null ? constaintPieceLeft.getRight() : FREE;
		constaintRight = constaintPieceRight != null ? constaintPieceRight.getLeft() : FREE;
		listeMatchPossible = cacheIdSolutions[constaintTop][constaintLeft][constaintBottom][constaintRight];

		int indexToTaken;
		for (indexToTaken = 0; indexToTaken < listeMatchPossible.length; indexToTaken++) {
			if (pieces[listeMatchPossible[indexToTaken]] != null) {
				nbMatch--;
				if (nbMatch <= 0) {
					break;
				}
			}
		}
		int id = listeMatchPossible[indexToTaken];
		OrientedPiece piece = pieces[id];
		Orientation orientation = cacheOrientationSolutions[constaintTop][constaintLeft][constaintBottom][constaintRight][indexToTaken];
		piece.setOrientation(orientation);
		getBoard().setPieceAt(piece, targetX, targetY);
		pieces[id] = null;

		if (targetY != 0 && piece.getTop() == 0) {
			return true;
		}
		if (targetY != board.getSIZE() - 1 && piece.getBot() == 0) {
			return true;
		}
		if (targetX != 0 && piece.getLeft() == 0) {
			return true;
		}
		if (targetX != board.getSIZE() - 1 && piece.getRight() == 0) {
			return true;
		}
		return false;
	}

	private int countMatchAt(int x, int y) {
		if (getBoard().getPieceAt(x, y) != null) {
			return -1;
		}
		constaintPieceTop = getBoard().getPieceAt(x, y - 1);
		constaintPieceBottom = getBoard().getPieceAt(x, y + 1);
		constaintPieceLeft = getBoard().getPieceAt(x - 1, y);
		constaintPieceRight = getBoard().getPieceAt(x + 1, y);
		if (constaintPieceTop != null || constaintPieceBottom != null || constaintPieceLeft != null
				|| constaintPieceRight != null) {
			constaintTop = constaintPieceTop != null ? constaintPieceTop.getBot() : FREE;
			constaintBottom = constaintPieceBottom != null ? constaintPieceBottom.getTop() : FREE;
			constaintLeft = constaintPieceLeft != null ? constaintPieceLeft.getRight() : FREE;
			constaintRight = constaintPieceRight != null ? constaintPieceRight.getLeft() : FREE;
			listeMatchPossible = cacheIdSolutions[constaintTop][constaintLeft][constaintBottom][constaintRight];
			int count = 0;
			for (int i = 0; i < listeMatchPossible.length; i++) {
				if (pieces[listeMatchPossible[i]] != null) {
					count++;
				}
			}
			return count++;
		} else {
			return Integer.MAX_VALUE;
		}
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
					quad.setPattern(QuadModel.DIR_NORTH,
							Pattern.getPatternByCode(mapToExternalID(piece.getTop())));
					quad.setPattern(QuadModel.DIR_WEST,
							Pattern.getPatternByCode(mapToExternalID(piece.getLeft())));
					quad.setPattern(QuadModel.DIR_SOUTH,
							Pattern.getPatternByCode(mapToExternalID(piece.getBot())));
					quad.setPattern(QuadModel.DIR_EAST,
							Pattern.getPatternByCode(mapToExternalID(piece.getRight())));
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
