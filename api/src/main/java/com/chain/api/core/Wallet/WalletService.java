package com.chain.api.core.Wallet;

public interface WalletService {
    String createWallet();

    String getWalletBalance(String publicKey);
}
