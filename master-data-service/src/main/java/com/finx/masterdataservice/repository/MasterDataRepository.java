package com.finx.masterdataservice.repository;

import com.finx.masterdataservice.domain.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {
    List<MasterData> findByDataType(String dataType);

    Optional<MasterData> findByDataTypeAndCode(String dataType, String code);

    boolean existsByDataTypeAndCode(String dataType, String code);

    boolean existsByDataType(String dataType);

    void deleteByDataType(String dataType);

    @Query("SELECT DISTINCT m.dataType FROM MasterData m ORDER BY m.dataType")
    List<String> findAllDistinctDataTypes();

    @Query("SELECT m.dataType, COUNT(m) FROM MasterData m GROUP BY m.dataType ORDER BY m.dataType")
    List<Object[]> findCategoryWiseCounts();

    @Query("SELECT m.dataType, COUNT(m), " +
           "CASE WHEN SUM(CASE WHEN m.isActive = true THEN 1 ELSE 0 END) > 0 THEN 'ACTIVE' ELSE 'INACTIVE' END " +
           "FROM MasterData m GROUP BY m.dataType ORDER BY m.dataType")
    List<Object[]> findCategoryWiseCountsWithStatus();
}
