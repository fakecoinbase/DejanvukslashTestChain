package com.chain.api.core.Transaction.exceptions;

public class CreateTransactionException extends RuntimeException {
    public CreateTransactionException() {
    }

    public CreateTransactionException(String message) {
        super(message);
    }

    public CreateTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateTransactionException(Throwable cause) {
        super(cause);
    }
}
