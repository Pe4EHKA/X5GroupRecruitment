package com.x5.recruitment.application.service;

import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.*;
import com.x5.recruitment.infrastructure.util.PhoneNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for importing candidates and applications from XLSX files.
 * Implements header-based mapping, validation, deduplication, and normalization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class XlsxImportService {

    private final CandidateRepository candidateRepository;
    private final VacancyRepository vacancyRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationPreferenceRepository preferenceRepository;
    private final ImportBatchRepository batchRepository;
    private final ImportRowErrorRepository errorRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // Expected Excel column headers
    private static final String[] EXPECTED_HEADERS = {
        "Фамилия", "Имя", "ТГ", "Телефон", "Почта", "Резюме",
        "Первый приоритет", "Второй приоритет", "Курс", "Специальность",
        "Другая специальность", "График", "Город", "Другой город",
        "Откуда узнал", "Год рождения", "Гражданство", "ВУЗ",
        "Другой ВУЗ", "Языки", "Дата заявки"
    };

    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "Фамилия", "Имя", "Первый приоритет", "Дата заявки"
    );

    /**
     * Import applications from XLSX file.
     */
    @Transactional
    public ImportBatch importFromXlsx(MultipartFile file) throws IOException {
        log.info("Starting XLSX import from file: {}", file.getOriginalFilename());

        // Get current user
        User uploadedBy = getCurrentUser();

        // Create import batch
        ImportBatch batch = ImportBatch.builder()
            .fileName(file.getOriginalFilename())
            .uploadedBy(uploadedBy)
            .uploadedAt(LocalDateTime.now())
            .totalRows(0)
            .successRows(0)
            .failedRows(0)
            .completed(false)
            .build();
        batch = batchRepository.save(batch);

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            
            // Parse header row and create column mapping
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file has no header row");
            }

            Map<String, Integer> headerMap = parseHeaderRow(headerRow);
            validateHeaders(headerMap);

            // Process data rows
            int totalRows = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                totalRows++;
                try {
                    processRow(row, headerMap, batch, i + 1);
                    batch.incrementSuccess();
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i + 1, e.getMessage(), e);
                    batch.incrementFailure();
                    recordError(batch, i + 1, "PROCESSING_ERROR", e.getMessage(), row, headerMap);
                }
            }

            batch.setTotalRows(totalRows);
            batch.markCompleted();
            batchRepository.save(batch);

            log.info("Import completed: {} total, {} success, {} failed",
                totalRows, batch.getSuccessRows(), batch.getFailedRows());

            return batch;

        } catch (Exception e) {
            log.error("Fatal error during import: {}", e.getMessage(), e);
            batch.setCompleted(true);
            batchRepository.save(batch);
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse header row and create column name to index mapping.
     */
    private Map<String, Integer> parseHeaderRow(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerName = getCellValueAsString(cell).trim();
                headerMap.put(headerName, i);
            }
        }
        
        log.debug("Parsed headers: {}", headerMap.keySet());
        return headerMap;
    }

    /**
     * Validate that all required headers are present.
     */
    private void validateHeaders(Map<String, Integer> headerMap) {
        Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);
        missingHeaders.removeAll(headerMap.keySet());
        
        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException(
                "Missing required headers: " + String.join(", ", missingHeaders));
        }
    }

    /**
     * Process a single row from Excel.
     */
    private void processRow(Row row, Map<String, Integer> headerMap, ImportBatch batch, int rowNumber) {
        // Extract data from row
        RowData data = extractRowData(row, headerMap);

        // Validate row
        validateRow(data, batch, rowNumber);

        // Normalize data
        normalizeData(data);

        // Find or create candidate
        Candidate candidate = findOrCreateCandidate(data);

        // Find or create vacancy for first priority
        Vacancy primaryVacancy = findOrCreateVacancy(data.getPriority1());

        // Create application
        Application application = createApplication(candidate, primaryVacancy, data);

        // Create preferences
        createPreferences(application, data);

        // Create notification
        createNotification(candidate, application);

        // Create or link User account for the candidate
        createOrLinkUserAccount(candidate, batch);

        log.debug("Successfully processed row {} for candidate: {}", rowNumber, candidate.getEmail());
    }

    /**
     * Extract data from row into RowData object.
     */
    private RowData extractRowData(Row row, Map<String, Integer> headerMap) {
        RowData data = new RowData();
        
        data.setLastName(getCellValue(row, headerMap, "Фамилия"));
        data.setFirstName(getCellValue(row, headerMap, "Имя"));
        data.setTelegram(getCellValue(row, headerMap, "ТГ"));
        data.setPhone(getCellValue(row, headerMap, "Телефон"));
        data.setEmail(getCellValue(row, headerMap, "Почта"));
        data.setResume(getCellValue(row, headerMap, "Резюме"));
        data.setPriority1(getCellValue(row, headerMap, "Первый приоритет"));
        data.setPriority2(getCellValue(row, headerMap, "Второй приоритет"));
        data.setCourse(getCellValue(row, headerMap, "Курс"));
        data.setSpeciality(getCellValue(row, headerMap, "Специальность"));
        data.setOtherSpeciality(getCellValue(row, headerMap, "Другая специальность"));
        data.setSchedule(getCellValue(row, headerMap, "График"));
        data.setCity(getCellValue(row, headerMap, "Город"));
        data.setOtherCity(getCellValue(row, headerMap, "Другой город"));
        data.setSource(getCellValue(row, headerMap, "Откуда узнал"));
        data.setBirthYear(getCellValueAsInteger(row, headerMap, "Год рождения"));
        data.setCitizenship(getCellValue(row, headerMap, "Гражданство"));
        data.setUniversity(getCellValue(row, headerMap, "ВУЗ"));
        data.setOtherUniversity(getCellValue(row, headerMap, "Другой ВУЗ"));
        data.setLanguages(getCellValue(row, headerMap, "Языки"));
        data.setSubmittedAt(getCellValueAsDateTime(row, headerMap, "Дата заявки"));
        
        return data;
    }

    /**
     * Validate row data.
     */
    private void validateRow(RowData data, ImportBatch batch, int rowNumber) {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        if (isBlank(data.getFirstName()) || isBlank(data.getLastName())) {
            errors.add("Имя и Фамилия обязательны");
        }

        if (isBlank(data.getEmail()) && isBlank(data.getPhone())) {
            errors.add("Необходимо указать хотя бы Email или Телефон");
        }

        if (isBlank(data.getPriority1())) {
            errors.add("Первый приоритет обязателен");
        }

        if (data.getSubmittedAt() == null) {
            errors.add("Дата заявки обязательна");
        }

        // Validate birth year
        if (data.getBirthYear() != null) {
            int currentYear = LocalDateTime.now().getYear();
            if (data.getBirthYear() < 1900 || data.getBirthYear() > currentYear) {
                errors.add("Год рождения вне допустимого диапазона (1900-" + currentYear + ")");
            }
        }

        if (!errors.isEmpty()) {
            String errorMsg = String.join("; ", errors);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Normalize data (email, phone, telegram, languages).
     */
    private void normalizeData(RowData data) {
        // Normalize email
        if (data.getEmail() != null) {
            data.setEmail(data.getEmail().trim().toLowerCase());
        }

        // Normalize phone
        if (data.getPhone() != null) {
            String rawPhone = data.getPhone().trim();
            String phoneE164 = PhoneNormalizer.normalizeToE164(rawPhone);
            data.setPhoneE164(phoneE164);
            data.setRawPhone(rawPhone);
        }

        // Normalize telegram
        if (data.getTelegram() != null) {
            String tg = data.getTelegram().trim();
            if (tg.startsWith("@")) {
                tg = tg.substring(1);
            }
            data.setTelegram(tg);
        }

        // Parse languages
        if (data.getLanguages() != null) {
            String[] langs = data.getLanguages().split(";");
            List<String> languageList = Arrays.stream(langs)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            data.setLanguagesList(languageList);
        }
    }

    /**
     * Find existing candidate or create new one.
     */
    private Candidate findOrCreateCandidate(RowData data) {
        Candidate candidate = null;

        // Try to find by email
        if (data.getEmail() != null) {
            candidate = candidateRepository.findByEmail(data.getEmail()).orElse(null);
        }

        // Try to find by phone E.164
        if (candidate == null && data.getPhoneE164() != null) {
            candidate = candidateRepository.findByPhoneE164(data.getPhoneE164()).orElse(null);
        }

        if (candidate != null) {
            // Update existing candidate with new data (merge strategy)
            mergeCandidate(candidate, data);
            return candidateRepository.save(candidate);
        }

        // Create new candidate
        candidate = Candidate.builder()
            .firstName(data.getFirstName())
            .lastName(data.getLastName())
            .email(data.getEmail())
            .phone(data.getPhone())
            .phoneE164(data.getPhoneE164())
            .rawPhone(data.getRawPhone())
            .telegram(data.getTelegram())
            .birthYear(data.getBirthYear())
            .citizenship(data.getCitizenship())
            .university(data.getUniversity())
            .otherUniversity(data.getOtherUniversity())
            .speciality(data.getSpeciality())
            .otherSpeciality(data.getOtherSpeciality())
            .course(data.getCourse())
            .schedule(data.getSchedule())
            .city(data.getCity())
            .otherCity(data.getOtherCity())
            .source(data.getSource())
            .languages(data.getLanguagesList())
            .rawLanguages(data.getLanguages())
            .resumePath(data.getResume())
            .accessToken(UUID.randomUUID().toString())
            .build();

        return candidateRepository.save(candidate);
    }

    /**
     * Merge new data into existing candidate.
     * Strategy: update empty fields, but don't overwrite existing non-empty fields.
     */
    private void mergeCandidate(Candidate candidate, RowData data) {
        if (isBlank(candidate.getPhone()) && data.getPhone() != null) {
            candidate.setPhone(data.getPhone());
        }
        if (isBlank(candidate.getPhoneE164()) && data.getPhoneE164() != null) {
            candidate.setPhoneE164(data.getPhoneE164());
        }
        if (isBlank(candidate.getRawPhone()) && data.getRawPhone() != null) {
            candidate.setRawPhone(data.getRawPhone());
        }
        if (isBlank(candidate.getTelegram()) && data.getTelegram() != null) {
            candidate.setTelegram(data.getTelegram());
        }
        if (candidate.getBirthYear() == null && data.getBirthYear() != null) {
            candidate.setBirthYear(data.getBirthYear());
        }
        if (isBlank(candidate.getCitizenship()) && data.getCitizenship() != null) {
            candidate.setCitizenship(data.getCitizenship());
        }
        if (isBlank(candidate.getUniversity()) && data.getUniversity() != null) {
            candidate.setUniversity(data.getUniversity());
        }
        if (isBlank(candidate.getSpeciality()) && data.getSpeciality() != null) {
            candidate.setSpeciality(data.getSpeciality());
        }
        if (isBlank(candidate.getCourse()) && data.getCourse() != null) {
            candidate.setCourse(data.getCourse());
        }
        if (isBlank(candidate.getCity()) && data.getCity() != null) {
            candidate.setCity(data.getCity());
        }
        if (candidate.getLanguages() == null && data.getLanguagesList() != null) {
            candidate.setLanguages(data.getLanguagesList());
            candidate.setRawLanguages(data.getLanguages());
        }
        if (isBlank(candidate.getResumePath()) && data.getResume() != null) {
            candidate.setResumePath(data.getResume());
        }
    }

    /**
     * Find vacancy by title or code, or create unmapped entry.
     */
    private Vacancy findOrCreateVacancy(String rawValue) {
        if (isBlank(rawValue)) {
            return null;
        }

        // Try to find by title (case insensitive)
        Optional<Vacancy> vacancy = vacancyRepository.findByTitleIgnoreCase(rawValue.trim());
        if (vacancy.isPresent()) {
            return vacancy.get();
        }

        // Try to find by code
        vacancy = vacancyRepository.findByCode(rawValue.trim());
        if (vacancy.isPresent()) {
            return vacancy.get();
        }

        // Create unmapped vacancy placeholder
        Vacancy unmapped = Vacancy.builder()
            .code("UNMAPPED_" + UUID.randomUUID().toString().substring(0, 8))
            .title(rawValue.trim())
            .description("Автоматически созданная запись для неизвестной программы: " + rawValue)
            .active(false)
            .allowUnmapped(true)
            .build();

        log.warn("Creating unmapped vacancy for: {}", rawValue);
        return vacancyRepository.save(unmapped);
    }

    /**
     * Create application for candidate.
     */
    private Application createApplication(Candidate candidate, Vacancy vacancy, RowData data) {
        // Check for duplicate application
        Optional<Application> existing = applicationRepository
            .findByCandidateIdAndVacancyId(candidate.getId(), vacancy.getId());

        if (existing.isPresent()) {
            log.debug("Application already exists for candidate {} and vacancy {}", 
                candidate.getId(), vacancy.getId());
            return existing.get();
        }

        Application application = Application.builder()
            .candidate(candidate)
            .vacancy(vacancy)
            .status(ApplicationStatus.NEW)
            .submittedAt(data.getSubmittedAt())
            .build();

        return applicationRepository.save(application);
    }

    /**
     * Create application preferences for priority 1 and 2.
     */
    private void createPreferences(Application application, RowData data) {
        // First priority
        if (data.getPriority1() != null) {
            Vacancy vacancy1 = findOrCreateVacancy(data.getPriority1());
            ApplicationPreference pref1 = ApplicationPreference.builder()
                .application(application)
                .vacancy(vacancy1)
                .rank(1)
                .rawValue(data.getPriority1())
                .isMapped(vacancy1 != null && vacancy1.getActive())
                .build();
            preferenceRepository.save(pref1);
        }

        // Second priority
        if (data.getPriority2() != null) {
            Vacancy vacancy2 = findOrCreateVacancy(data.getPriority2());
            ApplicationPreference pref2 = ApplicationPreference.builder()
                .application(application)
                .vacancy(vacancy2)
                .rank(2)
                .rawValue(data.getPriority2())
                .isMapped(vacancy2 != null && vacancy2.getActive())
                .build();
            preferenceRepository.save(pref2);
        }
    }

    /**
     * Create notification for candidate.
     */
    private void createNotification(Candidate candidate, Application application) {
        Notification notification = Notification.builder()
            .candidate(candidate)
            .application(application)
            .type(NotificationType.APPLICATION_RECEIVED)
            .subject("Ваша заявка принята")
            .body(String.format("Здравствуйте, %s!\n\nВаша заявка на программу \"%s\" успешно получена. " +
                "Вы можете отслеживать статус по ссылке с токеном: %s",
                candidate.getFullName(), application.getVacancy().getTitle(), candidate.getAccessToken()))
            .sent(false)
            .attempts(0)
            .build();

        notificationRepository.save(notification);
    }

    /**
     * Create or link User account for candidate.
     * This allows the candidate to login as a STAGER and view their application status.
     */
    private void createOrLinkUserAccount(Candidate candidate, ImportBatch batch) {
        if (candidate.getEmail() == null || candidate.getEmail().isBlank()) {
            log.warn("Cannot create user account for candidate {} - no email", candidate.getId());
            return;
        }

        String normalizedEmail = candidate.getEmail().toLowerCase().trim();
        
        // Check if user already exists with this email
        Optional<User> existingUser = userRepository.findByEmailNormalized(normalizedEmail);
        
        if (existingUser.isPresent()) {
            // User already exists - link to existing account
            User user = existingUser.get();
            
            // Ensure the user has STAGER or CANDIDATE role
            if (!user.getRoles().contains(UserRole.STAGER) && !user.getRoles().contains(UserRole.CANDIDATE)) {
                user.getRoles().add(UserRole.STAGER);
                userRepository.save(user);
                log.info("Added STAGER role to existing user: {}", user.getEmail());
            }
            
            batch.incrementUsersLinked();
            log.debug("Linked candidate {} to existing user account: {}", candidate.getEmail(), user.getUsername());
        } else {
            // Create new user account
            String username = generateUsername(candidate);
            String temporaryPassword = generateTemporaryPassword();
            
            User newUser = User.builder()
                .username(username)
                .email(candidate.getEmail())
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .phone(candidate.getPhone())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .roles(Set.of(UserRole.STAGER))
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();
            
            userRepository.save(newUser);
            batch.incrementUsersCreated();
            
            log.info("Created new user account for candidate {}: username={}, temporary password for testing: {}",
                candidate.getEmail(), username, temporaryPassword);
        }
    }

    /**
     * Generate username from candidate email.
     * Format: email prefix or email with unique suffix if collision.
     */
    private String generateUsername(Candidate candidate) {
        String baseUsername = candidate.getEmail().split("@")[0].toLowerCase();
        
        // Sanitize username (remove special characters)
        baseUsername = baseUsername.replaceAll("[^a-z0-9._-]", "");
        
        String username = baseUsername;
        int suffix = 1;
        
        // Check for collision and add suffix if needed
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        
        return username;
    }

    /**
     * Generate temporary password for new stager accounts.
     * For production: this should be replaced with email-based password setup or stronger mechanism.
     * For testing: using a simple pattern for convenience.
     */
    private String generateTemporaryPassword() {
        // For testing environment: use predictable password
        // In production, generate random password and send via email
        return "Stager2024!";
    }

    /**
     * Record import error.
     */
    private void recordError(ImportBatch batch, int rowNumber, String errorCode, 
                           String errorMessage, Row row, Map<String, Integer> headerMap) {
        // Create snapshot of row data
        Map<String, Object> snapshot = new HashMap<>();
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            Cell cell = row.getCell(entry.getValue());
            if (cell != null) {
                snapshot.put(entry.getKey(), getCellValueAsString(cell));
            }
        }

        ImportRowError error = ImportRowError.builder()
            .batch(batch)
            .rowNumber(rowNumber)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .rawSnapshot(snapshot)
            .build();

        errorRepository.save(error);
    }

    /**
     * Get current authenticated user.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    /**
     * Get cell value as string.
     */
    private String getCellValue(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer colIndex = headerMap.get(headerName);
        if (colIndex == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        return getCellValueAsString(cell);
    }

    /**
     * Get cell value as integer.
     */
    private Integer getCellValueAsInteger(Row row, Map<String, Integer> headerMap, String headerName) {
        String value = getCellValue(row, headerMap, headerName);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Unable to parse integer from: {}", value);
            return null;
        }
    }

    /**
     * Get cell value as LocalDateTime.
     */
    private LocalDateTime getCellValueAsDateTime(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer colIndex = headerMap.get(headerName);
        if (colIndex == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return null;
    }

    /**
     * Get cell value as string (handles all cell types).
     * Truncates excessively long strings to prevent issues.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        String value = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                // Check if value is actually a whole number
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    yield String.valueOf((long) numValue);
                } else {
                    yield String.valueOf(numValue);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };

        // Truncate excessively long strings (safety limit)
        if (value != null && value.length() > 5000) {
            log.warn("Cell value truncated from {} to 5000 characters", value.length());
            value = value.substring(0, 5000);
        }

        return value;
    }

    /**
     * Check if row is empty.
     */
    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if string is blank.
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Internal class for holding row data.
     */
    @lombok.Data
    private static class RowData {
        private String lastName;
        private String firstName;
        private String telegram;
        private String phone;
        private String phoneE164;
        private String rawPhone;
        private String email;
        private String resume;
        private String priority1;
        private String priority2;
        private String course;
        private String speciality;
        private String otherSpeciality;
        private String schedule;
        private String city;
        private String otherCity;
        private String source;
        private Integer birthYear;
        private String citizenship;
        private String university;
        private String otherUniversity;
        private String languages;
        private List<String> languagesList;
        private LocalDateTime submittedAt;
    }
}
