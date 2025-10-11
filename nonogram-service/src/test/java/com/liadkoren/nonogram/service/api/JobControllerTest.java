package com.liadkoren.nonogram.service.api;

import com.liadkoren.nonogram.service.jobs.JobService;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import com.liadkoren.nonogram.service.jobs.model.JobEntity.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the JobController.
 *
 * @WebMvcTest focuses on the web layer, loading only the specified controller
 * and its related Spring MVC components. It does not load the full application context.
 * @MockitoBean creates a mock implementation of the JobService, allowing us to
 * define its behavior for our tests without involving the actual service logic.
 */
@WebMvcTest(JobController.class)
class JobControllerTest {

	// MockMvc provides a powerful way to test MVC controllers without needing a full HTTP server.
	@Autowired
	private MockMvc mockMvc;

	// We mock the service layer to isolate the controller for unit testing.
	@MockitoBean
	private JobService jobService;

	// --- Tests for GET /{jobId} ---

	/**
	 * Test case for the "happy path": a job ID is provided and the corresponding job exists.
	 * We expect an HTTP 200 OK status and a JSON response body containing the job details.
	 */
	@Test
	void getJobStatus_whenJobExists_returnsOkWithJobDto() throws Exception {
		// --- Arrange ---
		UUID jobId = UUID.randomUUID();
		JobEntity mockJob = new JobEntity();
		mockJob.setId(jobId);
		mockJob.setStatus(JobStatus.SUCCESS);
		mockJob.setSolutionTimeMs(1234L);
		mockJob.setResultGrid(new boolean[][]{{true, false}, {false, true}});
		mockJob.setErrorMessage(null); // Explicitly null for a success case

		// Define the behavior of the mocked service
		when(jobService.findJob(jobId)).thenReturn(Optional.of(mockJob));

		// --- Act & Assert ---
		mockMvc.perform(get("/jobs/{jobId}", jobId))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(jobId.toString()))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.solutionTimeMs").value(1234))
				.andExpect(jsonPath("$.resultGrid[0][0]").value(true))
				.andExpect(jsonPath("$.resultGrid[1][1]").value(true))
				.andExpect(jsonPath("$.errorMessage").doesNotExist()); // Verify field is absent due to NON_NULL
	}

	/**
	 * Test case for the "not found" scenario: a job ID is provided, but no such job exists.
	 * We expect an HTTP 404 Not Found status and an empty response body.
	 */
	@Test
	void getJobStatus_whenJobDoesNotExist_returnsNotFound() throws Exception {
		// --- Arrange ---
		UUID nonExistentJobId = UUID.randomUUID();

		// Define the behavior of the mocked service to return an empty Optional
		when(jobService.findJob(nonExistentJobId)).thenReturn(Optional.empty());

		// --- Act & Assert ---
		mockMvc.perform(get("/jobs/{jobId}", nonExistentJobId))
				.andExpect(status().isNotFound())
				.andExpect(content().string("")); // Body should be empty for a 404
	}

	// --- Tests for POST /url ---

	@Test
	void submitUrl_whenRequestIsValid_returnsAccepted() throws Exception {
		// --- Arrange ---
		UUID newJobId = UUID.randomUUID();
		long budgetMs = 5000L;
		String requestJson = """
				{
				    "url": "http://example.com/puzzle.txt",
				    "budgetMs": %d
				}
				""".formatted(budgetMs);

		// Mock the service layer call
		when(jobService.submitUrlJob(any(URI.class), eq(budgetMs))).thenReturn(newJobId);

		// --- Act & Assert ---
		mockMvc.perform(post("/jobs/url")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.jobId").value(newJobId.toString()))
				.andExpect(jsonPath("$.status").value("QUEUED"));
	}

	@Test
	void submitUrl_whenBudgetIsNegative_returnsBadRequest() throws Exception {
		// --- Arrange ---
		String requestJson = """
				{
				    "url": "http://example.com/puzzle.txt",
				    "budgetMs": -1
				}
				""";

		// --- Act & Assert ---
		// We expect a 400 Bad Request due to the @PositiveOrZero validation on the DTO.
		mockMvc.perform(post("/jobs/url")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest());
	}
	// --- Tests for POST /puzzle ---

	@Test
	void submitPuzzle_whenRequestIsValid_returnsAccepted() throws Exception {
		// --- Arrange ---
		UUID newJobId = UUID.randomUUID();
		long budgetMs = 10000L;
		String requestJson = """
				{
				    "puzzle": {
				        "rows": [[1, 1], [3]],
				        "cols": [[2], [1], [2]]
				    },
				    "budgetMs": %d
				}
				""".formatted(budgetMs);

		// Mock the service layer call
		when(jobService.submitPuzzleJob(any(), eq(budgetMs))).thenReturn(newJobId);

		// --- Act & Assert ---
		mockMvc.perform(post("/jobs/puzzle")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.jobId").value(newJobId.toString()))
				.andExpect(jsonPath("$.status").value("QUEUED"));
	}

	@Test
	void submitPuzzle_whenBudgetIsNegative_returnsBadRequest() throws Exception {
		// --- Arrange ---
		String requestJson = """
				{
				    "puzzle": {
				        "rows": [[1]],
				        "cols": [[1]]
				    },
				    "budgetMs": -100
				}
				""";

		// --- Act & Assert ---
		// We expect a 400 Bad Request due to the @PositiveOrZero validation on the DTO.
		mockMvc.perform(post("/jobs/puzzle")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest());
	}
}

