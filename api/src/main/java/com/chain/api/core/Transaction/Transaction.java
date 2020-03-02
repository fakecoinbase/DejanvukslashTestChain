package com.chain.api.core.Transaction;

import com.chain.util.crypto.CryptoUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Transaction {
    private Integer versionNo; // 1
    private Short Flag; // If present, always 0001, and indicates the presence of witness data
    private String TXID; // also called TXHash, made by hashing transaction data through SHA256
    private PublicKey sender;
    private PublicKey receiver;
    private float value;

    private List<TransactionInput> inputs = new ArrayList<TransactionInput>();
    private List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();


    // Constructor:
    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs, ArrayList<TransactionOutput> outputs) {
        this.sender = from;
        this.receiver = to;
        this.value = value;
        this.inputs = inputs;
        this.outputs = outputs;
    }
}
