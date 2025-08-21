package com.ams.service;

import com.ams.entity.User;
import com.ams.entity.enums.UserRole;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        User user = userRepository.findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with employee ID: " + employeeId));

        logger.debug("Loaded user: {} with role: {}", user.getEmployeeId(), user.getRole());
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findActiveByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with employee ID: " + employeeId));
    }

    @Transactional(readOnly = true)
    public List<User> getAllActiveUsers() {
        return userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByDepartment(UUID departmentId) {
        return userRepository.findByDepartmentIdAndIsActiveTrue(departmentId);
    }

    @Transactional(readOnly = true)
    public List<User> getTeamMembersByManagerId(String managerId) {
        return userRepository.findTeamMembersByManagerId(managerId);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }

    public User createUser(User user) {
        // Check if employee ID already exists
        if (userRepository.existsByEmployeeId(user.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already exists: " + user.getEmployeeId());
        }

        // Check if email already exists (if provided)
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Encode password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        User savedUser = userRepository.save(user);
        logger.info("Created new user: {}", savedUser.getEmployeeId());
        return savedUser;
    }

    public User updateUser(UUID id, User userUpdates) {
        User existingUser = getUserById(id);

        // Update allowed fields
        if (userUpdates.getName() != null) {
            existingUser.setName(userUpdates.getName());
        }
        if (userUpdates.getEmail() != null) {
            // Check if email is already used by another user
            Optional<User> userWithEmail = userRepository.findByEmail(userUpdates.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("Email already exists: " + userUpdates.getEmail());
            }
            existingUser.setEmail(userUpdates.getEmail());
        }
        if (userUpdates.getDepartment() != null) {
            existingUser.setDepartment(userUpdates.getDepartment());
        }
        if (userUpdates.getRole() != null) {
            existingUser.setRole(userUpdates.getRole());
        }

        User savedUser = userRepository.save(existingUser);
        logger.info("Updated user: {}", savedUser.getEmployeeId());
        return savedUser;
    }

    public void changePassword(String employeeId, String newPassword) {
        User user = getUserByEmployeeId(employeeId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed for user: {}", employeeId);
    }

    public void deactivateUser(UUID id) {
        User user = getUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
        logger.info("Deactivated user: {}", user.getEmployeeId());
    }

    public void activateUser(UUID id) {
        User user = getUserById(id);
        user.setIsActive(true);
        userRepository.save(user);
        logger.info("Activated user: {}", user.getEmployeeId());
    }

    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return userRepository.existsByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    @Transactional(readOnly = true)
    public long countActiveUsersByDepartment(UUID departmentId) {
        return userRepository.countActiveUsersByDepartment(departmentId);
    }
}