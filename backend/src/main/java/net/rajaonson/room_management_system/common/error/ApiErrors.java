package net.rajaonson.room_management_system.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public final class ApiErrors {

    private ApiErrors() {}

    public static ErrorResponseException badRequest(String detail) {
        return of(HttpStatus.BAD_REQUEST, detail);
    }

    public static ErrorResponseException unauthorized(String detail) {
        return of(HttpStatus.UNAUTHORIZED, detail);
    }

    public static ErrorResponseException forbidden(String detail) {
        return of(HttpStatus.FORBIDDEN, detail);
    }

    public static ErrorResponseException notFound(String detail) {
        return of(HttpStatus.NOT_FOUND, detail);
    }

    public static ErrorResponseException conflict(String detail) {
        return of(HttpStatus.CONFLICT, detail);
    }

    private static ErrorResponseException of(HttpStatus status, String detail) {
        return new ErrorResponseException(status, ProblemDetail.forStatusAndDetail(status, detail), null);
    }
}
