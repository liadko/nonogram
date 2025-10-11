package com.liadkoren.nonogram.scraper;

import com.liadkoren.nonogram.core.ports.Scraper;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class ScraperRouter {
	private final List<Scraper> scrapers;

	public ScraperRouter(List<Scraper> scrapers) {
		this.scrapers = scrapers;
	}

	public Optional<Scraper> route(URI uri) {

		return scrapers.stream()
				.filter(scraper -> scraper.canScrape(uri))
				.findFirst();

	}

}
