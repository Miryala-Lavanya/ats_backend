package com.example.ats.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate appliedDate;

    private String status; // e.g., "PENDING", "REVIEWED", "REJECTED"

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "applicant_id")
    private User applicant;
    
    
    @Lob
    @Column(name = "resume", columnDefinition = "LONGBLOB")
    private byte[] resume;

    @Lob
    @Column(name = "cover_letter", columnDefinition = "LONGBLOB")
    private byte[] coverLetter;

    @Column(name = "resume_path")
    private String resumePath;

    @Column(name = "cover_letter_path")
    private String coverLetterPath;

}
