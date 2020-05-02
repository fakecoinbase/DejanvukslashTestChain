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
    @JsonIgnore
    private PublicKey sender;
    @JsonIgnore
    private PublicKey receiver;
    private float value;


    @JsonIgnore
    private List<TransactionInput> inputs;
    @JsonIgnore
    private List<TransactionOutput> outputs;

}
