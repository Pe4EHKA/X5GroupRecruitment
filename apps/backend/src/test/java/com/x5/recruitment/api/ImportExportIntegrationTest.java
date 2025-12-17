package com.x5.recruitment.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.recruitment.domain.model.User;
import com.x5.recruitment.domain.model.UserRole;
import com.x5.recruitment.domain.model.UserStatus;
import com.x5.recruitment.domain.repository.ImportBatchRepository;
import com.x5.recruitment.domain.repository.UserRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImportExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImportBatchRepository batchRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User recruiter = User.builder()
            .username("recruiter")
            .email("recruiter@example.com")
            .firstName("Recruiter")
            .lastName("User")
            .passwordHash(passwordEncoder.encode("password"))
            .roles(Set.of(UserRole.RECRUITER))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
        userRepository.save(recruiter);
    }

    @Test
    void importXlsx_shouldPersistCountersAndReturnSummary() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sample.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            buildValidWorkbook()
        );

        String response = mockMvc.perform(multipart("/api/import-export/import")
                .file(file)
                .with(httpBasic("recruiter", "password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batch.totalRows").value(1))
            .andExpect(jsonPath("$.batch.successRows").value(1))
            .andExpect(jsonPath("$.batch.failedRows").value(0))
            .andExpect(jsonPath("$.batch.usersCreated").value(1))
            .andExpect(jsonPath("$.batch.usersLinked").value(0))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        long batchId = root.path("batch").path("id").asLong();

        var batch = batchRepository.findById(batchId);
        assertThat(batch).isPresent();
        assertThat(batch.get().getUsersCreated()).isNotNull();
        assertThat(batch.get().getUsersLinked()).isNotNull();
    }

    @Test
    void authMe_shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("recruiter", "password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("recruiter"))
            .andExpect(jsonPath("$.roles[0]").value("RECRUITER"));
    }

    private byte[] buildValidWorkbook() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("data");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Фамилия");
            header.createCell(1).setCellValue("Имя");
            header.createCell(2).setCellValue("Почта");
            header.createCell(3).setCellValue("Первый приоритет");
            header.createCell(4).setCellValue("Дата заявки");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Doe");
            row.createCell(1).setCellValue("John");
            row.createCell(2).setCellValue("john.doe@example.com");
            row.createCell(3).setCellValue("Engineering");
            row.createCell(4).setCellValue(LocalDate.now());

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
