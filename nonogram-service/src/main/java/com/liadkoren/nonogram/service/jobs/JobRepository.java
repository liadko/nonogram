package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {

}
