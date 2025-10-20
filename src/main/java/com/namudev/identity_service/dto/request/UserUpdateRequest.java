package com.namudev.identity_service.dto.request;

import java.time.LocalDate;

public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private LocalDate dob;

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(String firstName, String lastName, LocalDate dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
}
