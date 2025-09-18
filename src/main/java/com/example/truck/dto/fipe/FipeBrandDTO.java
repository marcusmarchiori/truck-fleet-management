package com.example.truck.dto.fipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FipeBrandDTO {

    @JsonProperty("codigo")
    private String code;

    @JsonProperty("nome")
    private String name;
}
