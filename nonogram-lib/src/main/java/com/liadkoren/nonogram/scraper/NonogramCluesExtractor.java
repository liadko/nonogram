package com.liadkoren.nonogram.scraper;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class NonogramCluesExtractor {
	List<int[]> extractAllRows(Element table, Function<Element, String> cellParser) {
		if (table == null) {
			System.out.println("No table element provided for row clues extraction.");
			return new ArrayList<>();
		}

		return table.select("tr").stream()
				.map(row -> row.select("td")) // For each row, get all its cells
				.map(cells -> extractRowClues(cells, cellParser)) // Transform the list of cells into an int[]
				.toList();

	}

	int[] extractRowClues(Elements cells, Function<Element, String> cellParser) {
		return cells.stream()
				.map(cellParser)
				.filter(s -> !s.isBlank())
				.mapToInt(Integer::parseInt)
				.toArray();
	}

	List<int[]> extractAllCols(Element table, Function<Element, String> cellParser) {
		if (table == null) {
			System.out.println("No table element provided for row clues extraction.");
			return new ArrayList<>();
		}

		Element firstRow = table.select("tr").first();
		int colCount = (firstRow != null) ? firstRow.select("td").size() : 0;
		if (colCount == 0)
			return new ArrayList<>();

		List<ArrayList<Integer>> cols = Stream.generate(ArrayList<Integer>::new).limit(colCount).toList();

		table.select("tr").forEach(row -> {
			Elements cells = row.select("td");
			for (int i = 0; i < colCount; i++) {
				String cellText = cellParser.apply(cells.get(i));
				if (!cellText.isBlank()) {
					cols.get(i).add(Integer.parseInt(cellText));
				}
			}
		});

		return cols.stream()
				.map(colList -> colList.stream().mapToInt(Integer::intValue).toArray())
				.toList();
	}
}
