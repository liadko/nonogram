package com.liadkoren.nonogram.service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for creating a nonogram solving job from a URL.
 *
 * @param url The URL to scrape the nonogram puzzle from
 * @param budgetMs Maximum time budget in milliseconds for solving the puzzle
 */
public record UrlJobRequestDto(
    @NotBlank String url,
    long budgetMs
) {}
