package com.liadkoren.nonogram.app.api.dto;

import java.util.UUID;


public record JobSubmittedResponse(UUID jobId, String status) {

}
