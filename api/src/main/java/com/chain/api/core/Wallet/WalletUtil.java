package com.chain.api.core.Wallet;

import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.UTXO;
import com.chain.util.crypto.CryptoUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class WalletUtil {
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random); // 256 bytes provides an acceptable security level
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            //privateKey = keyPair.getPrivate();
            //publicKey = keyPair.getPublic();
            return keyPair;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
