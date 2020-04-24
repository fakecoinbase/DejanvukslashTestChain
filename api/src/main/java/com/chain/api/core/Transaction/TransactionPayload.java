package com.chain.api.core.Transaction;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPayload {
    @NotBlank(message = "Private key of sender cannot be empty!")
    private String from;

    @NotBlank(message = "Public key of receiver cannot be empty!")
    private String to;

    @NotBlank(message = "Value cannot be empty!")
    private float value;
}
