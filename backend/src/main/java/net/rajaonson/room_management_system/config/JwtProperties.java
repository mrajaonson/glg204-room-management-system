package net.rajaonson.room_management_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, @DurationUnit(ChronoUnit.SECONDS) Duration expiration) {
}
