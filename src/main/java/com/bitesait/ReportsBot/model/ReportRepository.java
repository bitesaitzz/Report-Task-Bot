package com.bitesait.ReportsBot.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends CrudRepository<Report, Long> {

    @Query("SELECT r.id FROM ReportTable r WHERE r.reestr.id = :reestrId")
    List<Long> findByReestrId(Long reestrId);

    @Query("SELECT COUNT(r) > 0 FROM ReportTable r WHERE r.reestr.id = :reestrId AND MONTH(r.date) = :currentMonth AND YEAR(r.date) = :currentYear AND r.type = :type")
    boolean existsByReestrIdAndCurrentMonthAndType(@Param("reestrId") Long reestrId, @Param("currentMonth") int currentMonth, @Param("currentYear") int currentYear, @Param("type") String type);

    @Query("SELECT COUNT(r) > 0 FROM ReportTable r WHERE r.reestr.id = :reestrId AND r.type = :type")
    boolean existsByType(@Param("reestrId") Long reestrId, @Param("type") String type);

    @Query("SELECT MAX(r.date) FROM ReportTable r WHERE r.reestr.id = :reestrId AND r.type = :type")
    Optional<LocalDate> findLastReportDateByType(@Param("reestrId") Long reestrId, @Param("type") String type);

}
