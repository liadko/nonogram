package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.Job;
import com.liadkoren.nonogram.service.jobs.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

}
