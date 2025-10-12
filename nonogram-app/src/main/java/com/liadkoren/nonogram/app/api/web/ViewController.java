package com.liadkoren.nonogram.app.api.web;

import com.liadkoren.nonogram.app.api.dto.UrlJobRequest;
import com.liadkoren.nonogram.app.jobs.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j(topic = "web.view.controller")
public class ViewController {

	private final JobService jobService;

	@GetMapping("/")
	public String showSubmitForm(Model model) {
		log.debug("render submit-form");
		model.addAttribute("urlJobRequest", new UrlJobRequest("https://onlinenonograms.com/4166", 3000L));
		return "submit-form";
	}

	@PostMapping("/jobs")
	public String submitUrl(@Valid @ModelAttribute UrlJobRequest req, BindingResult br) {
		if (br.hasErrors()) {
			log.warn("submit-url invalid url={} budgetMs={} errors={}", req.url(), req.budgetMs(),
					br.getFieldErrors().stream().map(e -> e.getField() + ":" + e.getDefaultMessage()).toList());
			return "submit-form";
		}
		log.info("submit-url accepted url={} budgetMs={}", req.url(), req.budgetMs());
		UUID jobId = jobService.submitUrlJob(URI.create(req.url()), req.budgetMs());
		log.info("submit-url created jobId={}", jobId);
		return "redirect:/jobs/" + jobId; // HTML page shell; JS will fetch JSON
	}

	@GetMapping("/jobs/{jobId}")
	public String jobPage(@PathVariable UUID jobId, Model model) {
		log.debug("render job-page jobId={}", jobId);
		model.addAttribute("jobId", jobId); // ONLY the id; no entity/DTO here
		return "job-status";
	}
}
