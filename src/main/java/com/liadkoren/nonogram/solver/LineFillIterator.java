package com.liadkoren.nonogram.solver;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

// Not thread-safe
// Iterates over all valid placements of blocks in a row of given length

public final class LineFillIterator implements Iterator<int[]> {
	private final int[][] puzzleGrid;

	private final boolean isRow;
	private final int lineIndex;
	private final int lineLength;

	private final int[] blockSizes;
	private final int[] initialBlockPositions, blockPositions;

	private final int[] spaceTakenByBlocksFromIndex;

	private final int[] intersection, currentLine, certainStateCache;

	private boolean hasNextCalculated = false;
	private boolean hasNextValue = false;

	public LineFillIterator(int[] blockSizes, int[][] puzzleGrid, boolean isRow, int lineIndex) {
		if (puzzleGrid == null) throw new IllegalArgumentException("Puzzle grid cannot be null");
		if (blockSizes == null) throw new IllegalArgumentException("Block sizes cannot be null");

		this.lineLength = isRow ? puzzleGrid[0].length : puzzleGrid.length;
		if (lineLength == 0) throw new IllegalArgumentException("Line length cannot be zero");
		this.puzzleGrid = puzzleGrid;

		this.isRow = isRow;
		this.lineIndex = lineIndex;

		this.blockSizes = blockSizes;
		this.intersection = new int[lineLength];
		this.currentLine = new int[lineLength];
		this.certainStateCache = new int[lineLength];

		this.initialBlockPositions = getInitialBlockPositions(blockSizes, lineLength);
		this.blockPositions = new int[blockSizes.length];

		this.spaceTakenByBlocksFromIndex = getSpaceTakenByBlocksFromIndex(blockSizes);

		resetBlocks();
	}

	public boolean hasNext() {
		if (hasNextCalculated) return hasNextValue;
		hasNextCalculated = true;

		do {
			boolean advanced = tryAdvance();

			if (!advanced) {
				hasNextValue = false;
				return false;
			}
		} while (!IsValidState());

		// fill buffer
		fillCurrentLineBuffer();
		hasNextValue = true;
		return true;
	}

	public int[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("No next element");
		}

		hasNextCalculated = false;
		return currentLine;
	}


	// Deduces certain cells by intersecting all valid fills
	// Updates the puzzle grid with certain cells
	// Returns true if all cells are certain (no zeros in intersectionView)
	public boolean deduce() {
		resetBlocks();
		boolean certain = intersect();
		updateGridWithIntersection();
		return certain;
	}

	public record DeductionResult(boolean certain) {}

	/**
	 * Runs deduction but does not write to the grid.
	 * Returns a view of the internal intersectionView buffer.
	 * This array is reused by subsequent calls and is only valid
	 * until the next call to any deduction method on this iterator.
	 */
	public DeductionResult parallelDeduce() {
		//System.out.println("Parallel deduce called on line " + (isRow ? "row " : "col ") + lineIndex);
		resetBlocks();
		boolean certain = intersect();
		return new DeductionResult(certain);
	}


	public void resetBlocks() {
		// reset block positions
		System.arraycopy(initialBlockPositions, 0, blockPositions, 0, blockPositions.length);

		// build certain state cache from current puzzle grid
		if (isRow) {
			System.arraycopy(puzzleGrid[lineIndex], 0, certainStateCache, 0, lineLength);
		} else {
			for (int i = 0; i < lineLength; i++) {
				certainStateCache[i] = puzzleGrid[i][lineIndex];
			}
		}

		hasNextCalculated = false;
		if (IsValidState()) {
			fillCurrentLineBuffer();
			hasNextCalculated = true;
			hasNextValue = true;
		}
	}


	// Intersects all valid fills, storing result in intersectionView array
	// Returns true if all cells are certain (no zeros in intersectionView)
	public boolean intersect() {
		if (!hasNext()) {
			throw new IllegalStateException("No valid fills for this line.");
		}

		// seed from the first candidate
		int[] first = next();
		System.arraycopy(first, 0, intersection, 0, lineLength);


		boolean certain = true;
		// intersect with remaining candidates
		while (hasNext()) {
			int[] next = next();
			for (int i = 0; i < lineLength; i++) {
				if (intersection[i] == 0) {
					certain = false;
					continue;
				}

				if (intersection[i] != next[i]) {
					certain = false;
					intersection[i] = 0;
				}
			}

		}

		return certain;
	}

	public void updateGridWithIntersection() {
		if (isRow) {
			System.arraycopy(intersection, 0, puzzleGrid[lineIndex], 0, lineLength);
		} else {
			for (int i = 0; i < lineLength; i++) {
				puzzleGrid[i][lineIndex] = intersection[i];
			}
		}
	}

	/** Applies the current intersection to grid, writing only new certainties.
	 *  Returns true if any cell changed; throws if a contradiction is found. */
	public boolean parallelMergeIntersectionIntoGrid() {
		boolean changed = false;
		if (isRow) {
			int r = lineIndex;
			for (int c = 0; c < intersection.length; c++) {
				int v = intersection[c];
				if (v == 0) continue;                // unknown in this line ⇒ don't touch
				int prev = puzzleGrid[r][c];
				if (prev == 0) {                     // new certainty
					puzzleGrid[r][c] = v;
					changed = true;
				} else if (prev != v) {
					throw new IllegalStateException("Deduction conflict at (" + r + "," + c + "): " + prev + " vs " + v);
				}
			}
		} else {
			int c = lineIndex;
			for (int r = 0; r < intersection.length; r++) {
				int v = intersection[r];
				if (v == 0) continue;
				int prev = puzzleGrid[r][c];
				if (prev == 0) {
					puzzleGrid[r][c] = v;
					changed = true;
				} else if (prev != v) {
					throw new IllegalStateException("Deduction conflict at (" + r + "," + c + "): " + prev + " vs " + v);
				}
			}
		}
		return changed;
	}


	private boolean IsValidState() {
		int blockIndex = 0;

		for (int cell = 0; cell < lineLength; cell++) {
			final int certainState = certainStateCache[cell];
			if (certainState == 0) continue;

			while (blockIndex < blockPositions.length && cell > blockPositions[blockIndex] + blockSizes[blockIndex] - 1) {
				blockIndex++;
			}
			boolean inBlock = blockIndex < blockPositions.length && cell >= blockPositions[blockIndex] && cell <= blockPositions[blockIndex] + blockSizes[blockIndex] - 1;

			if (certainState == 1 && !inBlock) return false;
			if (certainState == -1 && inBlock) return false;
		}
		return true;
	}


	// tries to advance to the next valid state, returns false if no more states
	private boolean tryAdvance() {
		// find block to move
		boolean found = false;
		int i;
		for (i = blockPositions.length - 1; i >= 0; i--) {
			if (blockPositions[i] + spaceTakenByBlocksFromIndex[i] < lineLength) {
				found = true;
				break;
			}
		}
		if (!found) return false;

		// move block
		advanceBlock(i);
		return true;
	}

	// advances the i-th block by one position to the right, and resets all following blocks to their initial positions
	private void advanceBlock(int i) {
		blockPositions[i]++;
		for (int j = i + 1; j < blockPositions.length; j++) {
			blockPositions[j] = blockPositions[j - 1] + blockSizes[j - 1] + 1;
		}
	}

	private int[] getInitialBlockPositions(int[] blockSizes, int length) {
		var blockPositions = new int[blockSizes.length];
		if (blockSizes.length == 0) return blockPositions; // no blocks to place

		blockPositions[0] = 0;
		for (int i = 1; i < blockPositions.length; i++) {
			blockPositions[i] = blockPositions[i - 1] + blockSizes[i - 1] + 1;
		}

		if (blockPositions[blockPositions.length - 1] + blockSizes[blockSizes.length - 1] > length) {
			throw new IllegalStateException("Blocks do not fit in the given length");
		}

		return blockPositions;
	}

	private int[] getSpaceTakenByBlocksFromIndex(int[] blockSizes) {
		int[] spaceTakenByBlocksStartingWith = new int[blockSizes.length];
		if (blockSizes.length == 0) return spaceTakenByBlocksStartingWith; // no blocks to place

		int last = blockSizes.length - 1;
		spaceTakenByBlocksStartingWith[last] = blockSizes[last];
		// Fill remaining entries from the end
		for (int i = last - 1; i >= 0; i--) {
			spaceTakenByBlocksStartingWith[i] = blockSizes[i] + 1 + spaceTakenByBlocksStartingWith[i + 1];
		}
		return spaceTakenByBlocksStartingWith;
	}

	// maybe optimize by only updating buffer after index i, or after block i? probably not. tried
	// fills currentLine buffer according to current block positions
	private void fillCurrentLineBuffer() {
		Arrays.fill(currentLine, -1); // start with all empty

		// fill blocks
		for (int i = 0; i < blockSizes.length; i++) {
			final int blockStart = blockPositions[i];
			final int blockSize = blockSizes[i];
			Arrays.fill(currentLine, blockStart, blockStart + blockSize, 1);
		}

		//printBuffer();
	}

	private void printBuffer() {
		for (int i = 0; i < lineLength; i++) {
			System.out.print(currentLine[i] == 1 ? '#' : ' ');
		}
		System.out.println();
	}

	public static BigInteger countPlacements(int lineLength, int[] blockSizes) {
		if (lineLength < 0) return BigInteger.ZERO;
		if (blockSizes == null) throw new IllegalArgumentException("blockSizes cannot be null");

		int k = blockSizes.length;
		if (k == 0) return BigInteger.ONE; // all empty line

		long sum = 0;
		for (int b : blockSizes) {
			if (b <= 0) throw new IllegalArgumentException("block sizes must be positive");
			sum += b;
		}

		long mandatory = sum + (k - 1); // blocks + required single gaps between them
		if (mandatory > lineLength) return BigInteger.ZERO;

		long extra = lineLength - mandatory; // extra empty cells to distribute
		int gaps = k + 1;                    // leading, (k-1) internal, trailing

		// #ways = C(extra + gaps - 1, gaps - 1)
		return binomial(extra + gaps - 1, gaps - 1);
	}

	private static BigInteger binomial(long n, long k) {
		if (k < 0 || k > n) return BigInteger.ZERO;
		k = Math.min(k, n - k);
		BigInteger res = BigInteger.ONE;
		for (long i = 1; i <= k; i++) {
			res = res.multiply(BigInteger.valueOf(n - k + i));
			res = res.divide(BigInteger.valueOf(i)); // exact division
		}
		return res;
	}

}