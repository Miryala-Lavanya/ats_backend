package com.example.ats.repository;

import com.example.ats.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // 1️⃣ Existing method - Get applications by applicant email
    List<JobApplication> findByApplicantEmail(String email);

    // 2️⃣ Enhancement 1 - Prevent duplicate applications
    boolean existsByJobIdAndApplicantEmail(Long id, String email);

    // 3️⃣ Enhancement 2 - Get applications for a specific job (for recruiter)
    List<JobApplication> findByJobId(Long id);
}

