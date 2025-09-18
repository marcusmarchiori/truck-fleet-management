package com.example.truck.controller;

import com.example.truck.entity.TruckEntity;
import com.example.truck.service.TruckService;
import com.example.truck.service.fipe.FipeApiClient;
import com.example.truck.dto.fipe.FipeBrandDTO;
import com.example.truck.dto.fipe.FipeModelDTO;
import com.example.truck.dto.fipe.FipeModelResponseDTO;
import com.example.truck.dto.fipe.FipePriceResponseDTO;
import com.example.truck.dto.fipe.FipeYearDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trucks")
@CrossOrigin(origins = "http://localhost:4200")
public class TruckController {

    private final TruckService truckService;
    private final FipeApiClient fipeApiClient;

    public TruckController(TruckService truckService, FipeApiClient fipeApiClient) {
        this.truckService = truckService;
        this.fipeApiClient = fipeApiClient;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TruckEntity> getTruckById(@PathVariable Long id) {
        return ResponseEntity.of(truckService.getTruckById(id));
    }

    @GetMapping
    public ResponseEntity<List<TruckEntity>> getAllTrucks() {
        List<TruckEntity> trucks = truckService.getAllTrucks();
        return new ResponseEntity<>(trucks, HttpStatus.OK);
    }

    @GetMapping("/fipe/brands")
    public ResponseEntity<List<FipeBrandDTO>> getAllBrands() {
        FipeBrandDTO[] brands = fipeApiClient.getAllBrands();
        List<FipeBrandDTO> body = (brands == null) ? List.of() : List.of(brands);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/fipe/brands/{brandCode}/models")
    public ResponseEntity<List<FipeModelDTO>> getModelsByBrand(@PathVariable String brandCode) {
        FipeModelResponseDTO response = fipeApiClient.getModelsByBrand(brandCode);
        List<FipeModelDTO> models = (response != null && response.getModels() != null) ? response.getModels() : List.of();
        return new ResponseEntity<>(models, HttpStatus.OK);
    }

    @GetMapping("/fipe/brands/{brandCode}/models/{modelCode}/years")
    public ResponseEntity<List<FipeYearDTO>> getYearsByModel(@PathVariable String brandCode,
                                                             @PathVariable String modelCode) {
        List<FipeYearDTO> years = fipeApiClient.getYearsByModel(brandCode, modelCode);
        return new ResponseEntity<>(years == null ? List.of() : years, HttpStatus.OK);
    }

    @GetMapping("/fipe/brands/{brandCode}/models/{modelCode}/years/{yearCode}")
    public ResponseEntity<FipePriceResponseDTO> getPriceFromFipe(@PathVariable String brandCode,
                                                                 @PathVariable String modelCode,
                                                                 @PathVariable String yearCode) {
        FipePriceResponseDTO price = fipeApiClient.getPrice(brandCode, modelCode, yearCode);
        return price != null ? new ResponseEntity<>(price, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<?> createTruck(@Valid @RequestBody TruckEntity truck) {
        try {
            TruckEntity createdTruck = truckService.createTruck(truck);
            return new ResponseEntity<>(createdTruck, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro interno: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTruck(@PathVariable Long id, @Valid @RequestBody TruckEntity truckDetails) {
        try {
            TruckEntity updatedTruck = truckService.updateTruck(id, truckDetails);
            return new ResponseEntity<>(updatedTruck, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
