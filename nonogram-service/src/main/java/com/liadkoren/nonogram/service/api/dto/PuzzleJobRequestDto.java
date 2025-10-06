package com.liadkoren.nonogram.service.api.dto;

import com.liadkoren.nonogram.core.model.Puzzle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for creating a nonogram solving job from a provided puzzle.
 *
 * @param puzzle The nonogram puzzle to solve
 * @param budgetMs Maximum time budget in milliseconds for solving the puzzle
 */
public record PuzzleJobRequestDto(
    @Valid Puzzle puzzle,
    @PositiveOrZero long budgetMs
) {}
