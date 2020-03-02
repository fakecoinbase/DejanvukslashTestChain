package com.chain.api.core.Transaction;

import lombok.*;

import java.security.PublicKey;


@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
//keep a database of UTXO so we don't have to scan the blockchain everytime we make a transaction
public class UTXO {
    private String previousTx;
    private Integer index;
    private PublicKey owner; // owner of the coins
    private float value;
}
