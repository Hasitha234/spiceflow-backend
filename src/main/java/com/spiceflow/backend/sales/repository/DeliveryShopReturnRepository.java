package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.DeliveryShopReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryShopReturnRepository extends JpaRepository<DeliveryShopReturn, Long> {
}
