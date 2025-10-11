package com.liadkoren.nonogram.scraper;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.ports.Scraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.List;

public abstract class AbstractNonogramScraper implements Scraper {
	NonogramCluesExtractor nonogramCluesExtractor = new NonogramCluesExtractor();

	@Override
	public Puzzle apply(URI uri) {

		if (!canScrape(uri)) {
			throw new IllegalArgumentException(
					"The provided URL host '" + uri.getHost() + "' is not supported by " +
							getClass().getSimpleName() + ". Expected domain: " + getAcceptedDomain()
			);
		}

		Document doc;
		try {
			// 1. Connect and fetch the HTML document
			doc = Jsoup.connect(uri.toString())
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36")
					.get();
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch html puzzle from URI: " + uri, e);
		}

		return parseDocument(doc);
	}


	Puzzle parseDocument(Document doc) {
		Element rowCluesTable = doc.selectFirst(getRowCluesSelector());
		Element colCluesTable = doc.selectFirst(getColCluesSelector());

		// 3. Parse each table into its list of clues
		List<int[]> rowClues = nonogramCluesExtractor.extractAllRows(rowCluesTable, this::parseCell);
		List<int[]> colClues = nonogramCluesExtractor.extractAllCols(colCluesTable, this::parseCell);

		// 4. Construct and return the final Puzzle object
		return new Puzzle(rowClues, colClues);
	}

	public boolean canScrape(URI uri) {
		String host = uri.getHost();
		return host != null && host.endsWith(getAcceptedDomain());
	}

	public abstract String getAcceptedDomain();

	protected abstract String getRowCluesSelector();

	protected abstract String getColCluesSelector();

	protected abstract String parseCell(Element cell);

}