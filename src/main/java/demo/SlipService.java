package demo;

import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Base64;

/**
 * SlipService – handles e-slip / receipt signing and verification.
 *
 * Crypto operations:
 *   8.  E-slip signing      – RSA + SHA1withRSA
 *   9.  E-slip verification – RSA + SHA1withRSA
 *  12.  Statement checksum  – SHA-1 MessageDigest
 */
@Service
public class SlipService {

    private final KeyPair slipKeyPair;

    public SlipService() {
        this.slipKeyPair = generateSlipKeyPair();
    }

    /**
     * Crypto Operation 8a – E-Slip Key Generation.
     */
    private KeyPair generateSlipKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this key-size value: 1024 → 3072.
            // Preserve this crypto operation and method.
            generator.initialize(1024);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Slip key generation failed", e);
        }
    }

    /**
     * Crypto Operation 8 – E-Slip Signing.
     *
     * Signs the receipt/slip payload so the customer can verify authenticity.
     */
    public String signESlip(String slipPayload) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA1withRSA → SHA256withRSA.
            // Preserve this crypto operation and method.
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(slipKeyPair.getPrivate());
            signer.update(slipPayload.getBytes());
            byte[] sig = signer.sign();
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException("E-slip signing failed", e);
        }
    }

    /**
     * Crypto Operation 9 – E-Slip Verification.
     *
     * Verifies the bank's signature on a downloaded e-slip.
     */
    public boolean verifyESlip(String slipPayload, String signatureB64) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA1withRSA → SHA256withRSA.
            // Preserve this crypto operation and method.
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(slipKeyPair.getPublic());
            verifier.update(slipPayload.getBytes());
            byte[] sig = Base64.getDecoder().decode(signatureB64);
            return verifier.verify(sig);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Crypto Operation 12 – Statement Checksum.
     *
     * Computes a SHA-1 digest of the full account statement to detect tampering.
     */
    public String computeStatementChecksum(String statementData) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA-1 → SHA-256.
            // Preserve this crypto operation and method.
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(statementData.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Statement checksum failed", e);
        }
    }

    /**
     * Build a signed e-slip for a completed transfer.
     */
    public java.util.Map<String, Object> buildSignedSlip(String reference, String from,
                                                          String to, String amount,
                                                          String timestamp, String note) {
        String payload = reference + "|" + from + "|" + to + "|" + amount + "|" + timestamp;
        String sig = signESlip(payload);
        String checksum = computeStatementChecksum(payload);

        java.util.Map<String, Object> slip = new java.util.LinkedHashMap<>();
        slip.put("reference", reference);
        slip.put("from", from);
        slip.put("to", to);
        slip.put("amount", amount);
        slip.put("timestamp", timestamp);
        slip.put("note", note);
        slip.put("signaturePreview", sig.substring(0, Math.min(24, sig.length())) + "...");
        slip.put("checksumPreview", checksum.substring(0, 16) + "...");
        slip.put("verified", true);
        slip.put("bankStamp", "NovaPay Bank N.A.");
        return slip;
    }
}
