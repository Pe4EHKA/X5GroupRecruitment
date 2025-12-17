package com.x5.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for X5 Internship Recruitment System.
 * 
 * This system automates the recruitment process for internship programs,
 * providing functionality for:
 * - Candidate application management
 * - Recruiter workflow automation
 * - Hiring Manager decision making
 * - Automated notifications and feedback
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class RecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentApplication.class, args);
    }
}
