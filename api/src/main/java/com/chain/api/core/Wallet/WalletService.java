package com.chain.api.core.Wallet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface WalletService {
    @GetMapping(value = "/wallet/{walletPublicKey}", produces = "application/json")
    ResponseEntity<?> getUsersBalance(@PathVariable String walletPublicKey);

    @GetMapping(value = "/wallet", produces = "application/json")
    ResponseEntity<?> generateWallet();
}
