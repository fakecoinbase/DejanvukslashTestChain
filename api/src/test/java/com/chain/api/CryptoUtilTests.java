package com.chain.api;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Wallet.WalletUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
public class CryptoUtilTests {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @BeforeEach
    public void setup() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // generate random wallet
        KeyPair keyPair = WalletUtil.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();

        // add some test UTXO's
    }

    @Test
    public void testEncryption() {
        String encryptedString = CryptoUtil.encryptSha256("TEST STRING");
        byte[] byteArraySignature = CryptoUtil.applyECDSASig(privateKey, encryptedString);

        assertTrue(CryptoUtil.verifyECDSASig(publicKey,encryptedString,byteArraySignature));

        String encryptedModifiedString = CryptoUtil.encryptSha256("TEST MODIFIED STRING");

        assertFalse(CryptoUtil.verifyECDSASig(publicKey,encryptedModifiedString,byteArraySignature));
    }

    /**
     *
     */
    @Test
    public void testByteToStringConversions() {
        String publicKeyString = "TEST STRING";
        String hexString = String.format("%040x", new BigInteger(1, publicKeyString.getBytes(/*YOUR_CHARSET?*/)));
        byte[] stringToByte = CryptoUtil.hexStringToByteArray(hexString); // String to byte array
        String byteToString = CryptoUtil.byteArrayToHexString(stringToByte); // byte array to String

        assertEquals(hexString,byteToString);
    }

    /**
     * PublicKey,PrivateKey conversion to String and back
     */
    @Test
    public void testStringKeyConversions() {
        String publicKeyString = CryptoUtil.getStringFromKey(publicKey);                            // PublicKey to String
        String privateKeyString = CryptoUtil.getStringFromKey(privateKey);                          // PrivateKey to String
        try {
            PublicKey generatedPublicKey = CryptoUtil.getPublicKeyFromString(publicKeyString);      // String to PublicKey
            String generatedPublicKeyString = CryptoUtil.getStringFromKey(generatedPublicKey);      // PublicKey to String

            PrivateKey generatedPrivateKey = CryptoUtil.getPrivateKeyFromString(privateKeyString);  // String to PrivateKey
            String generatedPrivateKeyString = CryptoUtil.getStringFromKey(generatedPrivateKey);    // PrivateKey to String

            assertEquals(publicKeyString, generatedPublicKeyString);
            assertEquals(privateKeyString, generatedPrivateKeyString);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test that the public key generated using the private key is the same as the original one
     */
    @Test
    public void testDerivePubKeyFromPrivKey() {
        String publicKeyString = CryptoUtil.getStringFromKey(publicKey);
        try {
            PublicKey derivedPublicKey = CryptoUtil.DerivePubKeyFromPrivKey((BCECPrivateKey) privateKey);
            String derivedPublicKeyString = CryptoUtil.getStringFromKey(derivedPublicKey);

            assertEquals(publicKeyString, derivedPublicKeyString);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}
