package com.liadkoren.nonogram.solver;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class LineFillIteratorTest {

	@Test
	void advancesToFirstValidStateForTwoBlocks() {
		int[][] grid = new int[1][5]; // all unknown
		LineFillIterator it = new LineFillIterator(new int[]{2, 1}, grid, true, 0);

		assertTrue(it.hasNext());
		int[] first = it.next();
		assertArrayEquals(new int[]{1, 1, -1, 1, -1}, first);
	}

	@Test
	void advancesThroughSequenceOfStates() {
		int[][] grid = new int[1][5];
		LineFillIterator it = new LineFillIterator(new int[]{2, 1}, grid, true, 0);

		assertTrue(it.hasNext());
		assertArrayEquals(new int[]{1, 1, -1, 1, -1}, it.next());

		assertTrue(it.hasNext());
		assertArrayEquals(new int[]{1, 1, -1, -1, 1}, it.next());

		assertTrue(it.hasNext());
		assertArrayEquals(new int[]{-1, 1, 1, -1, 1}, it.next());

		assertFalse(it.hasNext());
		assertThrows(NoSuchElementException.class, it::next);
	}

	@Test
	void singleFullBlockHasNoNextState() {
		int[][] grid = new int[1][5];
		LineFillIterator it = new LineFillIterator(new int[]{5}, grid, true, 0);

		assertTrue(it.hasNext());
		assertArrayEquals(new int[]{1, 1, 1, 1, 1}, it.next());
		assertFalse(it.hasNext());
	}

	@Test
	void respectsCertainFilledCells() {
		// len=4, clue [2], pre-mark first two cells filled
		int[][] grid = new int[1][4];
		grid[0][0] = 1;
		grid[0][1] = 1;

		LineFillIterator it = new LineFillIterator(new int[]{2}, grid, true, 0);

		assertTrue(it.hasNext());
		assertArrayEquals(new int[]{1, 1, -1, -1}, it.next());
		assertFalse(it.hasNext());
	}

	@Test
	void respectsCertainEmptyCells() {
		// len=5, clue [1,2], cell index 1 must be empty
		int[][] grid = new int[1][5];
		grid[0][1] = -1;

		LineFillIterator it = new LineFillIterator(new int[]{1, 2}, grid, true, 0);

		assertTrue(it.hasNext());
		assertArrayEquals(new int[]{1, -1, 1, 1, -1}, it.next());

		assertTrue(it.hasNext());
		assertArrayEquals(new int[]{1, -1, -1, 1, 1}, it.next());

		assertFalse(it.hasNext());
	}

	@Test
	void throwsForNullBlockSizes() {
		int[][] grid = new int[1][3];
		assertThrows(IllegalArgumentException.class, () ->
				new LineFillIterator(null, grid, true, 0));
	}

	@Test
	void throwsForNullGrid() {
		assertThrows(IllegalArgumentException.class, () ->
				new LineFillIterator(new int[]{1}, null, true, 0));
	}

	@Test
	void throwsWhenBlocksDoNotFit() {
		int[][] grid = new int[1][5];
		assertThrows(IllegalStateException.class, () ->
				new LineFillIterator(new int[]{3, 3}, grid, true, 0));
	}
}
