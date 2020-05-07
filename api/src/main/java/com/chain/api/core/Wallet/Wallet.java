package com.chain.api.core.Wallet;

import com.chain.api.core.Transaction.TransactionOutput;
import lombok.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    public String privateKey;
    public String publicKey;
}
