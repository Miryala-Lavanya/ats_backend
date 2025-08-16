package com.example.ats.service;

import com.example.ats.dto.ApplicationResponseDTO;
import com.example.ats.model.Job;
import com.example.ats.model.JobApplication;
import com.example.ats.model.User;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class JobApplicationService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public JobApplicationService(JobRepository jobRepo,
                                 JobApplicationRepository appRepo,
                                 UserRepository userRepo,
                                 NotificationService notificationService) {
        this.jobRepository = jobRepo;
        this.applicationRepository = appRepo;
        this.userRepository = userRepo;
        this.notificationService = notificationService;
    }

    // 1️⃣ Apply to a job
    public JobApplication applyToJob(Long jobId, String applicantEmail,
                                     MultipartFile resume, MultipartFile coverLetterFile) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (applicationRepository.existsByJobIdAndApplicantEmail(jobId, applicantEmail)) {
            throw new RuntimeException("You have already applied to this job.");
        }

        Path uploadPath = Paths.get("uploads");
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            byte[] resumeBytes = resume.getBytes();
            byte[] coverLetterBytes = coverLetterFile.getBytes();

            String resumeFileName = System.currentTimeMillis() + "_resume_" + resume.getOriginalFilename();
            Files.write(uploadPath.resolve(resumeFileName), resumeBytes);

            String coverLetterFileName = System.currentTimeMillis() + "_coverLetter_" + coverLetterFile.getOriginalFilename();
            Files.write(uploadPath.resolve(coverLetterFileName), coverLetterBytes);

            JobApplication application = new JobApplication();
            application.setJob(job);
            application.setApplicant(applicant);
            application.setAppliedDate(LocalDate.now());
            application.setStatus("PENDING");
            application.setResume(resumeBytes);
            application.setCoverLetter(coverLetterBytes);

            return applicationRepository.save(application);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store files", e);
        }
    }

    // 2️⃣ View applications by user with count
    public Map<String, Object> getApplicationsByUserWithCount(String email) {
        List<JobApplication> applications = applicationRepository.findByApplicantEmail(email);
        return Map.of(
                "count", applications.size(),
                "applications", applications
        );
    }

    // 3️⃣ Update application status + notify applicant on approval/rejection
    public JobApplication updateApplicationStatus(Long applicationId, String status, String requesterEmail) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ADMIN".equalsIgnoreCase(requester.getRole()) &&
            !"RECRUITER".equalsIgnoreCase(requester.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only ADMIN or RECRUITER can update application status.");
        }

        String newStatus = status.toUpperCase();
        String oldStatus = application.getStatus();

        // Only update and send notification if status actually changes
        if (!newStatus.equals(oldStatus)) {
            application.setStatus(newStatus);
            JobApplication updatedApp = applicationRepository.save(application);

            System.out.println("📤 Application ID: " + applicationId + " | New status: " + newStatus);

            if ("APPROVED".equalsIgnoreCase(newStatus) || "REJECTED".equalsIgnoreCase(newStatus)) {
                notificationService.sendApplicationStatusNotification(
                        application.getApplicant().getEmail(),
                        application.getApplicant().getUsername(),
                        application.getJob().getTitle(),
                        newStatus
                );
            }
            return updatedApp;
        } else {
            // No status change, no email sent, just return existing application
            System.out.println("⚠️ Status for Application ID: " + applicationId + " is(" + oldStatus + ").");
            return application;
        }
    }

    // 4️⃣ View applications for specific job with count (Admin only)
    public Map<String, Object> getApplicationsForJobWithCount(Long jobId, String requesterEmail) {
        jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ADMIN".equalsIgnoreCase(requester.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to view job applications.");
        }

        List<JobApplication> applications = applicationRepository.findByJobId(jobId);

        List<ApplicationResponseDTO> dtoList = applications.stream()
                .map(app -> new ApplicationResponseDTO(
                        app.getApplicant().getUsername(),
                        app.getJob().getTitle(),
                        app.getStatus(),
                        app.getResumePath(),
                        app.getCoverLetterPath()
                ))
                .toList();

        return Map.of(
                "count", dtoList.size(),
                "applications", dtoList
        );
    }

    // 5️⃣ Get application by ID and email (for applicant access control)
    public JobApplication getApplicationByIdAndEmail(Long applicationId, String email) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getApplicant().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this application.");
        }

        return app;
    }

    // 6️⃣ Get application by ID (for admin or recruiter access)
    public JobApplication getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    // 7️⃣ Resume bytes for download
    public byte[] getResumeByApplicationId(Long applicationId) {
        JobApplication app = getApplicationById(applicationId);
        if (app.getResume() == null) {
            throw new RuntimeException("Resume not found.");
        }
        return app.getResume();
    }

    // 8️⃣ Cover letter bytes for download
    public byte[] getCoverLetterByApplicationId(Long applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        byte[] coverLetter = application.getCoverLetter();
        if (coverLetter == null || coverLetter.length == 0) {
            throw new RuntimeException("Cover letter not found.");
        }

        return coverLetter;
    }

    // 9️⃣ Withdraw application
    public String withdrawApplication(Long applicationId, String email) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getApplicant().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized to withdraw this application.");
        }

        application.setStatus("WITHDRAWN");
        applicationRepository.save(application);
        return "Application withdrawn successfully.";
    }

    // 🔟 Check if user is admin
    public boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    // 11️⃣ Admin - get all applications (no filtering)
    public List<Map<String, Object>> getAllApplicationsWithJobInfo() {
        List<JobApplication> applications = applicationRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (JobApplication app : applications) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("status", app.getStatus());
            map.put("applicantEmail", app.getApplicant().getEmail());
            map.put("jobTitle", app.getJob().getTitle());
            result.add(map);
        }
        return result;
    }

    // 12️⃣ Helper - access control
    public boolean canAccessApplication(Long applicationId, String email) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRole().equalsIgnoreCase("ADMIN") ||
               user.getRole().equalsIgnoreCase("RECRUITER") ||
               app.getApplicant().getEmail().equalsIgnoreCase(email);
    }
}
