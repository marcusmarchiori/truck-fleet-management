package com.example.truck.service;

import com.example.truck.dto.fipe.FipePriceResponseDTO;
import com.example.truck.dto.fipe.FipeYearDTO;
import com.example.truck.entity.TruckEntity;
import com.example.truck.repository.TruckRepository;
import com.example.truck.service.fipe.FipeApiClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TruckService {

    private final TruckRepository truckRepository;
    private final FipeApiClient fipeApiClient;

    public TruckService(TruckRepository truckRepository, FipeApiClient fipeApiClient) {
        this.truckRepository = truckRepository;
        this.fipeApiClient = fipeApiClient;
    }

    public TruckEntity createTruck(TruckEntity truck) {
        if (truckRepository.existsByLicensePlate(truck.getLicensePlate())) {
            throw new IllegalArgumentException("Placa já cadastrada");
        }

        String brandId = fipeApiClient.getBrandCode(truck.getBrand());
        if (brandId == null) {
            throw new IllegalArgumentException("Marca não encontrada na FIPE.");
        }

        String modelId = fipeApiClient.getModelCode(brandId, truck.getModel());
        if (modelId == null) {
            throw new IllegalArgumentException("Modelo não encontrado para a marca especificada.");
        }

        List<FipeYearDTO> years = fipeApiClient.getYearsByModel(brandId, modelId);
        String yearCode = years.stream()
                .filter(y -> y.getName().startsWith(String.valueOf(truck.getManufacturingYear())))
                .map(FipeYearDTO::getCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ano não encontrado para este modelo na FIPE."));

        FipePriceResponseDTO fipeData = fipeApiClient.getPrice(brandId, modelId, yearCode);
        if (fipeData == null || fipeData.getPrice() == null) {
            throw new IllegalArgumentException("Combinação de marca, modelo e ano não encontrada na FIPE.");
        }

        truck.setFipePrice(new BigDecimal(
                fipeData.getPrice().replaceAll("[^\\d,]", "").replace(",", ".")
        ));

        return truckRepository.save(truck);
    }

    public List<TruckEntity> getAllTrucks() {
        return truckRepository.findAll();
    }

    public TruckEntity updateTruck(Long id, TruckEntity updatedTruck) {
        return truckRepository.findById(id)
                .map(existingTruck -> {
                    if (!existingTruck.getLicensePlate().equals(updatedTruck.getLicensePlate())
                            && truckRepository.existsByLicensePlate(updatedTruck.getLicensePlate())) {
                        throw new IllegalArgumentException("A nova placa já está cadastrada para outro veículo.");
                    }

                    String brandId = fipeApiClient.getBrandCode(updatedTruck.getBrand());
                    if (brandId == null) {
                        throw new IllegalArgumentException("Marca não encontrada na FIPE.");
                    }

                    String modelId = fipeApiClient.getModelCode(brandId, updatedTruck.getModel());
                    if (modelId == null) {
                        throw new IllegalArgumentException("Modelo não encontrado para a marca especificada.");
                    }

                    List<FipeYearDTO> years = fipeApiClient.getYearsByModel(brandId, modelId);
                    String yearCode = years.stream()
                            .filter(y -> y.getName().startsWith(String.valueOf(updatedTruck.getManufacturingYear())))
                            .map(FipeYearDTO::getCode)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Ano não encontrado para este modelo na FIPE."));

                    FipePriceResponseDTO fipeData = fipeApiClient.getPrice(brandId, modelId, yearCode);
                    if (fipeData == null || fipeData.getPrice() == null) {
                        throw new IllegalArgumentException("Combinação de marca, modelo e ano não encontrada na FIPE.");
                    }

                    existingTruck.setLicensePlate(updatedTruck.getLicensePlate());
                    existingTruck.setBrand(updatedTruck.getBrand());
                    existingTruck.setModel(updatedTruck.getModel());
                    existingTruck.setManufacturingYear(updatedTruck.getManufacturingYear());
                    existingTruck.setFipePrice(new BigDecimal(
                            fipeData.getPrice().replaceAll("[^\\d,]", "").replace(",", ".")
                    ));

                    return truckRepository.save(existingTruck);
                })
                .orElseThrow(() -> new RuntimeException("Caminhão não encontrado com o ID: " + id));
    }
}
