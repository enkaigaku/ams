package com.ams.repository;

import com.ams.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByName(String name);

    Optional<Department> findByManagerId(String managerId);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager")
    List<Department> findAllWithManager();

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
    Optional<Department> findByIdWithEmployees(@Param("id") UUID id);

    boolean existsByName(String name);

    @Query("SELECT COUNT(d) FROM Department d")
    long countDepartments();
}