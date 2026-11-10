package com.datlichkhambenh.backend.strategy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Một quyết định của Admin trên yêu cầu quyền hưởng (APPROVE / REJECT / REVOKE). Chỉ thêm, không sửa. */
@Entity
@Table(name = "benefit_decisions")
@Getter
@NoArgsConstructor
public class BenefitDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private BenefitRequest request;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(nullable = false, length = 1000)
    private String reason;

    /** userId của Admin ra quyết định. */
    @Column(name = "decided_by", nullable = false)
    private Long decidedBy;

    @CreationTimestamp
    @Column(name = "decided_at", nullable = false, updatable = false)
    private LocalDateTime decidedAt;

    public BenefitDecision(BenefitRequest request, String action, String reason, Long decidedBy) {
        this.request = request;
        this.action = action;
        this.reason = reason;
        this.decidedBy = decidedBy;
    }
}
