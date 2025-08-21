package com.ams.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ams.entity.TimeRecord;
import com.ams.entity.enums.AttendanceStatus;

@Repository
public interface TimeRecordRepository extends JpaRepository<TimeRecord, UUID> {

    Optional<TimeRecord> findByUserIdAndRecordDate(UUID userId, LocalDate recordDate);

    List<TimeRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
            UUID userId, LocalDate startDate, LocalDate endDate);

    List<TimeRecord> findByRecordDateAndStatus(LocalDate recordDate, AttendanceStatus status);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.user.department.id = :departmentId AND tr.recordDate = :recordDate")
    List<TimeRecord> findByDepartmentAndDate(@Param("departmentId") UUID departmentId, @Param("recordDate") LocalDate recordDate);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.user.department.managerId = :managerId AND tr.recordDate = :recordDate")
    List<TimeRecord> findByManagerIdAndDate(@Param("managerId") String managerId, @Param("recordDate") LocalDate recordDate);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.user.department.managerId = :managerId AND tr.recordDate BETWEEN :startDate AND :endDate ORDER BY tr.recordDate DESC")
    List<TimeRecord> findByManagerIdAndDateRange(@Param("managerId") String managerId, 
                                                @Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.recordDate = :recordDate AND tr.clockIn IS NOT NULL AND tr.clockOut IS NULL")
    List<TimeRecord> findIncompleteRecordsForDate(@Param("recordDate") LocalDate recordDate);

    @Query("SELECT COUNT(tr) FROM TimeRecord tr WHERE tr.user.id = :userId AND tr.recordDate BETWEEN :startDate AND :endDate AND tr.status = :status")
    long countByUserAndDateRangeAndStatus(@Param("userId") UUID userId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate, 
                                         @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(tr) FROM TimeRecord tr WHERE tr.recordDate = :recordDate AND tr.status = :status")
    long countByDateAndStatus(@Param("recordDate") LocalDate recordDate, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(tr) FROM TimeRecord tr WHERE tr.user.department.id = :departmentId AND tr.recordDate = :recordDate AND tr.status = :status")
    long countByDepartmentAndDateAndStatus(@Param("departmentId") UUID departmentId, 
                                          @Param("recordDate") LocalDate recordDate, 
                                          @Param("status") AttendanceStatus status);

    @Query("SELECT AVG(tr.totalHours) FROM TimeRecord tr WHERE tr.user.id = :userId AND tr.recordDate BETWEEN :startDate AND :endDate AND tr.totalHours IS NOT NULL")
    Double getAverageHoursByUserAndDateRange(@Param("userId") UUID userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(tr.totalHours) FROM TimeRecord tr WHERE tr.user.id = :userId AND tr.recordDate BETWEEN :startDate AND :endDate AND tr.totalHours IS NOT NULL")
    Double getTotalHoursByUserAndDateRange(@Param("userId") UUID userId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.user.id = :userId ORDER BY tr.recordDate DESC LIMIT 1")
    Optional<TimeRecord> findLatestByUserId(@Param("userId") UUID userId);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.recordDate BETWEEN :startDate AND :endDate ORDER BY tr.recordDate DESC, tr.user.name ASC")
    List<TimeRecord> findAllByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByUserIdAndRecordDate(UUID userId, LocalDate recordDate);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.user.id = :userId AND tr.recordDate BETWEEN :startDate AND :endDate")
    Stream<TimeRecord> findByUserIdAndDateRangeStream(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.recordDate BETWEEN :startDate AND :endDate")
    Stream<TimeRecord> findByDateRangeStream(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(tr) FROM TimeRecord tr WHERE tr.user.id = :userId AND tr.recordDate BETWEEN :startDate AND :endDate AND tr.status = :status")
    long countByUserIdAndDateRangeAndStatus(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") AttendanceStatus status);
}