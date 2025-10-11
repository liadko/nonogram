package com.liadkoren.nonogram.scraper;

import com.liadkoren.nonogram.core.model.Puzzle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class JapaneseNonogramScraperTest {

	@Test
	void apply_withInvalidUrl_throwsRuntimeException() {
		// 1. Arrange
		JapaneseNonogramScraper scraper = new JapaneseNonogramScraper();
		String invalidUrl = "http://invalid-url";

		// 2. Act & Assert
		RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> {
			scraper.apply(URI.create(invalidUrl));
		});
	}

	@Test
	void apply_withInvalidUrl_throwsException() {
		// 1. Arrange
		JapaneseNonogramScraper scraper = new JapaneseNonogramScraper();
		String invalidUrl = "http://nonograms.org";

		// 2. Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			scraper.apply(URI.create(invalidUrl));
		});
	}

	@Test
	void tryParse_successfullyParsesPuzzleFromResourceFile() throws IOException {
		// 1. Arrange
		JapaneseNonogramScraper scraper = new JapaneseNonogramScraper();

		// Load the HTML file from the 'resources' folder
		Document doc;
		try (InputStream is = JapaneseNonogramScraperTest.class
				.getResourceAsStream("japanese-nonogram.html")) {
			assertNotNull(is, "Test resource file not found!");
			doc = Jsoup.parse(is, StandardCharsets.UTF_8.name(), "");
		}

		Puzzle result = scraper.parseDocument(doc);

		assertNotNull(result);

		// Assertions remain the same...
		assertAll(
				() -> assertArrayEquals(new int[]{3, 7}, result.rows().get(0)),
				() -> assertArrayEquals(new int[]{1, 1, 2, 3}, result.rows().get(6)),
				() -> assertArrayEquals(new int[]{7, 4}, result.cols().get(0)),
				() -> assertArrayEquals(new int[]{1, 2, 2, 1}, result.cols().get(2))
		);


	}
}
