package com.liadkoren.nonogram.app.api.web;

import com.liadkoren.nonogram.app.api.dto.UrlJobRequest;
import com.liadkoren.nonogram.app.jobs.JobService;
import com.liadkoren.nonogram.app.jobs.model.JobEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ViewController {

	private final JobService jobService;

	/**
	 * Handles GET requests to the root URL ("/") to display the job submission form.
	 *
	 * @param model Spring provides this object. We use it to pass data to the view template.
	 * @return The logical name of the view template to render.
	 */
	@GetMapping("/")
	public String showSubmitForm(Model model) {

		model.addAttribute("urlJobRequest", new UrlJobRequest("https://onlinenonograms.com/4166", 3000L));

		return "submit-form";
	}

	/**
	 * Handles the POST request from the submission form.
	 *
	 * @param urlJobRequest This object is automatically created by Spring from the form data.
	 * The @Valid annotation triggers validation rules on it.
	 * @param bindingResult A special object that collects any validation errors.
	 * @return A redirect string that tells the browser to go to a new URL.
	 */
	@PostMapping("/submit-url")
	public String handleSubmitForm(@Valid @ModelAttribute UrlJobRequest urlJobRequest, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return "submit-form";
		}

		// If validation passes, we call the service layer.
		UUID jobId = jobService.submitUrlJob(URI.create(urlJobRequest.url()), urlJobRequest.budgetMs());

		// We redirect to a GET endpoint to show the status.
		// This prevents re-submission on refresh.
		return "redirect:/job/" + jobId;
	}

	/**
	 * Displays the status page for a specific job.
	 */
	@GetMapping("/job/{jobId}")
	public String showJobStatus(@PathVariable UUID jobId, Model model) {
		Optional<JobEntity> jobOptional = jobService.findJob(jobId);

		if (jobOptional.isEmpty()) {
			// If the job doesn't exist, maybe show an error page.
			// For now, we'll redirect back to the home page.
			return "redirect:/";
		}

		// If the job is found, add it to the model and show the status page.
		model.addAttribute("job", jobOptional.get());
		return "job-status";
	}
}