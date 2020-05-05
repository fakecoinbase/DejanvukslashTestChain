package com.chain.api.core.Transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.security.*;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Integer versionNo; // 1
    private Short Flag; // If present, always 0001, and indicates the presence of witness data
    private String TXID; // also called TXHash, made by hashing transaction data through SHA256
    private PublicKey sender;
    private PublicKey receiver;
    private float value;

    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;

}
