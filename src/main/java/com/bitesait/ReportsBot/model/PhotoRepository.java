package com.bitesait.ReportsBot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {


    @Query("SELECT p FROM PhotoTable p WHERE p.report.id = :reportId")
    List<Photo> findByReport(@Param("reportId") Long reportId);
}
