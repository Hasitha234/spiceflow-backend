package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.RepOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepOrderItemRepository extends JpaRepository<RepOrderItem, Long> {
}
