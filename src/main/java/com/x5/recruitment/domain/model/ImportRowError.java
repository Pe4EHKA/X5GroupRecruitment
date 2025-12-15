package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Import row error entity for tracking errors during Excel/CSV import.
 * Stores detailed error information for failed rows.
 */
@Entity
@Table(name = "import_row_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportRowError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ImportBatch batch;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "error_code", nullable = false, length = 100)
    private String errorCode;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> rawSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
