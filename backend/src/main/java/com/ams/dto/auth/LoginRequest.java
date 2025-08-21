package com.ams.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "社員番号は必須です")
    @Size(max = 20, message = "社員番号は20文字以内で入力してください")
    private String employeeId;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 6, max = 100, message = "パスワードは6文字以上100文字以内で入力してください")
    private String password;

    // Default constructor
    public LoginRequest() {
    }

    // Constructor
    public LoginRequest(String employeeId, String password) {
        this.employeeId = employeeId;
        this.password = password;
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "employeeId='" + employeeId + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}