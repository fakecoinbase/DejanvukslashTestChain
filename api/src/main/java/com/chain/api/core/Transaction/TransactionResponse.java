package com.chain.api.core.Transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.security.PublicKey;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String TXID; // also called TXHash, made by hashing transaction data through SHA256
    private String sender;
    private String receiver;
    private float value;

    private boolean isVerified;

    private String ownerBlock;

    private List<TransactionInput> inputs;
    private List<TransactionOutputResponse> outputs;
}
