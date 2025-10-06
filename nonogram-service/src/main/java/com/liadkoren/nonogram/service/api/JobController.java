package com.liadkoren.nonogram.service.api;

import com.liadkoren.nonogram.service.api.dto.UrlJobRequestDto;
import com.liadkoren.nonogram.service.api.dto.PuzzleJobRequestDto;
import com.liadkoren.nonogram.service.jobs.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Slf4j(topic = "jobs.api")
@Validated
class JobController {
	private final JobService jobService;

	@PostMapping("/url")
	ResponseEntity<Map<String,Object>> submitUrl(@Valid @RequestBody UrlJobRequestDto dto) {
		log.info("submit.url received host={} budgetMs={}", safeHost(dto.url()), dto.budgetMs());
		UUID jobId = jobService.submitUrlJob(URI.create(dto.url()), dto.budgetMs());
		log.info("submit.url accepted jobId={}", jobId);
		return ResponseEntity.accepted().body(Map.of("jobId", jobId, "status", "QUEUED"));
	}

	@PostMapping("/puzzle")
	ResponseEntity<Map<String,Object>> submitPuzzle(@Valid @RequestBody PuzzleJobRequestDto dto) {
		log.info("submit.puzzle received rows={} cols={} budgetMs={}",
				dto.puzzle().rows().size(), dto.puzzle().cols().size(), dto.budgetMs());
		UUID jobId = jobService.submitPuzzleJob(dto.puzzle(), dto.budgetMs());
		log.info("submit.puzzle accepted jobId={}", jobId);
		return ResponseEntity.accepted().body(Map.of("jobId", jobId, "status", "QUEUED"));
	}

	private static String safeHost(String url) {
		try { return URI.create(url).getHost(); } catch (Exception e) { return "bad_url"; }
	}
}