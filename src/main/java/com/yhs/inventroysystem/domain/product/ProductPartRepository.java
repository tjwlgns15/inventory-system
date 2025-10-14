package com.yhs.inventroysystem.domain.product;

import com.yhs.inventroysystem.domain.part.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPartRepository extends JpaRepository<ProductPart, Long> {

    /**
     * 특정 부품이 제품에서 사용 중인지 확인
     */
    @Query("SELECT COUNT(pp) > 0 FROM ProductPart pp " +
            "WHERE pp.part = :part AND pp.product.deletedAt IS NULL")
    boolean existsByPart(@Param("part") Part part);

    /**
     * 특정 부품을 사용하는 제품 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT pp.product) FROM ProductPart pp " +
            "WHERE pp.part = :part AND pp.product.deletedAt IS NULL")
    long countProductsByPart(@Param("part") Part part);
}
