package com.finx.management.repository;

import com.finx.management.domain.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {
    List<MasterData> findByDataType(String dataType);

    Optional<MasterData> findByDataTypeAndCode(String dataType, String code);

    boolean existsByDataTypeAndCode(String dataType, String code);
}
