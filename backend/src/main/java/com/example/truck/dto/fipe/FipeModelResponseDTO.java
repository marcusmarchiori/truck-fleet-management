package com.example.truck.dto.fipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class FipeModelResponseDTO {

    @JsonProperty("modelos")
    private List<FipeModelDTO> models;

    @JsonProperty("anos")
    private List<FipeModelDTO> years;
}
