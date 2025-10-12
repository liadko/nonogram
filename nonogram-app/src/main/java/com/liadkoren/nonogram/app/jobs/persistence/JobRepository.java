package com.liadkoren.nonogram.app.jobs.persistence;

import com.liadkoren.nonogram.app.jobs.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {

}
