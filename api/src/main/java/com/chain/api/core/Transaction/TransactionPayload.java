package com.chain.api.core.Transaction;

import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

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

    @Range(min = 0, max = 10, message = "Value cannot be empty!")
    private Float value;
}
