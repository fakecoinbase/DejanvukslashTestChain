package com.chain.api.core.Wallet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface WalletService {
    @GetMapping(value = "/wallet/{walletPublicKey}", produces = "application/json")
    ResponseEntity<?> getUsersBalance(@PathVariable String walletPublicKey);

    @GetMapping(value = "/wallet", produces = "application/json")
    ResponseEntity<?> generateWallet();

    /**
     * Spring boot restriction of /
     * use POST or requestparam
     */
    @GetMapping(value = "/address/foo", produces = "application/json")
    ResponseEntity<?> getUsersTransaction(@RequestParam String walletPublicKey);
}
