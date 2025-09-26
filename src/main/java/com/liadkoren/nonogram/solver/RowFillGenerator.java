package com.liadkoren.nonogram.solver;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

// Not thread-safe
// Iterates over all valid placements of blocks in a row of given length

public final class RowFillGenerator implements Iterator<int[]> {
	private final int rowLength;
	private final int[] certainState; // 1 = filled, -1 = empty, 0 = unknown
	private final int[] blockSizes;
	private final int[] spaceTakenByBlocksFromIndex;
	private final int[] blockPositions;
	private final int[] buffer;

	private boolean hasNextCalculated = false;
	private boolean hasNextValue = false;

	public RowFillGenerator(int[] blockSizes, int[] certainState) {
		if (blockSizes == null) throw new IllegalArgumentException("Block sizes cannot be null");
		if (certainState == null) throw new IllegalArgumentException("Certain state cannot be null");
		if (certainState.length == 0) throw new IllegalArgumentException("Certain state cannot be empty");

		this.certainState = certainState.clone();
		this.rowLength = certainState.length;
		this.blockSizes = blockSizes.clone();
		this.buffer = new int[rowLength];

		this.blockPositions = initialBlockPositions(blockSizes, rowLength);
		this.spaceTakenByBlocksFromIndex = getSpaceTakenByBlocksFromIndex(blockSizes);

		if (IsValidState()) {
			fillBuffer();
			hasNextCalculated = true;
			hasNextValue = true;
		}
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
		fillBuffer();
		hasNextValue = true;
		return true;
	}

	public int[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("No next element");
		}

		hasNextCalculated = false;
		return buffer;
	}

	private int[] getSpaceTakenByBlocksFromIndex(int[] blockSizes) {
		var spaceTakenByBlocksStartingWith = new int[blockSizes.length];
		if( blockSizes.length == 0) return spaceTakenByBlocksStartingWith; // no blocks to place

		int last = blockSizes.length - 1;
		spaceTakenByBlocksStartingWith[last] = blockSizes[last];
		// Fill remaining entries from the end
		for (int i = last - 1; i >= 0; i--) {
			spaceTakenByBlocksStartingWith[i] = blockSizes[i] + 1 + spaceTakenByBlocksStartingWith[i + 1];
		}
		return spaceTakenByBlocksStartingWith;
	}

	private boolean IsValidState() {

		int blockIndex = 0;

		for (int cell = 0; cell < rowLength; cell++) {
			if (certainState[cell] == 0) continue;

			while (blockIndex < blockPositions.length && cell > blockPositions[blockIndex] + blockSizes[blockIndex] - 1) {
				blockIndex++;
			}
			boolean inBlock = blockIndex < blockPositions.length && cell >= blockPositions[blockIndex] && cell <= blockPositions[blockIndex] + blockSizes[blockIndex] - 1;

			if (certainState[cell] == 1 && !inBlock) return false;
			if (certainState[cell] == -1 && inBlock) return false;
		}
		return true;
	}


	private void fillBuffer() {
		for (int i = 0; i < rowLength; i++) {
			buffer[i] = -1;
		}
		for (int i = 0; i < blockSizes.length; i++) {
			for (int j = 0; j < blockSizes[i]; j++) {
				buffer[blockPositions[i] + j] = 1;
			}
		}
		//printBuffer();
	}

	private void printBuffer() {
		for (int i = 0; i < rowLength; i++) {
			System.out.print(buffer[i] == 1 ? '#' : ' ');
		}
		System.out.println();
	}

	private boolean tryAdvance() {
		// find block to move
		boolean found = false;
		int i;
		for (i = blockPositions.length - 1; i >= 0; i--) {
			if (blockPositions[i] + spaceTakenByBlocksFromIndex[i] < rowLength) {
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

	private int[] initialBlockPositions(int[] blockSizes, int length) {
		var blockPositions = new int[blockSizes.length];
		if( blockSizes.length == 0) return blockPositions; // no blocks to place

		blockPositions[0] = 0;
		for (int i = 1; i < blockPositions.length; i++) {
			blockPositions[i] = blockPositions[i - 1] + blockSizes[i - 1] + 1;
		}

		if (blockPositions[blockPositions.length - 1] + blockSizes[blockSizes.length - 1] > length) {
			throw new IllegalStateException("Blocks do not fit in the given length");
		}

		return blockPositions;
	}

}