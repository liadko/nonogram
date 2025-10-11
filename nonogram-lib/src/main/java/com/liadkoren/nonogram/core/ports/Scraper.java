package com.liadkoren.nonogram.core.ports;

import com.liadkoren.nonogram.core.model.Puzzle;

import java.net.URI;
import java.util.function.Function;

public interface Scraper extends Function<URI, Puzzle> {
	default Puzzle scrape(URI url) {
		return apply(url);
	}

	boolean canScrape(URI url);
}
