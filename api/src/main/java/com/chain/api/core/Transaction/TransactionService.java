package com.chain.api.core.Transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface TransactionService {
    @PostMapping(value = "/transaction", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionPayload payload, BindingResult bindingResult);
}
