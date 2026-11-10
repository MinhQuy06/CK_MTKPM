package com.datlichkhambenh.backend.strategy.entity;

import com.datlichkhambenh.backend.entity.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Yêu cầu quyền hưởng Bảo hiểm / VIP của một bệnh nhân (xem "Quyền hưởng" trong GLOSSARY.md).
 * Trạng thái: PENDING → APPROVED / REJECTED, APPROVED → REVOKED. Gia hạn tạo yêu cầu mới trỏ về bản cũ.
 */
@Entity
@Table(name = "benefit_requests", indexes = @Index(name = "idx_benefit_patient", columnList = "patient_id,created_at"))
@Getter
@Setter
@NoArgsConstructor
public class BenefitRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** INSURANCE hoặc VIP. */
    @Column(nullable = false, length = 20)
    private String kind;

    /** Mã hồ sơ để Admin đối chiếu ngoài hệ thống. */
    @Column(name = "reference_code", nullable = false, length = 120)
    private String referenceCode;

    /** Khoảng ngày bệnh nhân đề nghị. */
    @Column(name = "requested_from", nullable = false)
    private LocalDate requestedFrom;

    @Column(name = "requested_until", nullable = false)
    private LocalDate requestedUntil;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Khoảng ngày Admin duyệt; chỉ có giá trị khi đã duyệt. */
    @Column(name = "approved_from")
    private LocalDate approvedFrom;

    @Column(name = "approved_until")
    private LocalDate approvedUntil;

    /** Yêu cầu gốc nếu đây là lần gia hạn. */
    @Column(name = "renewal_of_id")
    private Long renewalOfId;

    /** Khóa lạc quan: chống hai Admin cùng quyết định một yêu cầu. */
    @Version
    @Column(nullable = false)
    private long version;
}
