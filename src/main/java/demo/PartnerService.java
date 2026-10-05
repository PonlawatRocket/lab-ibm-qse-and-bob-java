package demo;

import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PartnerService – manages partner-bank secure connection key exchange.
 *
 * Crypto operations:
 *   14. Partner-bank key exchange – Diffie-Hellman key agreement
 */
@Service
public class PartnerService {

    /**
     * Crypto Operation 14 – Partner-Bank Secure Connection Key Exchange.
     *
     * Performs a Diffie-Hellman key exchange simulation to establish a shared
     * session key for inter-bank settlement communication.
     */
    public Map<String, Object> performPartnerKeyExchange(String partnerBankId) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this key-size value: 1024 → 2048.
            // Preserve this crypto operation and method.
            KeyPairGenerator dhGen = KeyPairGenerator.getInstance("DH");
            dhGen.initialize(1024);
            KeyPair localKeyPair = dhGen.generateKeyPair();

            // Simulate partner's DH public key (same parameters for demo)
            KeyPairGenerator partnerGen = KeyPairGenerator.getInstance("DH");
            partnerGen.initialize(1024);
            KeyPair partnerKeyPair = partnerGen.generateKeyPair();

            // Local side key agreement
            KeyAgreement localKA = KeyAgreement.getInstance("DH");
            localKA.init(localKeyPair.getPrivate());
            localKA.doPhase(partnerKeyPair.getPublic(), true);
            byte[] sharedSecret = localKA.generateSecret();

            String sharedSecretPreview = Base64.getEncoder().encodeToString(sharedSecret);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("partnerBankId", partnerBankId);
            result.put("keyExchangeAlgorithm", "DH-1024");
            result.put("sessionKeyPreview",
                    sharedSecretPreview.substring(0, Math.min(24, sharedSecretPreview.length())) + "...");
            result.put("status", "ESTABLISHED");
            result.put("establishedAt", java.time.LocalDateTime.now().toString());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Partner key exchange failed", e);
        }
    }
}
