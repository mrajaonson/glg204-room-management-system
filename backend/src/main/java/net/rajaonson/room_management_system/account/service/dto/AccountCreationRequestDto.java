package net.rajaonson.room_management_system.account.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountCreationRequestDto {

    @NotBlank
    @Size(min = 3, max = 50)
    private String login;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    private String passwordConfirmation;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    public AccountCreationRequestDto() {
    }

    public AccountCreationRequestDto(String login, String password, String passwordConfirmation, String email) {
        this.login = login;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        this.email = email;
    }

    @JsonIgnore
    @AssertTrue(message = "password confirmation does not match")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirmation);
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
