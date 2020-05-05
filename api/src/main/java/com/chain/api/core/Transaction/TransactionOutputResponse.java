package com.chain.api.core.Transaction;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOutputResponse {
    private String to;
    private float value;
}
