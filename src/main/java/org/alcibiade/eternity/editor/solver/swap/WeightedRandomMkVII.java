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

package org.alcibiade.eternity.editor.solver.swap;

import org.alcibiade.eternity.editor.model.GridModel;
import org.alcibiade.eternity.editor.model.Pattern;
import org.alcibiade.eternity.editor.model.PatternColor;
import org.alcibiade.eternity.editor.model.QuadModel;
import org.alcibiade.eternity.editor.solver.ClusterManager;

/**
 * Extends MkV weight adaptation step by trying to compute fitness according to
 * surrounding colors.
 */
public class WeightedRandomMkVII extends WeightedRandomMkV {

	public WeightedRandomMkVII(GridModel grid, GridModel solutionGrid, ClusterManager clusterManager) {
		super(grid, solutionGrid, clusterManager);
	}

	@Override
	public String getSolverName() {
		return "WeightedRandomMkVII Solver $Revision: 263 $";
	}

	@Override
	protected void computeWeights(GridModel grid, WeightMatrix weights) {
		super.computeWeights(grid, weights);

		int positions = grid.getSize() * grid.getSize();

		for (int position = 0; position < positions; position++) {
			QuadModel quad = grid.getQuad(position);
			int misses = countMisses(grid, position, quad);
			long weight = weights.getWeight(position) * (1 + misses);
			weights.setWeight(position, weight);
		}
	}

	@Override
	protected void adaptWeights(GridModel grid, WeightMatrix weights, int firstSelection) {
		super.adaptWeights(grid, weights, firstSelection);

		int positions = grid.getSize() * grid.getSize();
		QuadModel quad = grid.getQuad(firstSelection);

		for (int position = 0; position < positions; position++) {
			if (position != firstSelection) {
				int misses = countMisses(grid, position, quad);
				if (misses < 3) {
					long weight = weights.getWeight(position) * (4 - misses);
					weights.setWeight(position, weight);
				} else {
					weights.setWeight(position, weights.getWeight(position) / 10);
				}
			}
		}
	}

	private int countMisses(GridModel grid, int position, QuadModel quad) {
		int misses = 0;

		for (int d = 0; d < 4; d++) {
			QuadModel neighbor = grid.getNeighbor(position, d);

			if (neighbor != null) {
				PatternColor centerColor = quad.getPattern(d).getPatternBg();
				int sameBacks = 0;
				for (int nd = 0; nd < 4; nd++) {
					Pattern np = neighbor.getPattern(nd);
					if (np.getPatternBg() == centerColor) {
						sameBacks += 1;
					}
				}

				if (sameBacks < 2) {
					misses += 1;
				}
			}
		}
		return misses;
	}
}
