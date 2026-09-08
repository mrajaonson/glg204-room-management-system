package net.rajaonson.room_management_system.auth.service;

import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.config.JwtProperties;
import net.rajaonson.room_management_system.config.SecurityConfig;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(Account account) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(account.getLogin())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .claim(SecurityConfig.ROLES_CLAIM, List.of(account.getRole().name()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpiresIn() {
        return jwtProperties.expiration().toSeconds();
    }
}
