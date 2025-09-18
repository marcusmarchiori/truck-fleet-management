package com.example.truck.repository;

import com.example.truck.entity.TruckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {

    boolean existsByLicensePlate(String licensePlate);
}
