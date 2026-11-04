package com.datlichkhambenh.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties")
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Specialty() {}

    public Specialty(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static SpecialtyBuilder builder() {
        return new SpecialtyBuilder();
    }

    public static class SpecialtyBuilder {
        private Long id;
        private String name;
        private String description;

        public SpecialtyBuilder id(Long id) { this.id = id; return this; }
        public SpecialtyBuilder name(String name) { this.name = name; return this; }
        public SpecialtyBuilder description(String description) { this.description = description; return this; }

        public Specialty build() {
            return new Specialty(id, name, description);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
