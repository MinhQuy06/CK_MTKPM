package com.datlichkhambenh.backend.strategy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name="benefit_decisions") @Getter @NoArgsConstructor
public class BenefitDecision {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="request_id",nullable=false) private BenefitRequest request;
 @Column(nullable=false,length=20) private String action;
 @Column(nullable=false,length=1000) private String reason;
 @Column(name="decided_by",nullable=false) private Long decidedBy;
 @CreationTimestamp @Column(name="decided_at",nullable=false,updatable=false) private LocalDateTime decidedAt;
 public BenefitDecision(BenefitRequest request,String action,String reason,Long decidedBy) {
  this.request=request;this.action=action;this.reason=reason;this.decidedBy=decidedBy;
 }
}
