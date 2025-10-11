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
public class JapaneseNonogramScraperIT {

	/**
	 * This test is disabled by default to prevent it from running automatically
	 * during a normal build, as it is slow and depends on an external service.
	 * Remove the @Disabled annotation to run it manually.
	 */
	@Test
	@Disabled("Enable manually to run against the live website")
	void apply_scrapesRealUrlSuccessfully() {
		// 1. Arrange
		JapaneseNonogramScraper scraper = new JapaneseNonogramScraper();
		URI realUri = URI.create("https://www.nonograms.org/nonograms/i/78179");

		// 2. Act
		// This makes a real network call to the website.
		Puzzle result = scraper.apply(realUri);

		// 3. Assert
		assertNotNull(result);

		// These assertions validate that the live HTML was parsed correctly,
		// matching the known solution for this specific puzzle.
		assertAll("Puzzle validation for puzzle 78179",
				() -> assertEquals(15, result.rows().size(), "Should have 15 rows of clues"),
				() -> assertEquals(20, result.cols().size(), "Should have 20 columns of clues"),

				// Spot check a few rows
				() -> assertArrayEquals(new int[]{3, 7}, result.rows().get(0), "Row 1 clues incorrect"),
				() -> assertArrayEquals(new int[]{1, 1, 2, 3}, result.rows().get(6), "Row 7 clues incorrect"),
				() -> assertArrayEquals(new int[]{2, 2, 7}, result.rows().get(12), "Row 13 clues incorrect"),
				() -> assertTrue(result.rows().get(13).length == 0, "Row 14 should be empty"),

				// Spot check a few columns
				() -> assertArrayEquals(new int[]{7, 4}, result.cols().get(0), "Column 1 clues incorrect"),
				// NOTE: I corrected this assertion to match the puzzle image. The 3rd column is {1, 2, 1}.
				() -> assertArrayEquals(new int[]{1, 2, 1}, result.cols().get(2), "Column 3 clues incorrect"),
				() -> assertArrayEquals(new int[]{5, 8}, result.cols().get(17), "Column 18 clues incorrect"),
				() -> assertArrayEquals(new int[]{9}, result.cols().get(19), "Column 20 clues incorrect")
		);
	}
}
