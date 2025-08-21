package com.ams.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ams.entity.LeaveRequest;
import com.ams.entity.enums.LeaveType;
import com.ams.entity.enums.RequestStatus;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.department.managerId = :managerId ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByManagerIdOrderByCreatedAtDesc(@Param("managerId") String managerId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.department.managerId = :managerId AND lr.status = :status ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByManagerIdAndStatusOrderByCreatedAtDesc(@Param("managerId") String managerId, @Param("status") RequestStatus status);

    List<LeaveRequest> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, RequestStatus status);

    List<LeaveRequest> findByTypeAndStatusOrderByCreatedAtDesc(LeaveType type, RequestStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate AND lr.status = 'APPROVED'")
    List<LeaveRequest> findApprovedRequestsOverlapping(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.startDate <= :endDate AND lr.endDate >= :startDate AND lr.status IN ('PENDING', 'APPROVED')")
    List<LeaveRequest> findUserRequestsOverlapping(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = :status")
    long countByStatus(@Param("status") RequestStatus status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.user.department.managerId = :managerId AND lr.status = :status")
    long countByManagerIdAndStatus(@Param("managerId") String managerId, @Param("status") RequestStatus status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.type = :type AND lr.status = 'APPROVED' AND YEAR(lr.startDate) = :year")
    long countApprovedLeavesByUserAndTypeAndYear(@Param("userId") UUID userId, @Param("type") LeaveType type, @Param("year") int year);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate BETWEEN :startDate AND :endDate ORDER BY lr.startDate ASC")
    List<LeaveRequest> findByDateRangeOrderByStartDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.department.managerId = :managerId AND lr.startDate BETWEEN :startDate AND :endDate ORDER BY lr.startDate ASC")
    List<LeaveRequest> findByManagerIdAndDateRangeOrderByStartDate(@Param("managerId") String managerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByUserIdAndStartDateAndEndDateAndStatus(UUID userId, LocalDate startDate, LocalDate endDate, RequestStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.startDate <= :endDate AND lr.endDate >= :startDate AND lr.status IN (:statuses)")
    List<LeaveRequest> findOverlappingRequests(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("statuses") List<RequestStatus> statuses);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.type = :leaveType AND lr.status = 'APPROVED' AND lr.startDate BETWEEN :startDate AND :endDate")
    long countApprovedLeaveDays(@Param("userId") UUID userId, @Param("leaveType") LeaveType leaveType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.status = 'APPROVED' AND :date BETWEEN lr.startDate AND lr.endDate")
    List<LeaveRequest> findApprovedLeaveForDate(@Param("userId") UUID userId, @Param("date") LocalDate date);
}