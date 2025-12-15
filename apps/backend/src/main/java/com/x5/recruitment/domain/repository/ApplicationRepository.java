package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.Application;
import com.x5.recruitment.domain.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Application entity.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    Optional<Application> findByCandidateIdAndVacancyId(Long candidateId, Long vacancyId);
    
    List<Application> findByCandidateId(Long candidateId);
    
    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);
    
    Page<Application> findByAssignedRecruiterId(Long recruiterId, Pageable pageable);
    
    @Query("SELECT a FROM Application a WHERE a.status = :status AND a.vacancy.hiringManager.id = :hmId")
    Page<Application> findByStatusAndHiringManagerId(@Param("status") ApplicationStatus status, 
                                                       @Param("hmId") Long hmId, 
                                                       Pageable pageable);
    
    @Query("SELECT a FROM Application a WHERE a.status IN :statuses")
    List<Application> findByStatusIn(@Param("statuses") List<ApplicationStatus> statuses);
    
    long countByStatus(ApplicationStatus status);
}
