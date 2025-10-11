package com.liadkoren.nonogram.service.api;

import com.liadkoren.nonogram.service.api.dto.JobStatusResponse;
import com.liadkoren.nonogram.service.api.dto.JobSubmittedResponse;
import com.liadkoren.nonogram.service.api.dto.UrlJobRequest;
import com.liadkoren.nonogram.service.api.dto.PuzzleJobRequest;
import com.liadkoren.nonogram.service.jobs.JobService;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Slf4j(topic = "jobs.api")
@Validated
class JobController {
	private final JobService jobService;

	@PostMapping("/url")
	ResponseEntity<JobSubmittedResponse> submitUrl(@Valid @RequestBody UrlJobRequest dto) {
		log.info("submit.url received host={} budgetMs={}", safeHost(dto.url()), dto.budgetMs());
		UUID jobId = jobService.submitUrlJob(URI.create(dto.url()), dto.budgetMs());
		log.info("submit.url accepted jobId={}", jobId);
		return ResponseEntity.accepted().body(new JobSubmittedResponse(jobId, "QUEUED"));
	}

	@PostMapping("/puzzle")
	ResponseEntity<JobSubmittedResponse> submitPuzzle(@Valid @RequestBody PuzzleJobRequest dto) {
		log.info("submit.puzzle received rows={} cols={} budgetMs={}",
				dto.puzzle().rows().size(), dto.puzzle().cols().size(), dto.budgetMs());
		UUID jobId = jobService.submitPuzzleJob(dto.puzzle(), dto.budgetMs());
		log.info("submit.puzzle accepted jobId={}", jobId);
		return ResponseEntity.accepted().body(new JobSubmittedResponse(jobId, "QUEUED"));
	}

	@GetMapping("/{jobId}")
	public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable UUID jobId) {
		log.info("status request received jobId={}", jobId);
		return jobService.findJob(jobId)
				.map(JobStatusResponse::fromEntity)
				.map(ResponseEntity::ok)
				.orElseGet(() -> {
					log.warn("status request not found jobId={}", jobId);
					return ResponseEntity.notFound().build();
				});
	}

	private static String safeHost(String url) {
		try { return URI.create(url).getHost(); } catch (Exception e) { return "bad_url"; }
	}
}