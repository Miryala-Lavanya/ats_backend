package com.example.ats.controller;

import com.example.ats.model.JobApplication;
import com.example.ats.model.User;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.JobApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @SuppressWarnings("unused")
	@Autowired
    private UserRepository userRepository;

    @Autowired
    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    // Admin: View all applications
    @GetMapping("/admin/applications")
    public ResponseEntity<?> getAllApplicationsForAdmin(Principal principal) {
        String email = principal.getName();
        if (!service.isAdmin(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access Denied: Not an admin."));
        }
        List<Map<String, Object>> applications = service.getAllApplicationsWithJobInfo();
        return ResponseEntity.ok(applications);
    }

    // Apply to a job
    @PostMapping("/apply/{jobId}")
    public ResponseEntity<?> applyToJob(
            @PathVariable Long jobId,
            Principal principal,
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("coverLetter") MultipartFile coverLetter) {

        String email = principal.getName();
        JobApplication application = service.applyToJob(jobId, email, resume, coverLetter);
        return ResponseEntity.ok(application);
    }

    // View my applications
    @GetMapping("/my")
    public ResponseEntity<?> myApplications(Principal principal) {
        String email = principal.getName();
        Map<String, Object> response = service.getApplicationsByUserWithCount(email);
        return ResponseEntity.ok(response);
    }

    // ✅ Update application status (with email notification via service)
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam String status,
            Principal principal) {

        String requesterEmail = principal.getName();

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if ("WITHDRAWN".equalsIgnoreCase(application.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Cannot update a withdrawn application."));
        }

        // Call service method to handle validation + email
        JobApplication updated = service.updateApplicationStatus(applicationId, status, requesterEmail);
        return ResponseEntity.ok(updated);
    }

    // View applications for a job
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicationsWithCount(@PathVariable Long jobId, Principal principal) {
        String email = principal.getName();
        Map<String, Object> response = service.getApplicationsForJobWithCount(jobId, email);
        return ResponseEntity.ok(response);
    }

    // View application status
    @GetMapping("/{applicationId}/status")
    public ResponseEntity<?> getApplicationStatus(@PathVariable Long applicationId, Principal principal) {
        String email = principal.getName();
        JobApplication application = service.getApplicationByIdAndEmail(applicationId, email);
        return ResponseEntity.ok(Collections.singletonMap("status", application.getStatus()));
    }

    // Download resume
    @GetMapping("/resume/{applicationId}")
    public ResponseEntity<?> downloadResume(@PathVariable Long applicationId, Principal principal) {
        String email = principal.getName();

        if (!service.canAccessApplication(applicationId, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Unauthorized access to resume."));
        }

        byte[] resume = service.getResumeByApplicationId(applicationId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resume);
    }

    // Download cover letter
    @GetMapping("/cover-letter/{applicationId}")
    public ResponseEntity<?> downloadCoverLetter(@PathVariable Long applicationId, Principal principal) {
        String email = principal.getName();

        if (!service.canAccessApplication(applicationId, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] coverLetter = service.getCoverLetterByApplicationId(applicationId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cover_letter.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(coverLetter.length)
                .body(coverLetter);
    }

    // Withdraw application
    @PutMapping("/{applicationId}/withdraw")
    public ResponseEntity<?> withdrawApplication(@PathVariable Long applicationId, Principal principal) {
        String email = principal.getName();
        String message = service.withdrawApplication(applicationId, email);
        return ResponseEntity.ok(Collections.singletonMap("message", message));
    }
}





