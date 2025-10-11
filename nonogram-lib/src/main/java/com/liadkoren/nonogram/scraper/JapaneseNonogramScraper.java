package com.liadkoren.nonogram.scraper;

import org.jsoup.nodes.Element;

public class JapaneseNonogramScraper extends AbstractNonogramScraper {

	@Override
	public String getAcceptedDomain() {
		return "nonograms.org";
	}

	@Override
	protected String getRowCluesSelector() {
		return ".nmtl";
	}

	@Override
	protected String getColCluesSelector() {
		return ".nmtt";
	}

	@Override
	protected String parseCell(Element cell) {
		// This site nests clues in a <div>
		String text = cell.selectFirst("div").text();
		return text.trim().replace("\u00a0", ""); // Handle &nbsp;
	}
}
