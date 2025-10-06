package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class JobRepositorySmokeTest {
	@Autowired
	JobRepository repo;

	@Test
	void persists_boolean_matrix_json() {
		var j = JobEntity.forPuzzle(new Puzzle(Collections.emptyList(), Collections.emptyList()), 1000); // or forUrl if puzzle nullable
		boolean[][] grid = {{true, false}, {false, true}};
		j.setResultGrid(grid);
		repo.saveAndFlush(j);

		var got = repo.findById(j.getId()).orElseThrow();
		assertTrue(java.util.Arrays.deepEquals(grid, got.getResultGrid()));
	}
}