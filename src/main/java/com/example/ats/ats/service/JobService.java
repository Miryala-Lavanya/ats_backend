package com.example.ats.service;

import com.example.ats.model.Job;
import com.example.ats.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // ✅ Create a new job
    public Job createJob(Job job) {
        job.setPostedDate(LocalDate.now());
        return jobRepository.save(job);
    }

    // ✅ Get all jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // ✅ Get a single job by ID
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + id));
    }

    // 🔁 Update an existing job
    public Job updateJob(Long id, Job jobDetails) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + id));

        existingJob.setTitle(jobDetails.getTitle());
        existingJob.setDescription(jobDetails.getDescription());
        existingJob.setCompany(jobDetails.getCompany());
        existingJob.setLocation(jobDetails.getLocation());
        existingJob.setSalary(jobDetails.getSalary());
        existingJob.setPostedDate(LocalDate.now()); // Update posted date if needed

        return jobRepository.save(existingJob);
    }

    // ❌ Delete a job by ID
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + id));
        jobRepository.delete(job);
    }
}