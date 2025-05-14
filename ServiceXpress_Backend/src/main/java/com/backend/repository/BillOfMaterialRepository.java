package com.backend.repository;

import com.backend.model.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {
    Optional<BillOfMaterial> findByBookingId(Long bookingId);
}