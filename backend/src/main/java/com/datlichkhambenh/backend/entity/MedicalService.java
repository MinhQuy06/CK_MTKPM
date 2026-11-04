package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "medical_services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalService {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultPrice;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Boolean active;
}
