package com.grosshaeuser.olmp.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @NotBlank
    @Size(max = 100)
    @Column(name = "inventory_number", nullable = false, unique = true, length = 100)
    private String inventoryNumber;

    @Size(max = 100)
    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 30)
    @Builder.Default
    private Condition condition = Condition.GOOD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "acquired_at")
    private LocalDate acquiredAt;

    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    @Column(name = "acquisition_cost", precision = 10, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(name = "withdrawn_at")
    private LocalDate withdrawnAt;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @AssertTrue(message = "withdrawal date must be on or after acquisition date")
    public boolean isWithdrawalDateValid() {
        return withdrawnAt == null || acquiredAt == null || !withdrawnAt.isBefore(acquiredAt);
    }

    public enum Condition {
        NEW, GOOD, WORN, DAMAGED, UNUSABLE, LOST
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
