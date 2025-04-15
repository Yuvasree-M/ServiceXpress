package com.backend.repository;

import com.backend.model.Inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	
	Optional<Inventory> findByWorkitems(String workitems);
	List<Inventory> findAllByOrderByLastUpdatedDesc();

}
