package com.example.truck.dto.fipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FipePriceResponseDTO {

    @JsonProperty("Valor")
    private String price;

    @JsonProperty("Marca")
    private String brand;

    @JsonProperty("Modelo")
    private String model;

    @JsonProperty("AnoModelo")
    private Integer modelYear;

    @JsonProperty("Combustivel")
    private String fuel;

    @JsonProperty("CodigoFipe")
    private String fipeCode;

    @JsonProperty("MesReferencia")
    private String referenceMonth;

    @JsonProperty("SiglaCombustivel")
    private String fuelInitials;
}
