package com.liadkoren.nonogram.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class NonogramCluesExtractorTest {

	private NonogramCluesExtractor extractor;

	@BeforeEach
	void setUp() {
		extractor = new NonogramCluesExtractor();
	}

	// --- Test Data and Parsers ---

	private final String simpleTableHtml = "<table>" +
			"<tr> <td></td>  <td>2</td> </tr>" +
			"<tr> <td>1</td> <td>3</td> </tr>" +
			"</table>";

	private final String complexTableHtml = "<table>" +
			"<tr> <td><div>1</div></td> <td><div>&nbsp;</div></td> <td><div>2</div></td> </tr>" +
			"<tr> <td><div>&nbsp;</div></td> <td><div>3</div></td> <td><div>4</div></td> </tr>" +
			"</table>";

	private final Function<Element, String> simpleParser = Element::text;
	private final Function<Element, String> complexParser = cell -> {
		String text = cell.selectFirst("div").text();
		return text.trim().replace("\u00a0", ""); // Handle &nbsp;
	};

	// --- Row Extraction Tests ---

	@Test
	void extractAllRows_SimpleTable() {
		Element table = Jsoup.parse(simpleTableHtml).selectFirst("table");
		List<int[]> result = extractor.extractAllRows(table, simpleParser);

		assertEquals(2, result.size());
		assertArrayEquals(new int[]{2}, result.get(0));
		assertArrayEquals(new int[]{1, 3}, result.get(1));
	}

	@Test
	void extractAllRows_ComplexTable() {
		Element table = Jsoup.parse(complexTableHtml).selectFirst("table");
		List<int[]> result = extractor.extractAllRows(table, complexParser);

		assertEquals(2, result.size());
		assertArrayEquals(new int[]{1, 2}, result.get(0));
		assertArrayEquals(new int[]{3, 4}, result.get(1));
	}

	// --- Column Extraction Tests ---

	@Test
	void extractAllCols_SimpleTable() {
		Element table = Jsoup.parse(simpleTableHtml).selectFirst("table");
		List<int[]> result = extractor.extractAllCols(table, simpleParser);

		assertEquals(2, result.size());
		assertArrayEquals(new int[]{1}, result.get(0)); // First column
		assertArrayEquals(new int[]{2, 3}, result.get(1)); // Second column
	}

	@Test
	void extractAllCols_ComplexTable() {
		Element table = Jsoup.parse(complexTableHtml).selectFirst("table");
		List<int[]> result = extractor.extractAllCols(table, complexParser);

		assertEquals(3, result.size());
		assertArrayEquals(new int[]{1}, result.get(0));
		assertArrayEquals(new int[]{3}, result.get(1));
		assertArrayEquals(new int[]{2, 4}, result.get(2));
	}

	// --- Edge Case Tests ---

	@Test
	void extractMethods_ReturnEmptyList_ForEmptyTable() {
		Element table = Jsoup.parse("<table></table>").selectFirst("table");

		assertTrue(extractor.extractAllRows(table, simpleParser).isEmpty());
		assertTrue(extractor.extractAllCols(table, simpleParser).isEmpty());
	}

}