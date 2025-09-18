package com.example.truck.service.fipe;

import com.example.truck.dto.fipe.FipeBrandDTO;
import com.example.truck.dto.fipe.FipeModelDTO;
import com.example.truck.dto.fipe.FipeModelResponseDTO;
import com.example.truck.dto.fipe.FipePriceResponseDTO;
import com.example.truck.dto.fipe.FipeYearDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class FipeApiClient {

    private final RestTemplate restTemplate;

    @Value("${fipe.api.base.url}")
    private String fipeApiBaseUrl;

    public FipeApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getBrandCode(String brandName) {
        String url = fipeApiBaseUrl + "/marcas";
        FipeBrandDTO[] brandsArray = restTemplate.getForObject(url, FipeBrandDTO[].class);

        if (brandsArray == null) return null;

        return Arrays.stream(brandsArray)
                .filter(brand -> brand.getName().equalsIgnoreCase(brandName))
                .map(FipeBrandDTO::getCode)
                .findFirst()
                .orElse(null);
    }

    public String getModelCode(String brandCode, String modelName) {
        String url = String.format("%s/marcas/%s/modelos", fipeApiBaseUrl, brandCode);
        FipeModelResponseDTO response = restTemplate.getForObject(url, FipeModelResponseDTO.class);

        if (response == null || response.getModels() == null) return null;

        return response.getModels().stream()
                .filter(model -> model.getName().equalsIgnoreCase(modelName))
                .map(FipeModelDTO::getCode)
                .findFirst()
                .orElse(null);
    }

    public List<FipeYearDTO> getYearsByModel(String brandCode, String modelCode) {
        String url = String.format("%s/marcas/%s/modelos/%s/anos", fipeApiBaseUrl, brandCode, modelCode);
        FipeYearDTO[] years = restTemplate.getForObject(url, FipeYearDTO[].class);
        return years != null ? Arrays.asList(years) : List.of();
    }

    public FipePriceResponseDTO getPrice(String brandCode, String modelCode, String yearCode) {
        String url = String.format("%s/marcas/%s/modelos/%s/anos/%s", fipeApiBaseUrl, brandCode, modelCode, yearCode);
        try {
            return restTemplate.getForObject(url, FipePriceResponseDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public FipeBrandDTO[] getAllBrands() {
        String url = fipeApiBaseUrl + "/marcas";
        return restTemplate.getForObject(url, FipeBrandDTO[].class);
    }

    public FipeModelResponseDTO getModelsByBrand(String brandCode) {
        String url = String.format("%s/marcas/%s/modelos", fipeApiBaseUrl, brandCode);
        return restTemplate.getForObject(url, FipeModelResponseDTO.class);
    }
}
