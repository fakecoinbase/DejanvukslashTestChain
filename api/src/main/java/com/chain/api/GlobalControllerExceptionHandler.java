package com.chain.api;

import com.chain.api.core.Transaction.exceptions.CreateTransactionException;
import com.chain.api.core.Transaction.exceptions.InvalidTransactionPayloadException;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(CreateTransactionException.class)
    public HttpError handleCreateTransactionExceptions(Exception ex) {
        return createHttpError(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ResponseStatus()
    @ExceptionHandler(InvalidTransactionPayloadException.class)
    public HttpError handleInvalidTransactionPayloadExceptions(Exception ex) {
        return createHttpError(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    private HttpError createHttpError(HttpStatus httpStatus, Exception ex) {
        LOG.debug("Exception: {} message: {}", HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        return new HttpError(httpStatus, ZonedDateTime.now(), ex.getMessage());
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private class HttpError {
        private HttpStatus httpStatus;
        private ZonedDateTime timestamp;
        private String message;
    }
}
