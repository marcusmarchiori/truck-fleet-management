package com.example.truck.service;

import com.example.truck.dto.fipe.FipePriceResponseDTO;
import com.example.truck.entity.TruckEntity;
import com.example.truck.repository.TruckRepository;
import com.example.truck.service.fipe.FipeApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TruckServiceTest {

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private FipeApiClient fipeApiClient;

    @InjectMocks
    private TruckService truckService;

    private TruckEntity validTruck;
    private FipePriceResponseDTO fipePriceResponse;

    @BeforeEach
    void setUp() {
        validTruck = new TruckEntity();
        validTruck.setId(1L);
        validTruck.setLicensePlate("ABC-1234");
        validTruck.setBrand("Scania");
        validTruck.setModel("R 450");
        validTruck.setManufacturingYear(2022);

        fipePriceResponse = new FipePriceResponseDTO();
        fipePriceResponse.setPrice("R$ 450.000,00");
    }

    // Teste para createTruck
    @Test
    void testCreateTruck_Success() {
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(false);
        when(fipeApiClient.getBrandCode(anyString())).thenReturn("1");
        when(fipeApiClient.getModelCode(anyString(), anyString())).thenReturn("1");
        when(fipeApiClient.getPrice(anyString(), anyString(), anyString())).thenReturn(fipePriceResponse);
        when(truckRepository.save(any(TruckEntity.class))).thenReturn(validTruck);

        TruckEntity result = truckService.createTruck(validTruck);

        assertNotNull(result);
        assertEquals(new BigDecimal("450000.00"), result.getFipePrice());
        verify(truckRepository, times(1)).existsByLicensePlate(validTruck.getLicensePlate());
        verify(truckRepository, times(1)).save(any(TruckEntity.class));
    }

    @Test
    void testCreateTruck_ThrowsException_WhenLicensePlateExists() {
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> truckService.createTruck(validTruck));

        verify(truckRepository, never()).save(any(TruckEntity.class));
    }

    @Test
    void testCreateTruck_ThrowsException_WhenFipeBrandNotFound() {
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(false);
        when(fipeApiClient.getBrandCode(anyString())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> truckService.createTruck(validTruck));

        verify(truckRepository, never()).save(any(TruckEntity.class));
    }

    // Testes para getAllTrucks
    @Test
    void testGetAllTrucks_ReturnsListOfTrucks() {
        when(truckRepository.findAll()).thenReturn(Collections.singletonList(validTruck));

        List<TruckEntity> result = truckService.getAllTrucks();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(truckRepository, times(1)).findAll();
    }

    @Test
    void testUpdateTruck_Success() {
        when(truckRepository.findById(anyLong())).thenReturn(Optional.of(validTruck));
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(false);
        when(fipeApiClient.getBrandCode(anyString())).thenReturn("1");
        when(fipeApiClient.getModelCode(anyString(), anyString())).thenReturn("1");
        when(fipeApiClient.getPrice(anyString(), anyString(), anyString())).thenReturn(fipePriceResponse);
        when(truckRepository.save(any(TruckEntity.class))).thenReturn(validTruck);

        TruckEntity updatedDetails = new TruckEntity();
        updatedDetails.setLicensePlate("XYZ-9876");
        updatedDetails.setBrand("Volvo");
        updatedDetails.setModel("FH 540");
        updatedDetails.setManufacturingYear(2023);

        TruckEntity result = truckService.updateTruck(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("XYZ-9876", result.getLicensePlate());
        assertEquals(new BigDecimal("450000.00"), result.getFipePrice());
        verify(truckRepository, times(1)).save(any(TruckEntity.class));
    }

    @Test
    void testUpdateTruck_ThrowsException_WhenTruckNotFound() {
        when(truckRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> truckService.updateTruck(1L, validTruck));

        verify(truckRepository, never()).save(any(TruckEntity.class));
    }

    @Test
    void testUpdateTruck_ThrowsException_WhenNewLicensePlateExists() {
        TruckEntity existingTruckWithSamePlate = new TruckEntity();
        existingTruckWithSamePlate.setLicensePlate("XYZ-9876");

        when(truckRepository.findById(anyLong())).thenReturn(Optional.of(validTruck));
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> truckService.updateTruck(1L, existingTruckWithSamePlate));

        verify(truckRepository, never()).save(any(TruckEntity.class));
    }
}
