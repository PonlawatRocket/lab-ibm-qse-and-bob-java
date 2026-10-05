package demo;

import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Base64;
import java.util.UUID;

/**
 * AuthService – handles login token signing and verification.
 *
 * Crypto operations:
 *   1. Login token signing   – RSA + SHA1withRSA
 *   2. Login token verify    – RSA + SHA1withRSA
 */
@Service
public class AuthService {

    private final KeyPair loginKeyPair;

    public AuthService() {
        this.loginKeyPair = generateLoginKeyPair();
    }

    /**
     * Crypto Operation 1 – Login Key Generation.
     *
     * // QSE-DEMO-WEAK:
     * // Intentionally weak for IBM QSE + IBM Bob workshop.
     * // Bob should remediate ONLY this key-size value: 1024 → 3072.
     * // Preserve this crypto operation and method.
     */
    private KeyPair generateLoginKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this key-size value: 1024 → 3072.
            // Preserve this crypto operation and method.
            generator.initialize(1024);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Crypto Operation 2 – Login Token Signing.
     *
     * Signs a login session token with the bank's RSA private key.
     * Returns Base64-encoded signature.
     */
    public String signLoginToken(String userId, String sessionId) {
        try {
            String payload = userId + ":" + sessionId + ":" + System.currentTimeMillis();
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA1withRSA → SHA256withRSA.
            // Preserve this crypto operation and method.
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(loginKeyPair.getPrivate());
            signer.update(payload.getBytes());
            byte[] sig = signer.sign();
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException("Login token signing failed", e);
        }
    }

    /**
     * Crypto Operation 3 – Login Token Verification.
     *
     * Verifies a previously signed login token signature.
     */
    public boolean verifyLoginToken(String payload, String signatureB64) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA1withRSA → SHA256withRSA.
            // Preserve this crypto operation and method.
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(loginKeyPair.getPublic());
            verifier.update(payload.getBytes());
            byte[] sig = Base64.getDecoder().decode(signatureB64);
            return verifier.verify(sig);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convenience: sign and return a session token for a given user.
     */
    public String createSessionToken(String userId) {
        String sessionId = UUID.randomUUID().toString();
        String sig = signLoginToken(userId, sessionId);
        return sessionId + "." + sig.substring(0, Math.min(16, sig.length()));
    }
}
