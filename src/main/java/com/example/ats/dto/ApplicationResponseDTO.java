package com.example.ats.dto;

public class ApplicationResponseDTO {
    private String applicantName;
    private String jobTitle;
    private String status;
    private String resumePath;
    private String coverLetterPath;

    // Constructors
    public ApplicationResponseDTO() {}

    public ApplicationResponseDTO(String applicantName, String jobTitle, String status, String resumePath, String coverLetterPath) {
        this.applicantName = applicantName;
        this.jobTitle = jobTitle;
        this.status = status;
        this.resumePath = resumePath;
        this.coverLetterPath = coverLetterPath;
    }

    // Getters and Setters
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }

    public String getCoverLetterPath() { return coverLetterPath; }
    public void setCoverLetterPath(String coverLetterPath) { this.coverLetterPath = coverLetterPath; }
}

