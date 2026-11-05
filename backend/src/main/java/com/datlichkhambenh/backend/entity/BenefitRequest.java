package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="benefit_requests", indexes=@Index(name="idx_benefit_patient",columnList="patient_id,created_at"))
@Getter @Setter @NoArgsConstructor
public class BenefitRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="patient_id",nullable=false) private Patient patient;
    @Column(nullable=false,length=20) private String kind;
    @Column(name="reference_code",nullable=false,length=120) private String referenceCode;
    @Column(name="requested_from",nullable=false) private LocalDate requestedFrom;
    @Column(name="requested_until",nullable=false) private LocalDate requestedUntil;
    @Column(nullable=false,length=20) private String status="PENDING";
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
    @Column(name="approved_from") private LocalDate approvedFrom;
    @Column(name="approved_until") private LocalDate approvedUntil;
    @Version @Column(nullable=false) private long version;
}
