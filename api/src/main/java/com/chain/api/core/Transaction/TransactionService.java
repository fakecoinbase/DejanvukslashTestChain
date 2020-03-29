package com.chain.api.core.Transaction;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface TransactionService {
    @PostMapping(value = "/transaction", consumes = "application/json", produces = "application/json")
    Transaction createTransaction(@RequestBody TransactionPayload payload);
}
