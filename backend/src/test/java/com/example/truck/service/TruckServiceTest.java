package com.example.truck.service;

import com.example.truck.dto.fipe.FipePriceResponseDTO;
import com.example.truck.dto.fipe.FipeYearDTO;
import com.example.truck.entity.TruckEntity;
import com.example.truck.repository.TruckRepository;
import com.example.truck.service.fipe.FipeApiClient;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TruckServiceTest {

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private FipeApiClient fipeApiClient;

    @InjectMocks
    private TruckService truckService;

    private TruckEntity validTruck;
    private FipePriceResponseDTO fipePriceResponse;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void setUp() {
        validTruck = new TruckEntity();
        validTruck.setId(1L);
        validTruck.setLicensePlate("ABC1234");
        validTruck.setBrand("Scania");
        validTruck.setModel("R 450");
        validTruck.setManufacturingYear(2022);

        fipePriceResponse = new FipePriceResponseDTO();
        fipePriceResponse.setPrice("R$ 450.000,00");
    }

    private FipeYearDTO mkYear(String code, String name) {
        FipeYearDTO y = new FipeYearDTO();
        y.setCode(code);
        y.setName(name);
        return y;
    }

    @Test
    void testCreateTruck_Success() {
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(false);
        when(fipeApiClient.getBrandCode(anyString())).thenReturn("1");
        when(fipeApiClient.getModelCode(anyString(), anyString())).thenReturn("1");
        when(fipeApiClient.getYearsByModel(eq("1"), eq("1")))
                .thenReturn(List.of(mkYear("2022", "2022 Gasolina")));
        when(fipeApiClient.getPrice(eq("1"), eq("1"), eq("2022"))).thenReturn(fipePriceResponse);
        when(truckRepository.save(any(TruckEntity.class))).thenReturn(validTruck);

        TruckEntity result = truckService.createTruck(validTruck);

        assertNotNull(result);
        assertEquals(new BigDecimal("450000.00"), result.getFipePrice());
        verify(truckRepository).existsByLicensePlate("ABC1234");
        verify(truckRepository).save(any(TruckEntity.class));
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

    @Test
    void testGetAllTrucks_ReturnsListOfTrucks() {
        when(truckRepository.findAll()).thenReturn(Collections.singletonList(validTruck));

        List<TruckEntity> result = truckService.getAllTrucks();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(truckRepository).findAll();
    }

    @Test
    void testUpdateTruck_Success() {
        when(truckRepository.findById(anyLong())).thenReturn(Optional.of(validTruck));
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(false);
        when(fipeApiClient.getBrandCode(anyString())).thenReturn("1");
        when(fipeApiClient.getModelCode(anyString(), anyString())).thenReturn("1");
        when(fipeApiClient.getYearsByModel(eq("1"), eq("1")))
                .thenReturn(List.of(mkYear("2023-1", "2023 Gasolina")));
        when(fipeApiClient.getPrice(eq("1"), eq("1"), eq("2023-1"))).thenReturn(fipePriceResponse);
        when(truckRepository.save(any(TruckEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TruckEntity updatedDetails = new TruckEntity();
        updatedDetails.setLicensePlate("XYZ9876");
        updatedDetails.setBrand("Volvo");
        updatedDetails.setModel("FH 540");
        updatedDetails.setManufacturingYear(2023);

        TruckEntity result = truckService.updateTruck(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("XYZ9876", result.getLicensePlate());
        assertEquals("Volvo", result.getBrand());
        assertEquals("FH 540", result.getModel());
        assertEquals(2023, result.getManufacturingYear());
        assertEquals(new BigDecimal("450000.00"), result.getFipePrice());
        verify(truckRepository).save(any(TruckEntity.class));
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
        existingTruckWithSamePlate.setLicensePlate("XYZ9876");
        existingTruckWithSamePlate.setBrand("Volvo");
        existingTruckWithSamePlate.setModel("FH 540");
        existingTruckWithSamePlate.setManufacturingYear(2023);

        when(truckRepository.findById(anyLong())).thenReturn(Optional.of(validTruck));
        when(truckRepository.existsByLicensePlate(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> truckService.updateTruck(1L, existingTruckWithSamePlate));

        verify(truckRepository, never()).save(any(TruckEntity.class));
    }

    @Test
    void plateRegex_accepts_OldAndMercosul() {
        TruckEntity t1 = new TruckEntity("ABC1234", "X", "Y", 2022);
        TruckEntity t2 = new TruckEntity("ABC1D23", "X", "Y", 2022);

        assertTrue(validator.validate(t1).isEmpty());
        assertTrue(validator.validate(t2).isEmpty());
    }

    @Test
    void plateRegex_rejects_Invalid() {
        TruckEntity t = new TruckEntity("AB-1234", "X", "Y", 2022);

        assertFalse(validator.validate(t).isEmpty());
    }
}
