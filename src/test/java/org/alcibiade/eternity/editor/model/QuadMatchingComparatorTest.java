package org.alcibiade.eternity.editor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class QuadMatchingComparatorTest {

	@Test
	public void testFullMatches() {
		QuadModel q1 = new QuadModel(Pattern.PAT_01, Pattern.PAT_02, Pattern.PAT_03, Pattern.PAT_04);
		QuadModel q2 = new QuadModel(Pattern.PAT_03, Pattern.PAT_04, Pattern.PAT_01, Pattern.PAT_02);
		QuadMatchingComparator comparator = new QuadMatchingComparator(q1);
		assertTrue(comparator.compare(q1, q2) == 0);
		assertTrue(comparator.compare(q2, q1) == 0);
	}

	@Test
	public void testBadMatches() {
		QuadModel q1 = new QuadModel(Pattern.PAT_01, Pattern.PAT_02, Pattern.PAT_03, Pattern.PAT_04);
		QuadModel q2 = new QuadModel(Pattern.PAT_05, Pattern.PAT_06, Pattern.PAT_07, Pattern.PAT_08);
		QuadMatchingComparator comparator = new QuadMatchingComparator(q1);
		assertTrue(comparator.compare(q1, q2) < 0);
		assertTrue(comparator.compare(q2, q2) == 0);
		assertTrue(comparator.compare(q2, q1) > 0);
	}

	@Test
	public void testPartialMatches() {
		QuadModel q1 = new QuadModel(Pattern.PAT_01, Pattern.PAT_02, Pattern.PAT_03, Pattern.PAT_04);
		QuadModel q2 = new QuadModel(Pattern.PAT_01, Pattern.PAT_02, Pattern.PAT_09, Pattern.PAT_04);
		QuadModel q3 = new QuadModel(Pattern.PAT_05, Pattern.PAT_06, Pattern.PAT_07, Pattern.PAT_08);
		QuadMatchingComparator comparator = new QuadMatchingComparator(q1);
		assertTrue(comparator.compare(q2, q3) < 0);
		assertTrue(comparator.compare(q3, q2) > 0);
	}

	@Test
	public void testCollection() {
		QuadModel q1 = new QuadModel(Pattern.PAT_01, Pattern.PAT_02, Pattern.PAT_03, Pattern.PAT_04);
		QuadModel q2 = new QuadModel(Pattern.PAT_01, Pattern.PAT_02, Pattern.PAT_09, Pattern.PAT_04);
		QuadModel q3 = new QuadModel(Pattern.PAT_05, Pattern.PAT_06, Pattern.PAT_07, Pattern.PAT_08);

		QuadMatchingComparator comparator = new QuadMatchingComparator(q2, false);

		List<QuadModel> quads = new ArrayList<QuadModel>();
		quads.add(q1);
		quads.add(q2);
		quads.add(q3);

		Collections.sort(quads, comparator);

		assertEquals(q2, quads.get(0));
		assertEquals(q1, quads.get(1));
		assertEquals(q3, quads.get(2));

		QuadMatchingComparator comparatorInverted = new QuadMatchingComparator(q2, true);

		Collections.sort(quads, comparatorInverted);

		assertEquals(q3, quads.get(0));
		assertEquals(q1, quads.get(1));
		assertEquals(q2, quads.get(2));
	}
}
