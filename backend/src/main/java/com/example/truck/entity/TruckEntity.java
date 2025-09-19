package com.example.truck.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "truck")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "license_plate", nullable = false, unique = true, length = 7)
    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 7, message = "A placa deve conter exatamente 7 caracteres")
    @Pattern(
            regexp = "^(?:[A-Z]{3}\\d{4}|[A-Z]{3}\\d[A-Z]\\d{2})$",
            message = "Placa inválida (use ABC1234 ou ABC1D23)"
    )
    private String licensePlate;

    @PrePersist @PreUpdate
    private void normalizePlate() {
        if (licensePlate != null) {
            licensePlate = licensePlate.replaceAll("[^A-Za-z0-9]", "")
                    .toUpperCase(Locale.ROOT)
                    .trim();
        }
    }

    @Column(name = "brand", nullable = false, length = 50)
    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Deve-se ter no máximo 50 caracteres")
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50, message = "Deve-se ter no máximo 50 caracteres")
    private String model;

    @Column(name = "manufacturing_year", nullable = false)
    @NotNull(message = "Ano de fabricação é obrigatório")
    @Min(value = 1900, message = "Fabricação deve ser após 1900")
    private Integer manufacturingYear;

    @Column(name = "fipe_price", precision = 12, scale = 2)
    @PositiveOrZero(message = "Preço deve ser igual ou maior que 0.0")
    private BigDecimal fipePrice;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    public TruckEntity(String licensePlate, String brand, String model, Integer manufacturingYear) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
    }
}
