package org.alcibiade.eternity.editor.gui.action;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.Icon;

import org.alcibiade.eternity.editor.gui.EditableStatusProvider;
import org.alcibiade.eternity.editor.gui.GridView;
import org.alcibiade.eternity.editor.model.GridModel;
import org.alcibiade.eternity.editor.model.QuadsFormatException;
import org.alcibiade.eternity.editor.model.operation.GridLoader;
import org.k.eternity.Orientation;
import org.k.eternity.OrientedPiece;

public class LoadBoardAction extends GridUpdateAction {

	private static final long serialVersionUID = 1L;
	private String fileName;
	private GridModel grid;
	private boolean addLock = false;

	public LoadBoardAction(GridModel gridModel, String modelname, EditableStatusProvider editable) {
		super(modelname, editable);
		grid = gridModel;
	}

	public LoadBoardAction(GridModel gridModel, String modelname, EditableStatusProvider editable,
			boolean addLock) {
		super(modelname, editable);
		grid = gridModel;
		this.addLock = true;
	}

	public void actionPerformed(ActionEvent event) {
		String filePath = "./pieces.csv";
		try {
			Scanner scanner;
			scanner = new Scanner(new File(filePath));
			OrientedPiece[] pieces = new OrientedPiece[257];
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] info = line.split(";");
				OrientedPiece piece = new OrientedPiece(Integer.valueOf(info[0]).intValue(),
						Integer.valueOf(info[1]).intValue(), Integer.valueOf(info[4]).intValue(),
						Integer.valueOf(info[3]).intValue(), Integer.valueOf(info[2]).intValue());
				pieces[piece.getId()] = piece;

			}

			scanner.close();
			grid.reset();
			grid.setSize(16);
			if (addLock) {
				// I8
				OrientedPiece orientedPiece = pieces[139];
				pieces[139] = null;
				orientedPiece.setOrientation(Orientation.BOT);
				this.grid.getQuad(7, 8).loadFromPiece(orientedPiece);
				this.grid.getQuad(7, 8).setLocked(true);
				// C3 -- Top Value 13
				orientedPiece = pieces[208];
				pieces[208] = null;
				orientedPiece.setOrientation(Orientation.RIGHT);
				this.grid.getQuad(2, 2).loadFromPiece(orientedPiece);
				this.grid.getQuad(2, 2).setLocked(true);
				// C14 -- rightValue 11
				orientedPiece = pieces[255];
				pieces[255] = null;
				orientedPiece.setOrientation(Orientation.RIGHT);
				this.grid.getQuad(13, 2).loadFromPiece(orientedPiece);
				this.grid.getQuad(13, 2).setLocked(true);

				// N3 -- topValue 7
				orientedPiece = pieces[181];
				pieces[181] = null;
				orientedPiece.setOrientation(Orientation.RIGHT);
				this.grid.getQuad(2, 13).loadFromPiece(orientedPiece);
				this.grid.getQuad(2, 13).setLocked(true);
				// N14 -- topValue 8
				orientedPiece = pieces[249];
				pieces[249] = null;
				orientedPiece.setOrientation(Orientation.TOP);
				this.grid.getQuad(13, 13).loadFromPiece(orientedPiece);
				this.grid.getQuad(13, 13).setLocked(true);
			}
			try {
				this.grid.fromOrientedPieces(pieces);
			} catch (QuadsFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
