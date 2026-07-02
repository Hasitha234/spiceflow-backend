package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.DeliveryShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryShopItemRepository extends JpaRepository<DeliveryShopItem, Long> {
}
