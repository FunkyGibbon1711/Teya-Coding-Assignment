package com.project.teya.ledger.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({NegativeAmountException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class,
            Exception.class})
    public Response badRequest(Exception ex) {
        log.error("Bad request received: ", ex);

        return new Response(ErrorCodes.BAD_REQUEST.name(), String.format("%s - %s", ErrorCodes.BAD_REQUEST.getReason(), ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public Response notFound(Exception ex) {
        log.error("Record not found: ", ex);

        return new Response(ErrorCodes.NOT_FOUND.name(), ErrorCodes.NOT_FOUND.getReason());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ConflictException.class})
    public Response conflict(Exception ex) {
        log.error("Duplicate transaction request received: ", ex);

        return new Response(ErrorCodes.CONFLICT.name(), ErrorCodes.CONFLICT.getReason());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({DatabaseException.class})
    public Response internalServerError(Exception ex) {
        log.error("Internal server error: ", ex);

        return new Response(ErrorCodes.INTERNAL_SERVER_ERROR.name(), ErrorCodes.INTERNAL_SERVER_ERROR.getReason());
    }

    @Getter
    @AllArgsConstructor
    private enum ErrorCodes {

        BAD_REQUEST("There is an issue with the rest you have sent"),
        CONFLICT("There has been a conflict while making the requested updates"),
        INTERNAL_SERVER_ERROR("There has been an issue processing your request"),
        NOT_FOUND("The entity you are searching for has not been found");

        private final String reason;
    }

    @AllArgsConstructor
    @Getter
    public static class Response {
        private String code;

        private String reason;
    }
}
