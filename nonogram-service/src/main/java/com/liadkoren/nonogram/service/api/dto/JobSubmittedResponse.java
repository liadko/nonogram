package com.liadkoren.nonogram.service.api.dto;

import com.liadkoren.nonogram.service.jobs.model.JobEntity;

import java.util.UUID;


public record JobSubmittedResponse(UUID jobId, String status) {

}
