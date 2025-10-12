package com.liadkoren.nonogram.app.jobs.adapters;

import com.liadkoren.nonogram.scraper.JapaneseNonogramScraper;
import com.liadkoren.nonogram.scraper.OnlineNonogramScraper;
import com.liadkoren.nonogram.scraper.ScraperRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NonogramScraperConfig {

	@Bean
	public ScraperRouter scraperRouter() {
		return new ScraperRouter(List.of(
				new OnlineNonogramScraper(),
				new JapaneseNonogramScraper()
		));
	}

}
