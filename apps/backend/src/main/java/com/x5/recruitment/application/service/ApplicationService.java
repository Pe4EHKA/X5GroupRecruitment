package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.*;
import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing applications and candidate workflow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final VacancyRepository vacancyRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    /**
     * Create new application (with candidate deduplication).
     */
    public ApplicationDto createApplication(CreateApplicationRequest request) {
        log.info("Creating application for email: {}", request.getEmail());
        
        // Find or create candidate
        Candidate candidate = candidateRepository.findByEmail(request.getEmail())
            .orElseGet(() -> {
                Candidate newCandidate = Candidate.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .additionalInfo(request.getAdditionalInfo())
                    .accessToken(UUID.randomUUID().toString())
                    .build();
                return candidateRepository.save(newCandidate);
            });

        // Find vacancy
        Vacancy vacancy = vacancyRepository.findById(request.getVacancyId())
            .orElseThrow(() -> new IllegalArgumentException("Vacancy not found: " + request.getVacancyId()));

        // Check for duplicate application
        if (applicationRepository.findByCandidateIdAndVacancyId(candidate.getId(), vacancy.getId()).isPresent()) {
            throw new IllegalStateException("Application already exists for this candidate and vacancy");
        }

        // Create application
        Application application = Application.builder()
            .candidate(candidate)
            .vacancy(vacancy)
            .status(ApplicationStatus.NEW)
            .coverLetter(request.getCoverLetter())
            .build();

        application = applicationRepository.save(application);

        // Create notification
        createNotification(candidate, application, NotificationType.APPLICATION_RECEIVED,
            "Application Received",
            "Your application for " + vacancy.getTitle() + " has been received and is under review.");

        log.info("Created application ID: {} for candidate: {}", application.getId(), candidate.getEmail());
        
        return mapToDto(application);
    }

    /**
     * Get applications for recruiter dashboard.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsForRecruiter(ApplicationStatus status, Pageable pageable) {
        Page<Application> applications = status != null 
            ? applicationRepository.findByStatus(status, pageable)
            : applicationRepository.findAll(pageable);
        
        return applications.map(this::mapToDto);
    }

    /**
     * Change application status.
     */
    public ApplicationDto changeStatus(Long applicationId, ChangeStatusRequest request, User user) {
        log.info("Changing status for application {} to {}", applicationId, request.getNewStatus());
        
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        ApplicationStatus oldStatus = application.getStatus();
        application.changeStatus(request.getNewStatus(), user, request.getComment());
        
        application = applicationRepository.save(application);

        // Create notification for status change
        createNotification(application.getCandidate(), application, NotificationType.STATUS_CHANGED,
            "Application Status Updated",
            "Your application status has been updated to: " + request.getNewStatus());

        log.info("Changed application {} status from {} to {}", applicationId, oldStatus, request.getNewStatus());
        
        return mapToDto(application);
    }

    /**
     * Get applications pending HM review.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsPendingHmReview(Long hmId, Pageable pageable) {
        return applicationRepository.findByStatusAndHiringManagerId(
            ApplicationStatus.PENDING_HM_REVIEW, hmId, pageable)
            .map(this::mapToDto);
    }

    /**
     * Get application by ID with full details.
     */
    @Transactional(readOnly = true)
    public ApplicationDetailDto getApplication(Long id) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
        return mapToDetailDto(application);
    }

    /**
     * Count applications by status.
     */
    public long countByStatus(ApplicationStatus status) {
        return applicationRepository.countByStatus(status);
    }

    /**
     * Count all applications.
     */
    public long countAll() {
        return applicationRepository.count();
    }

    /**
     * Create notification for candidate.
     */
    private void createNotification(Candidate candidate, Application application, 
                                   NotificationType type, String subject, String body) {
        Notification notification = Notification.builder()
            .candidate(candidate)
            .application(application)
            .type(type)
            .subject(subject)
            .body(body)
            .build();
        
        notificationRepository.save(notification);
        log.debug("Created notification for candidate: {}", candidate.getEmail());
    }

    /**
     * Map Application entity to DTO.
     */
    private ApplicationDto mapToDto(Application application) {
        Candidate candidate = application.getCandidate();
        User assignedRecruiter = application.getAssignedRecruiter();
        
        // Create nested CandidateDto
        CandidateDto candidateDto = CandidateDto.builder()
            .id(candidate.getId())
            .fullName(candidate.getFullName())
            .email(candidate.getEmail())
            .phone(candidate.getPhone())
            .university(candidate.getUniversity())
            .course(candidate.getCourse())
            .statusToken(candidate.getAccessToken())
            .build();
        
        return ApplicationDto.builder()
            .id(application.getId())
            // New nested structure
            .candidate(candidateDto)
            // Backward compatibility
            .candidateId(candidate.getId())
            .candidateName(candidate.getFullName())
            .candidateEmail(candidate.getEmail())
            .vacancyId(application.getVacancy().getId())
            .vacancyTitle(application.getVacancy().getTitle())
            .status(application.getStatus())
            .coverLetter(application.getCoverLetter())
            .notes(application.getNotes())
            .recruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .recruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .assignedRecruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .assignedRecruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .screeningScore(application.getScreeningScore())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .statusChangedAt(application.getUpdatedAt())
            .build();
    }
    
    /**
     * Map Application entity to DetailDto with related entities.
     */
    private ApplicationDetailDto mapToDetailDto(Application application) {
        Candidate candidate = application.getCandidate();
        User assignedRecruiter = application.getAssignedRecruiter();
        
        // Create nested CandidateDto
        CandidateDto candidateDto = CandidateDto.builder()
            .id(candidate.getId())
            .fullName(candidate.getFullName())
            .email(candidate.getEmail())
            .phone(candidate.getPhone())
            .university(candidate.getUniversity())
            .course(candidate.getCourse())
            .statusToken(candidate.getAccessToken())
            .build();
        
        // Map status history
        List<StatusHistoryDto> statusHistoryDtos = application.getStatusHistory().stream()
            .map(sh -> StatusHistoryDto.builder()
                .id(sh.getId())
                .status(sh.getToStatus())
                .comment(sh.getComment())
                .changedBy(sh.getChangedBy() != null ? sh.getChangedBy().getFullName() : "System")
                .changedAt(sh.getChangedAt())
                .build())
            .toList();
        
        // Map feedbacks
        List<FeedbackDto> feedbackDtos = application.getFeedbacks().stream()
            .map(fb -> FeedbackDto.builder()
                .id(fb.getId())
                .hmName(fb.getAuthor().getFullName())
                .decision("APPROVE") // TODO: add decision field to Feedback entity
                .overallAssessment(fb.getGeneralComments())
                .strengths(fb.getStrengths())
                .areasForGrowth(fb.getWeaknesses())
                .recommendations(fb.getRecommendation())
                .talentPool(false) // TODO: add talentPool field if needed
                .createdAt(fb.getCreatedAt())
                .build())
            .toList();
        
        // Map preferences
        List<ApplicationPreferenceDto> preferenceDtos = application.getPreferences().stream()
            .map(pref -> ApplicationPreferenceDto.builder()
                .preferenceOrder(pref.getRank())
                .preferredPosition(pref.getRawValue())
                .preferredLocation(pref.getVacancy() != null ? pref.getVacancy().getLocation() : null)
                .build())
            .toList();
        
        // Map interviews (if any)
        List<InterviewDto> interviewDtos = application.getInterviews().stream()
            .map(iv -> InterviewDto.builder()
                .id(iv.getId())
                .interviewType(iv.getCompleted() ? "Completed" : "Scheduled")
                .scheduledAt(iv.getScheduledAt())
                .completedAt(iv.getCompletedAt())
                .interviewerName(iv.getInterviewer() != null ? iv.getInterviewer().getFullName() : null)
                .notes(iv.getNotes())
                .build())
            .toList();
        
        return ApplicationDetailDto.builder()
            .id(application.getId())
            // New nested structure
            .candidate(candidateDto)
            // Backward compatibility
            .candidateId(candidate.getId())
            .candidateName(candidate.getFullName())
            .candidateEmail(candidate.getEmail())
            .vacancyId(application.getVacancy().getId())
            .vacancyTitle(application.getVacancy().getTitle())
            .status(application.getStatus())
            .coverLetter(application.getCoverLetter())
            .notes(application.getNotes())
            .recruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .recruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .assignedRecruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .assignedRecruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .screeningScore(application.getScreeningScore())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .statusChangedAt(application.getUpdatedAt())
            // Detail information
            .preferences(preferenceDtos)
            .statusHistory(statusHistoryDtos)
            .feedbacks(feedbackDtos)
            .interviews(interviewDtos)
            .build();
    }
    
    /**
     * Get applications with advanced filters for HR dashboard.
     * Supports filtering by status, vacancy, date range, and search.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsWithFilters(
            List<ApplicationStatus> statuses,
            Long vacancyId,
            LocalDate dateFrom,
            LocalDate dateTo,
            String search,
            Pageable pageable) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Application> query = cb.createQuery(Application.class);
        Root<Application> application = query.from(Application.class);
        
        // Fetch joins to avoid N+1 queries
        application.fetch("candidate", JoinType.LEFT);
        application.fetch("vacancy", JoinType.LEFT);
        application.fetch("assignedRecruiter", JoinType.LEFT);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Status filter
        if (statuses != null && !statuses.isEmpty()) {
            predicates.add(application.get("status").in(statuses));
        }
        
        // Vacancy filter
        if (vacancyId != null) {
            predicates.add(cb.equal(application.get("vacancy").get("id"), vacancyId));
        }
        
        // Date range filter
        if (dateFrom != null) {
            LocalDateTime startOfDay = dateFrom.atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(application.get("createdAt"), startOfDay));
        }
        if (dateTo != null) {
            LocalDateTime endOfDay = dateTo.atTime(23, 59, 59);
            predicates.add(cb.lessThanOrEqualTo(application.get("createdAt"), endOfDay));
        }
        
        // Search filter (candidate name, email, phone)
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            Join<Application, Candidate> candidate = application.join("candidate", JoinType.LEFT);
            
            Predicate namePredicate = cb.or(
                cb.like(cb.lower(candidate.get("firstName")), searchPattern),
                cb.like(cb.lower(candidate.get("lastName")), searchPattern)
            );
            Predicate emailPredicate = cb.like(cb.lower(candidate.get("email")), searchPattern);
            Predicate phonePredicate = cb.like(cb.lower(candidate.get("phone")), searchPattern);
            
            predicates.add(cb.or(namePredicate, emailPredicate, phonePredicate));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(application.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(application.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }
        
        // Execute query with pagination
        TypedQuery<Application> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Application> applications = typedQuery.getResultList();
        
        // Count query for total elements
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Application> countRoot = countQuery.from(Application.class);
        countQuery.select(cb.count(countRoot));
        
        // Apply same WHERE predicates to count query (but no joins needed for counting)
        List<Predicate> countPredicates = new ArrayList<>();
        if (statuses != null && !statuses.isEmpty()) {
            countPredicates.add(countRoot.get("status").in(statuses));
        }
        if (vacancyId != null) {
            countPredicates.add(cb.equal(countRoot.get("vacancy").get("id"), vacancyId));
        }
        if (dateFrom != null) {
            LocalDateTime startOfDay = dateFrom.atStartOfDay();
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("createdAt"), startOfDay));
        }
        if (dateTo != null) {
            LocalDateTime endOfDay = dateTo.atTime(23, 59, 59);
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("createdAt"), endOfDay));
        }
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            Join<Application, Candidate> countCandidate = countRoot.join("candidate", JoinType.INNER);
            
            Predicate namePredicate = cb.or(
                cb.like(cb.lower(countCandidate.get("firstName")), searchPattern),
                cb.like(cb.lower(countCandidate.get("lastName")), searchPattern)
            );
            Predicate emailPredicate = cb.like(cb.lower(countCandidate.get("email")), searchPattern);
            Predicate phonePredicate = cb.like(cb.lower(countCandidate.get("phone")), searchPattern);
            
            countPredicates.add(cb.or(namePredicate, emailPredicate, phonePredicate));
        }
        
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        // Map to DTOs
        List<ApplicationDto> dtos = applications.stream()
            .map(this::mapToDto)
            .toList();
        
        return new PageImpl<>(dtos, pageable, total);
    }
}
