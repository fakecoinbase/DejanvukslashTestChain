package com.chain.api.core.Block;

import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BlockResponse {
    private Integer index;
    private String hash;
    private String previousHash;
    private Long timestamp;
    private List<TransactionResponse> transactions;
    private String merkleRoot;
    private Integer difficultyTarget;
    private Integer nonce;
}
