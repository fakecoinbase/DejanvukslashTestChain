package com.chain.api.core.Wallet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface WalletService {
    @GetMapping(value = "/wallet/{walletPublicKey}", produces = "application/json")
    Mono<String> getUsersBalance(@PathVariable String walletPublicKey);
}
