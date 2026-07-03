package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.DeliveryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryPaymentRepository extends JpaRepository<DeliveryPayment, Long> {
}
