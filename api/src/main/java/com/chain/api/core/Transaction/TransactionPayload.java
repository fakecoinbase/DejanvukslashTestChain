package com.chain.api.core.Transaction;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPayload {
    private String from;
    private String to;
    private String value;
}
