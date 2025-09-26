package com.liadkoren.nonogram.core.model;

public record Solution(int width, int height, int[][] grid) {
	public Solution(int[][] cells) {
		this(cells[0].length, cells.length, new int[cells.length][cells[0].length]);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				this.grid[y][x] = cells[y][x];
			}
		}
	}
}
