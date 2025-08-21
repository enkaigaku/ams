package com.ams.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @NotBlank(message = "部署名は必須です")
    @Size(max = 100, message = "部署名は100文字以内で入力してください")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "manager_id")
    private String managerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "employee_id", insertable = false, updatable = false)
    private User manager;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> employees = new ArrayList<>();

    // Default constructor
    public Department() {
    }

    // Constructor with name
    public Department(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public List<User> getEmployees() {
        return employees;
    }

    public void setEmployees(List<User> employees) {
        this.employees = employees;
    }

    @Override
    public String toString() {
        return "Department{" +
                "name='" + name + '\'' +
                ", managerId='" + managerId + '\'' +
                ", id=" + getId() +
                '}';
    }
}