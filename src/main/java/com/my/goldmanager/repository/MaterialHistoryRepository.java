package com.my.goldmanager.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.my.goldmanager.entity.MaterialHistory;

public interface MaterialHistoryRepository extends JpaRepository<MaterialHistory, String> {
	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId order by mh.entryDate desc")
	List<MaterialHistory> findByMaterial(@Param("materialId") String materialId);
	
	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId and (mh.entryDate BETWEEN :startDate and :endDate) order by mh.entryDate desc")
	List<MaterialHistory> findByMaterialInRange(@Param("materialId") String materialId, @Param("startDate") Date startDate, @Param("endDate") Date endDate );
	

	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId and mh.entryDate <= :startDate order by mh.entryDate desc")
	List<MaterialHistory> findByMaterialStartAt(@Param("materialId") String materialId, @Param("startDate") Date startDate );
	
	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId and mh.entryDate >=:endDate order by mh.entryDate desc")
	List<MaterialHistory> findByMaterialEndAt(@Param("materialId") String materialId, @Param("endDate") Date endDate );
}
