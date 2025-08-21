package com.ams.repository;

import com.ams.entity.User;
import com.ams.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmployeeId(String employeeId);

    Optional<User> findByEmployeeIdAndIsActiveTrue(String employeeId);

    Optional<User> findByEmail(String email);

    List<User> findByDepartmentIdAndIsActiveTrue(UUID departmentId);

    List<User> findByRoleAndIsActiveTrue(UserRole role);

    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.role = :role AND u.isActive = true")
    List<User> findByDepartmentAndRole(@Param("departmentId") UUID departmentId, @Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.department.managerId = :managerId AND u.isActive = true")
    List<User> findTeamMembersByManagerId(@Param("managerId") String managerId);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId AND u.isActive = true")
    long countActiveUsersByDepartment(@Param("departmentId") UUID departmentId);
}