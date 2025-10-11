package com.liadkoren.nonogram.scraper;

import com.liadkoren.nonogram.core.model.Puzzle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the JapaneseNonogramScraper.
 * These tests make real network calls to the live website to ensure the scraper
 * can still parse the HTML correctly.
 */
@Tag("integration") // Mark this class as containing integration tests
public class OnlineNonogramScraperIT {

	/**
	 * This test is disabled by default to prevent it from running automatically
	 * during a normal build, as it is slow and depends on an external service.
	 * Remove the @Disabled annotation to run it manually.
	 */
	@Test
	@Disabled("Enable manually to run against the live website")
	void apply_scrapesRealUrlSuccessfully() {
		// 1. Arrange
		OnlineNonogramScraper scraper = new OnlineNonogramScraper();
		URI realUri = URI.create("https://onlinenonograms.com/1326");

		// 2. Act
		// This makes a real network call to the website.
		Puzzle result = scraper.apply(realUri);

		// 3. Assert
		assertNotNull(result);

		assertAll("Puzzle validation for the new 5x5 puzzle",
				() -> assertEquals(5, result.rows().size(), "Should have 5 rows of clues"),
				() -> assertEquals(5, result.cols().size(), "Should have 5 columns of clues"),

				// Assert all rows
				() -> assertArrayEquals(new int[]{1, 1, 1}, result.rows().get(0), "Row 1 clues incorrect"),
				() -> assertArrayEquals(new int[]{5}, result.rows().get(1), "Row 2 clues incorrect"),
				() -> assertArrayEquals(new int[]{3}, result.rows().get(2), "Row 3 clues incorrect"),
				() -> assertArrayEquals(new int[]{5}, result.rows().get(3), "Row 4 clues incorrect"),
				() -> assertArrayEquals(new int[]{1, 1}, result.rows().get(4), "Row 5 clues incorrect"),

				// Assert all columns
				() -> assertArrayEquals(new int[]{2, 2}, result.cols().get(0), "Column 1 clues incorrect"),
				() -> assertArrayEquals(new int[]{3}, result.cols().get(1), "Column 2 clues incorrect"),
				() -> assertArrayEquals(new int[]{4}, result.cols().get(2), "Column 3 clues incorrect"),
				() -> assertArrayEquals(new int[]{3}, result.cols().get(3), "Column 4 clues incorrect"),
				() -> assertArrayEquals(new int[]{2, 2}, result.cols().get(4), "Column 5 clues incorrect")
		);
	}
}
