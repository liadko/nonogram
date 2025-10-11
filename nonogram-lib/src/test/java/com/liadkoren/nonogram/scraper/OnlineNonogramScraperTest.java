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

public class OnlineNonogramScraperTest {


	@Test
	void tryParse_successfullyParsesPuzzleFromResourceFile() throws IOException {
		// 1. Arrange
		OnlineNonogramScraper scraper = new OnlineNonogramScraper();

		// Load the HTML file from the 'resources' folder
		Document doc;
		try (InputStream is = OnlineNonogramScraperTest.class
				.getResourceAsStream("online-nonogram.html")) {
			assertNotNull(is, "Test resource file not found!");
			doc = Jsoup.parse(is, StandardCharsets.UTF_8.name(), "");
		}

		Puzzle result = scraper.parseDocument(doc);

		assertNotNull(result);

		// Assertions remain the same...
		assertAll(
				() -> assertArrayEquals(new int[]{1, 1, 1}, result.rows().get(0)),
				() -> assertArrayEquals(new int[]{5}, result.rows().get(1)),
				() -> assertArrayEquals(new int[]{2, 2}, result.cols().get(0)),
				() -> assertArrayEquals(new int[]{4}, result.cols().get(2))
		);


	}
}
