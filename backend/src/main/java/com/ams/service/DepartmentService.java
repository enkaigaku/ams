package com.ams.service;

import com.ams.entity.Department;
import com.ams.entity.User;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartmentsWithManager() {
        return departmentRepository.findAllWithManager();
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Department getDepartmentByIdWithEmployees(UUID id) {
        return departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Department> findByName(String name) {
        return departmentRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<Department> findByManagerId(String managerId) {
        return departmentRepository.findByManagerId(managerId);
    }

    public Department createDepartment(Department department) {
        // Check if department name already exists
        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException("Department name already exists: " + department.getName());
        }

        Department savedDepartment = departmentRepository.save(department);
        logger.info("Created new department: {}", savedDepartment.getName());
        return savedDepartment;
    }

    public Department updateDepartment(UUID id, Department departmentUpdates) {
        Department existingDepartment = getDepartmentById(id);

        // Update name if provided and different
        if (departmentUpdates.getName() != null && !departmentUpdates.getName().equals(existingDepartment.getName())) {
            // Check if new name already exists
            if (departmentRepository.existsByName(departmentUpdates.getName())) {
                throw new IllegalArgumentException("Department name already exists: " + departmentUpdates.getName());
            }
            existingDepartment.setName(departmentUpdates.getName());
        }

        // Update manager if provided
        if (departmentUpdates.getManagerId() != null) {
            existingDepartment.setManagerId(departmentUpdates.getManagerId());
        }

        Department savedDepartment = departmentRepository.save(existingDepartment);
        logger.info("Updated department: {}", savedDepartment.getName());
        return savedDepartment;
    }

    public void deleteDepartment(UUID id) {
        Department department = getDepartmentById(id);
        
        // Check if department has employees
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            throw new IllegalStateException("Cannot delete department with employees. Please reassign employees first.");
        }

        departmentRepository.delete(department);
        logger.info("Deleted department: {}", department.getName());
    }

    public Department assignManager(UUID departmentId, String managerId) {
        Department department = getDepartmentById(departmentId);
        
        // Remove manager from previous department if exists
        Optional<Department> previousDepartment = departmentRepository.findByManagerId(managerId);
        if (previousDepartment.isPresent() && !previousDepartment.get().getId().equals(departmentId)) {
            Department prevDept = previousDepartment.get();
            prevDept.setManagerId(null);
            departmentRepository.save(prevDept);
            logger.info("Removed manager {} from department: {}", managerId, prevDept.getName());
        }

        department.setManagerId(managerId);
        Department savedDepartment = departmentRepository.save(department);
        logger.info("Assigned manager {} to department: {}", managerId, savedDepartment.getName());
        return savedDepartment;
    }

    public Department removeManager(UUID departmentId) {
        Department department = getDepartmentById(departmentId);
        
        String previousManagerId = department.getManagerId();
        department.setManagerId(null);
        
        Department savedDepartment = departmentRepository.save(department);
        logger.info("Removed manager {} from department: {}", previousManagerId, savedDepartment.getName());
        return savedDepartment;
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return departmentRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public long countDepartments() {
        return departmentRepository.countDepartments();
    }

    @Transactional(readOnly = true)
    public List<User> getDepartmentEmployees(UUID departmentId) {
        Department department = getDepartmentByIdWithEmployees(departmentId);
        return department.getEmployees();
    }

    @Transactional(readOnly = true)
    public long getEmployeeCountByDepartment(UUID departmentId) {
        Department department = getDepartmentByIdWithEmployees(departmentId);
        return department.getEmployees().stream()
                .filter(User::getIsActive)
                .count();
    }
}