package net.rajaonson.room_management_system.auth.service.dto;

public class TokenResponseDto {

    private final String token;
    private final String type;
    private final long expiresIn;

    public TokenResponseDto(String token, String type, long expiresIn) {
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
