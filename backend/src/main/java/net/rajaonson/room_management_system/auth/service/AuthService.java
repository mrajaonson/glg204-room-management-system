package net.rajaonson.room_management_system.auth.service;

import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.repository.AccountRepository;
import net.rajaonson.room_management_system.auth.service.dto.LoginDto;
import net.rajaonson.room_management_system.auth.service.dto.TokenResponseDto;
import net.rajaonson.room_management_system.config.JwtProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(AuthenticationManager authenticationManager,
                       AccountRepository accountRepository,
                       JwtService jwtService,
                       JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public TokenResponseDto authenticate(LoginDto dto) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(dto.getLogin(), dto.getPassword()));
        } catch (AuthenticationException e) {
            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED);
        }

        Account account = accountRepository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new ErrorResponseException(HttpStatus.UNAUTHORIZED));

        return new TokenResponseDto(jwtService.generateToken(account), TOKEN_TYPE, jwtProperties.expiration().toSeconds());
    }
}
