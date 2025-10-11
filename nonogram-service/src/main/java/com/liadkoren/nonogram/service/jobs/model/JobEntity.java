package com.liadkoren.nonogram.service.jobs.model;

import com.liadkoren.nonogram.core.model.Puzzle;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
public class JobEntity {

	@Id
	@GeneratedValue
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private JobStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private JobSourceType sourceType; // URL | PUZZLE

	@Column(length = 1024)
	private String sourceUrl;

	@Type(JsonType.class)                 // serialize/deserialize via Jackson
	@Column(name = "puzzle", columnDefinition = "clob") // H2: CLOB under the hood
	private Puzzle puzzle;

	@Type(JsonType.class)
	@Column(name = "result_grid", columnDefinition = "clob")
	private boolean[][] resultGrid;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant startedAt;
	private Instant completedAt;

	@Column(nullable = false)
	private long budgetMs;

	private Long solutionTimeMs;

	@Column(length = 2000)
	private String errorMessage;

	// -------- factories (keep invariants centralized) --------
	public static JobEntity forUrl(URI url, long budgetMs) {
		if (url == null) throw new IllegalArgumentException("url is required");
		requireBudget(budgetMs);
		JobEntity j = baseNew(budgetMs);
		j.sourceType = JobSourceType.URL;
		j.sourceUrl = url.toString();
		return j;
	}

	public static JobEntity forPuzzle(Puzzle puzzle, long budgetMs) {
		if (puzzle == null) throw new IllegalArgumentException("puzzle is required");
		requireBudget(budgetMs);
		JobEntity j = baseNew(budgetMs);
		j.sourceType = JobSourceType.INLINE_PUZZLE;
		j.puzzle = puzzle;
		return j;
	}

	private static JobEntity baseNew(long budgetMs) {
		JobEntity j = new JobEntity();
		j.status = JobStatus.QUEUED;
		j.budgetMs = budgetMs;
		return j;
	}

	private static void requireBudget(long ms) {
		if (ms < 0) throw new IllegalArgumentException("budgetMs must be >= 0");
	}

	/** safe setter that defensively copies the 2D array */
	public void setResultGrid(boolean[][] grid) {
		if (grid == null) { this.resultGrid = null; return; }
		boolean[][] copy = new boolean[grid.length][];
		for (int i = 0; i < grid.length; i++) {
			copy[i] = java.util.Arrays.copyOf(grid[i], grid[i].length);
		}
		this.resultGrid = copy;
	}

	/** defensive copy on read to avoid external mutation */
	public boolean[][] getResultGrid() {
		if (this.resultGrid == null) return null;
		boolean[][] copy = new boolean[this.resultGrid.length][];
		for (int i = 0; i < this.resultGrid.length; i++) {
			copy[i] = java.util.Arrays.copyOf(this.resultGrid[i], this.resultGrid[i].length);
		}
		return copy;
	}

	public enum JobSourceType {URL, INLINE_PUZZLE}

	public enum JobStatus {QUEUED, RUNNING, SUCCESS, FAIL}
}