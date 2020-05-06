package com.chain.api.core.Transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface TransactionService {
    @PostMapping(value = "/transaction", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionPayload payload, BindingResult bindingResult);

    @GetMapping(value = "/transaction" ,produces = "application/json")
    ResponseEntity<?> getUnconfirmedTransactions();

    @GetMapping(value = "/transaction/{txid}" ,produces = "application/json")
    ResponseEntity<?> getTransaction(@PathVariable String txid);
}
