package com.liadkoren.nonogram.scraper;

import org.jsoup.nodes.Element;

public class OnlineNonogramScraper extends AbstractNonogramScraper {

	@Override
	public String getAcceptedDomain() {
		return "onlinenonograms.com";
	}

	@Override
	protected String getRowCluesSelector() {
		return "#cross_left";
	}

	@Override
	protected String getColCluesSelector() {
		return "#cross_top";
	}

	@Override
	protected String parseCell(Element cell) {
		// This site nests clues in a <div>
		String text = cell.text();
		return text.trim().replace("\u00a0", ""); // Handle &nbsp;
	}
}
