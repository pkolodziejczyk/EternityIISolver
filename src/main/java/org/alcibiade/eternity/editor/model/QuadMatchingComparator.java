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

package org.alcibiade.eternity.editor.model;

import java.util.Comparator;

public class QuadMatchingComparator implements Comparator<QuadModel> {

	private QuadModel referenceQuad;
	private boolean inverted;

	public QuadMatchingComparator(QuadModel reference) {
		this(reference, false);
	}

	public QuadMatchingComparator(QuadModel reference, boolean inverted) {
		this.referenceQuad = reference.clone();
		this.inverted = inverted;
	}

	@Override
	public int compare(QuadModel q1, QuadModel q2) {
		int result = 0;

		// Check open sides

		int def1 = referenceQuad.countDefaultPattern() - q1.countDefaultPattern();
		int def2 = referenceQuad.countDefaultPattern() - q2.countDefaultPattern();

		if (def1 == 0 && def2 == 0) {
			// Do nothing...
		} else if (def1 != 0 && def2 == 0) {
			result = 1;
		} else if (def1 == 0 && def2 != 0) {
			result = -1;
		} else {
			if (def1 > def2) {
				result = 1;
			} else {
				result = -1;
			}
		}

		// Check matching degrees

		if (result == 0) {
			int matches1 = referenceQuad.matchDegrees(q1);
			int matches2 = referenceQuad.matchDegrees(q2);
			result = matches2 - matches1;
		}

		// Count similar patterns

		if (result == 0) {
			int patScore1 = computePatternScore(q1);
			int patScore2 = computePatternScore(q2);
			result = patScore2 - patScore1;
		}

		if (inverted) {
			result = -result;
		}

		return result;
	}

	private int computePatternScore(QuadModel q) {
		int score = 0;

		for (int i = 0; i < 4; i++) {
			Pattern pat = q.getPattern(i);

			for (int j = 0; j < 4; j++) {
				Pattern refPat = referenceQuad.getPattern(j);
				if (refPat == pat) {
					score += 1;
				}
			}
		}

		return score;
	}

}
