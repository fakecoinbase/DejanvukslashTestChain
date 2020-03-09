package com.chain.api.core.Transaction;

import lombok.*;

import java.security.PublicKey;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOutput {
    private PublicKey to; // the address to which the coins will be locked to
    private float value;
}
