package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.solver.RowFillGenerator;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class RowFillGeneratorTest {

	@Test
	void advancesToFirstValidStateForTwoBlocks() {
		int[] blockSizes = {2, 1};
		int[] certainState = {0, 0, 0, 0, 0};
		RowFillGenerator generator = new RowFillGenerator(blockSizes, certainState);

		assertTrue(generator.hasNext());
		int[] result = generator.next();
		assertArrayEquals(new int[]{1, 1, -1, 1, -1}, result);
	}

	@Test
	void advancesThroughSequenceOfStates() {
		int[] blockSizes = {2, 1};
		int[] certainState = {0, 0, 0, 0, 0};

		RowFillGenerator generator = new RowFillGenerator(blockSizes, certainState);

		assertTrue(generator.hasNext());
		int[] first = generator.next();
		assertArrayEquals(new int[]{1, 1, -1, 1, -1}, first);


		assertTrue(generator.hasNext());
		int[] second = generator.next();
		assertArrayEquals(new int[]{1, 1, -1, -1, 1}, second);

		assertTrue(generator.hasNext());
		int[] third = generator.next();
		assertArrayEquals(new int[]{-1, 1, 1, -1, 1}, third);
	}

	@Test
	void singleFullBlockHasNoNextState() {
		int[] blockSizes = {5};
		int[] certainState = {0, 0, 0, 0, 0};
		RowFillGenerator generator = new RowFillGenerator(blockSizes, certainState);

		assertTrue(generator.hasNext());
		int[] first = generator.next();
		assertArrayEquals(new int[]{1, 1, 1, 1, 1}, first);

		// no further placements
		assertFalse(generator.hasNext());
		assertThrows(NoSuchElementException.class, generator::next);
	}

	@Test
	void respectsCertainFilledCells() {
		int[] blockSizes = {2};
		int[] certainState = {1, 1, 0, 0};
		RowFillGenerator generator = new RowFillGenerator(blockSizes, certainState);

		assertTrue(generator.hasNext());
		int[] first = generator.next();
		assertArrayEquals(new int[]{1, 1, -1, -1}, first);

		// no more valid placements
		assertFalse(generator.hasNext());
	}

	@Test
	void respectsCertainNonFilledCells() {
		int[] blockSizes = {1, 2};
		int[] certainState = {0, -1, 0, 0, 0};
		RowFillGenerator generator = new RowFillGenerator(blockSizes, certainState);

		assertTrue(generator.hasNext());
		int[] first = generator.next();
		assertArrayEquals(new int[]{1, -1, 1, 1, -1}, first);

		// no more valid placements
		assertTrue(generator.hasNext());
		int[] second = generator.next();
		assertArrayEquals(new int[]{1, -1, -1, 1, 1}, second);

		assertFalse(generator.hasNext());
	}

	@Test
	void throwsForNullBlockSizes() {
		int[] certainState = {0, 0, 0};
		assertThrows(IllegalArgumentException.class, () -> new RowFillGenerator(null, certainState));
	}

	@Test
	void throwsForNullCertainState() {
		int[] blockSizes = {1};
		assertThrows(IllegalArgumentException.class, () -> new RowFillGenerator(blockSizes, null));
	}

	@Test
	void throwsForEmptyCertainState() {
		int[] blockSizes = {1};
		int[] certainState = {};
		assertThrows(IllegalArgumentException.class, () -> new RowFillGenerator(blockSizes, certainState));
	}

	@Test
	void throwsWhenBlocksDoNotFit() {
		int[] blockSizes = {3, 3};
		int[] certainState = {0, 0, 0, 0, 0};
		assertThrows(IllegalStateException.class, () -> new RowFillGenerator(blockSizes, certainState));
	}
}
