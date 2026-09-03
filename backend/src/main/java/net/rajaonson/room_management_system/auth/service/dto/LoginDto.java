package net.rajaonson.room_management_system.auth.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public class LoginDto {

    @NotBlank
    @Size(min = 3, max = 50)
    private String login;

    @NotBlank
    private String password;

    public LoginDto() {
    }

    public LoginDto(String login, String password) {
        this.setLogin(login);
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login.toLowerCase(Locale.ROOT);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
