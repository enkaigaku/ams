package com.ams.repository;

import com.ams.entity.TimeModificationRequest;
import com.ams.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeModificationRequestRepository extends JpaRepository<TimeModificationRequest, UUID> {

    List<TimeModificationRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<TimeModificationRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    @Query("SELECT tmr FROM TimeModificationRequest tmr WHERE tmr.user.department.managerId = :managerId ORDER BY tmr.createdAt DESC")
    List<TimeModificationRequest> findByManagerIdOrderByCreatedAtDesc(@Param("managerId") String managerId);

    @Query("SELECT tmr FROM TimeModificationRequest tmr WHERE tmr.user.department.managerId = :managerId AND tmr.status = :status ORDER BY tmr.createdAt DESC")
    List<TimeModificationRequest> findByManagerIdAndStatusOrderByCreatedAtDesc(@Param("managerId") String managerId, @Param("status") RequestStatus status);

    List<TimeModificationRequest> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, RequestStatus status);

    List<TimeModificationRequest> findByRequestDateOrderByCreatedAtDesc(LocalDate requestDate);

    @Query("SELECT tmr FROM TimeModificationRequest tmr WHERE tmr.requestDate BETWEEN :startDate AND :endDate ORDER BY tmr.requestDate DESC")
    List<TimeModificationRequest> findByDateRangeOrderByRequestDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(tmr) FROM TimeModificationRequest tmr WHERE tmr.status = :status")
    long countByStatus(@Param("status") RequestStatus status);

    @Query("SELECT COUNT(tmr) FROM TimeModificationRequest tmr WHERE tmr.user.department.managerId = :managerId AND tmr.status = :status")
    long countByManagerIdAndStatus(@Param("managerId") String managerId, @Param("status") RequestStatus status);

    boolean existsByUserIdAndRequestDateAndStatus(UUID userId, LocalDate requestDate, RequestStatus status);

    @Query("SELECT tmr FROM TimeModificationRequest tmr WHERE tmr.user.id = :userId AND tmr.requestDate = :requestDate AND tmr.status IN ('PENDING', 'APPROVED')")
    List<TimeModificationRequest> findActiveRequestsByUserAndDate(@Param("userId") UUID userId, @Param("requestDate") LocalDate requestDate);
}