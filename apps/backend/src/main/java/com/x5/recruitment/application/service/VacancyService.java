package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.VacancyRequest;
import com.x5.recruitment.api.dto.VacancyResponse;
import com.x5.recruitment.domain.model.Vacancy;
import com.x5.recruitment.domain.repository.VacancyRepository;
import com.x5.recruitment.infrastructure.exception.ConflictException;
import com.x5.recruitment.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    @Transactional(readOnly = true)
    public List<VacancyResponse> getAllVacancies() {
        return vacancyRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public VacancyResponse getVacancy(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found with id: " + id));

        return toResponse(vacancy);
    }

    @Transactional
    public VacancyResponse createVacancy(VacancyRequest request) {
        String code = resolveCode(request.getCode(), request.getTitle());

        if (vacancyRepository.existsByCode(code)) {
            throw new ConflictException("Vacancy code already exists: " + code);
        }

        Vacancy vacancy = Vacancy.builder()
            .code(code)
            .title(request.getTitle())
            .description(request.getDescription())
            .department(request.getDepartment())
            .location(request.getLocation())
            .positionsAvailable(request.getPositionsAvailable())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .active(request.getActive() == null || request.getActive())
            .build();

        vacancy = vacancyRepository.save(vacancy);
        log.info("Created vacancy {} ({})", vacancy.getTitle(), vacancy.getCode());
        return toResponse(vacancy);
    }

    @Transactional
    public VacancyResponse updateVacancy(Long id, VacancyRequest request) {
        Vacancy vacancy = vacancyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found with id: " + id));

        if (StringUtils.hasText(request.getCode()) && !request.getCode().equalsIgnoreCase(vacancy.getCode())) {
            String normalizedCode = request.getCode().trim();
            if (vacancyRepository.existsByCode(normalizedCode)) {
                throw new ConflictException("Vacancy code already exists: " + normalizedCode);
            }
            vacancy.setCode(normalizedCode);
        }

        if (StringUtils.hasText(request.getTitle())) {
            vacancy.setTitle(request.getTitle());
        }
        vacancy.setDescription(request.getDescription());
        vacancy.setDepartment(request.getDepartment());
        vacancy.setLocation(request.getLocation());
        vacancy.setPositionsAvailable(request.getPositionsAvailable());
        vacancy.setStartDate(request.getStartDate());
        vacancy.setEndDate(request.getEndDate());
        if (request.getActive() != null) {
            vacancy.setActive(request.getActive());
        }

        vacancy = vacancyRepository.save(vacancy);
        log.info("Updated vacancy {} ({})", vacancy.getTitle(), vacancy.getCode());
        return toResponse(vacancy);
    }

    @Transactional
    public Vacancy findOrCreateByTitle(String title) {
        return vacancyRepository.findByTitleIgnoreCase(title)
            .orElseGet(() -> {
                String code = generateUniqueCode(title);
                Vacancy vacancy = Vacancy.builder()
                    .title(title)
                    .code(code)
                    .active(true)
                    .allowUnmapped(true)
                    .build();
                Vacancy saved = vacancyRepository.save(vacancy);
                log.info("Auto-created vacancy {} ({})", saved.getTitle(), saved.getCode());
                return saved;
            });
    }

    private String resolveCode(String providedCode, String title) {
        if (StringUtils.hasText(providedCode)) {
            return providedCode.trim();
        }
        return generateUniqueCode(title);
    }

    private String generateUniqueCode(String title) {
        String base = title == null ? "VACANCY" : title.toUpperCase().replaceAll("[^A-Z0-9]+", "_");
        base = base.replaceAll("_+", "_").replaceAll("^_", "").replaceAll("_$", "");
        if (!StringUtils.hasText(base)) {
            base = "VACANCY";
        }

        String code = base;
        int counter = 1;
        while (vacancyRepository.existsByCode(code)) {
            code = base + "_" + counter++;
            if (counter > 10) {
                code = base + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                break;
            }
        }
        return code;
    }

    private VacancyResponse toResponse(Vacancy vacancy) {
        return VacancyResponse.builder()
            .id(vacancy.getId())
            .code(vacancy.getCode())
            .title(vacancy.getTitle())
            .description(vacancy.getDescription())
            .department(vacancy.getDepartment())
            .location(vacancy.getLocation())
            .positionsAvailable(vacancy.getPositionsAvailable())
            .startDate(vacancy.getStartDate())
            .endDate(vacancy.getEndDate())
            .active(vacancy.getActive())
            .createdAt(vacancy.getCreatedAt())
            .updatedAt(vacancy.getUpdatedAt())
            .build();
    }
}
