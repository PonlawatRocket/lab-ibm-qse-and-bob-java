package demo;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * TransferService – handles fund transfer authorization and integrity.
 *
 * Crypto operations:
 *   4. Transfer authorization signing   – RSA + SHA1withRSA
 *   5. Transfer verification             – RSA + SHA1withRSA
 *   7. Transaction integrity HMAC        – HmacSHA1
 *  11. Transaction checksum              – SHA-1 MessageDigest
 */
@Service
public class TransferService {

    private final KeyPair transferKeyPair;
    private static final byte[] HMAC_KEY = "novapay-transfer-secret-key-2024".getBytes();

    public TransferService() {
        this.transferKeyPair = generateTransferKeyPair();
    }

    /**
     * Crypto Operation 4 – Transfer Key Generation.
     */
    private KeyPair generateTransferKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this key-size value: 1024 → 3072.
            // Preserve this crypto operation and method.
            generator.initialize(1024);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Transfer key generation failed", e);
        }
    }

    /**
     * Crypto Operation 5 – Transfer Authorization Signing.
     *
     * Signs the transfer instruction payload before it is submitted.
     */
    public String signTransferAuthorization(String fromAccount, String toAccount,
                                             String amount, String reference) {
        try {
            String payload = fromAccount + "|" + toAccount + "|" + amount + "|" + reference;
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA1withRSA → SHA256withRSA.
            // Preserve this crypto operation and method.
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(transferKeyPair.getPrivate());
            signer.update(payload.getBytes());
            byte[] sig = signer.sign();
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException("Transfer signing failed", e);
        }
    }

    /**
     * Crypto Operation 6 – Transfer Verification.
     *
     * Verifies the signed transfer payload on receipt.
     */
    public boolean verifyTransferAuthorization(String payload, String signatureB64) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA1withRSA → SHA256withRSA.
            // Preserve this crypto operation and method.
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(transferKeyPair.getPublic());
            verifier.update(payload.getBytes());
            byte[] sig = Base64.getDecoder().decode(signatureB64);
            return verifier.verify(sig);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Crypto Operation 7 – Transaction Integrity HMAC.
     *
     * Produces an HMAC of the transaction record for integrity verification.
     */
    public String computeTransactionHmac(String transactionData) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: HmacSHA1 → HmacSHA256.
            // Preserve this crypto operation and method.
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(HMAC_KEY, "HmacSHA1");
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(transactionData.getBytes());
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    /**
     * Crypto Operation 11 – Transaction Checksum.
     *
     * Computes a SHA-1 checksum of the full transaction blob.
     */
    public String computeTransactionChecksum(String transactionBlob) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA-1 → SHA-256.
            // Preserve this crypto operation and method.
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(transactionBlob.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Checksum failed", e);
        }
    }

    /**
     * Execute a demo transfer. Returns a transfer result map for the API.
     */
    public java.util.Map<String, Object> executeTransfer(String fromAccount, String toAccount,
                                                          String amount, String note) {
        String reference = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String payload = fromAccount + "|" + toAccount + "|" + amount + "|" + reference;

        String sig = signTransferAuthorization(fromAccount, toAccount, amount, reference);
        String hmac = computeTransactionHmac(payload);
        String checksum = computeTransactionChecksum(payload + "|" + timestamp);

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("reference", reference);
        result.put("fromAccount", fromAccount);
        result.put("toAccount", toAccount);
        result.put("amount", amount);
        result.put("note", note);
        result.put("timestamp", timestamp);
        result.put("status", "SUCCESS");
        result.put("signaturePreview", sig.substring(0, Math.min(20, sig.length())) + "...");
        result.put("hmacPreview", hmac.substring(0, Math.min(16, hmac.length())) + "...");
        result.put("checksum", checksum.substring(0, 12) + "...");
        return result;
    }
}
