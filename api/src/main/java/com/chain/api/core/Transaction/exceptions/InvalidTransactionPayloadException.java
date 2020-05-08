package com.chain.api.core.Transaction.exceptions;

public class InvalidTransactionPayloadException extends RuntimeException  {
    public InvalidTransactionPayloadException() {
        super();
    }

    public InvalidTransactionPayloadException(String message) {
        super(message);
    }

    public InvalidTransactionPayloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTransactionPayloadException(Throwable cause) {
        super(cause);
    }
}
