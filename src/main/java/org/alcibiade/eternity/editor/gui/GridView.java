/* This file is part of Eternity II Editor.
 * 
 * Eternity II Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Eternity II Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Eternity II Editor.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Eternity II Editor project is hosted on SourceForge:
 * http://sourceforge.net/projects/eternityii/
 * and maintained by Yannick Kirschhoffer <alcibiade@alcibiade.org>
 */

package org.alcibiade.eternity.editor.gui;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.alcibiade.eternity.editor.gui.action.CopyGridAction;
import org.alcibiade.eternity.editor.gui.action.GridAction;
import org.alcibiade.eternity.editor.gui.action.PasteGridAction;
import org.alcibiade.eternity.editor.gui.action.SortGridAction;
import org.alcibiade.eternity.editor.model.GridModel;
import org.alcibiade.eternity.editor.model.GridObserver;
import org.alcibiade.eternity.editor.model.QuadModel;

public class GridView extends JPanel implements GridObserver, EditableStatusProvider,
		GridLocalizable {

	private static final long serialVersionUID = 1L;

	private GridModel gridModel;
	private List<QuadView> quadViews;
	private boolean editable = true;
	private Set<EditableStatusListener> editableListeners;

	private JPopupMenu popup;

	private QuadModel selectedQuad = null;

	public GridView(GridModel grid) {
		gridModel = grid;
		quadViews = new ArrayList<QuadView>();
		editableListeners = new HashSet<EditableStatusListener>();

		GridAction copyAction = new CopyGridAction(gridModel);
		GridAction pasteAction = new PasteGridAction(gridModel, this);
		GridAction sortAction = new SortGridAction(gridModel, this, this);

		popup = new JPopupMenu();
		popup.add(copyAction);
		popup.add(pasteAction);
		popup.add(sortAction);

		setLayout(new GridLayout(gridModel.getSize(), gridModel.getSize()));
		updateStructure();
		gridModel.addGridObserver(this);
		setBorder(BorderFactory.createEtchedBorder());
	}

	public void addEditableStatusListener(EditableStatusListener listener) {
		editableListeners.add(listener);
	}

	public void removeEditableStatusListener(EditableStatusListener listener) {
		editableListeners.remove(listener);
	}

	private void notifyEditableStatusUpdated() {
		for (EditableStatusListener listener : editableListeners) {
			listener.editableStatusUpdated(editable);
		}
	}

	public GridModel getGridModel() {
		return gridModel;
	}

	private void updateStructure() {
		setLayout(new GridLayout(gridModel.getSize(), gridModel.getSize()));

		removeAll();
		quadViews.clear();

		for (QuadModel quad : gridModel.getQuads()) {
			QuadView quadView = new QuadView(quad);
			quadView.setEditable(editable);
			quadView.setComponentPopupMenu(popup);
			quadViews.add(quadView);
			quadView.addMouseMotionListener(new QuadSelectionUpdater(quad));
			add(quadView);
		}

		doLayout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alcibiade.eternity.editor.model.GridObserver#gridSizeUpdated(int)
	 */
	public void gridSizeUpdated(int size) {
		updateStructure();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alcibiade.eternity.editor.model.GridObserver#gridUpdated()
	 */
	public void gridUpdated() {
		repaint();
	}

	public List<QuadView> getQuadViews() {
		return quadViews;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;

		for (QuadView quad : quadViews) {
			quad.setEditable(editable);
		}

		notifyEditableStatusUpdated();
	}

	public boolean isEditable() {
		return editable;
	}

	public void setShowPatternIds(boolean show) {
		for (QuadView quad : quadViews) {
			quad.setShowPatternIds(show);
		}
	}

	public boolean getShowPatternIds() {
		return quadViews.get(0).getShowPatternIds();
	}

	@Override
	public QuadModel getSelectedQuad() {
		return selectedQuad;
	}

	private class QuadSelectionUpdater implements MouseMotionListener {
		private QuadModel quad;

		public QuadSelectionUpdater(QuadModel quad) {
			this.quad = quad;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			updateSelectedQuad();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			updateSelectedQuad();
		}

		private void updateSelectedQuad() {
			selectedQuad = quad;
		}
	}
}
