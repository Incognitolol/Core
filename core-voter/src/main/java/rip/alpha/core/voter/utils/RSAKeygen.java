package rip.alpha.core.voter.utils;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;


public class RSAKeygen {
    private static final SecureRandom RANDOM = new SecureRandom();
    /**
     * Generates an RSA key pair.
     *
     * @param bits The amount of bits
     * @return The key pair
     */
    public static KeyPair generate(int bits) throws Exception {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bits,
                RSAKeyGenParameterSpec.F4);
        keygen.initialize(spec);
        return keygen.generateKeyPair();
    }

    public static Key createKeyFrom(String token) {
        return new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
    public static String newToken() {
        return new BigInteger(130, RANDOM).toString(32);
    }
}
