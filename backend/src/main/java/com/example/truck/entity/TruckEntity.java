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

@Entity
@Table(name = "truck")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 10)
    @NotBlank(message = "Placa é obrigatória")
    @Size(max = 10, message = "Deve-se ter 10 caracteres no máximos")
    private String licensePlate;

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
    @Min(value = 1980, message = "Fabricação deve ser após 1900")
    private Integer manufacturingYear;

    @Column(name = "fipe_price", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Preço deve ser maior que zero")
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
