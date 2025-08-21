package com.ams.repository;

import com.ams.entity.Alert;
import com.ams.entity.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Alert> findByIsReadFalseOrderByCreatedAtDesc();

    @Query("SELECT a FROM Alert a WHERE a.user.department.managerId = :managerId ORDER BY a.createdAt DESC")
    List<Alert> findByManagerIdOrderByCreatedAtDesc(@Param("managerId") String managerId);

    @Query("SELECT a FROM Alert a WHERE a.user.department.managerId = :managerId AND a.isRead = false ORDER BY a.createdAt DESC")
    List<Alert> findUnreadByManagerIdOrderByCreatedAtDesc(@Param("managerId") String managerId);

    List<Alert> findByTypeAndAlertDateOrderByCreatedAtDesc(AlertType type, LocalDate alertDate);

    @Query("SELECT a FROM Alert a WHERE a.alertDate BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<Alert> findByDateRangeOrderByCreatedAtDesc(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.isRead = false")
    long countUnreadAlerts();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.user.department.managerId = :managerId AND a.isRead = false")
    long countUnreadAlertsByManagerId(@Param("managerId") String managerId);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.user.id = :userId AND a.isRead = false")
    long countUnreadAlertsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.id IN :alertIds")
    void markAlertsAsRead(@Param("alertIds") List<UUID> alertIds);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.user.id = :userId")
    void markAllAlertsAsReadByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.user.department.managerId = :managerId")
    void markAllAlertsAsReadByManagerId(@Param("managerId") String managerId);

    boolean existsByUserIdAndTypeAndAlertDate(UUID userId, AlertType type, LocalDate alertDate);

    @Modifying
    @Query("DELETE FROM Alert a WHERE a.createdAt < :cutoffDate")
    void deleteOldAlerts(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}