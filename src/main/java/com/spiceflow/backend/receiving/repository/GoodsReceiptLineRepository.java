package com.spiceflow.backend.receiving.repository;

import com.spiceflow.backend.receiving.entity.GoodsReceiptLineEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLineEntity, Long> {

    List<GoodsReceiptLineEntity> findByGoodsReceiptId(Long goodsReceiptId);
}
