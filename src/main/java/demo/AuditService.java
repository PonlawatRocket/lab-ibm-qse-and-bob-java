package demo;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AuditService – append-only audit log with HMAC-protected entries.
 *
 * Crypto operations:
 *   10. Audit-log HMAC – HmacSHA1
 */
@Service
public class AuditService {

    private static final byte[] AUDIT_HMAC_KEY = "novapay-audit-integrity-key-2024".getBytes();
    private final List<Map<String, String>> auditLog = new ArrayList<>();

    /**
     * Crypto Operation 10 – Audit-Log HMAC.
     *
     * Produces an HMAC over each audit entry to detect log tampering.
     */
    public String computeAuditEntryHmac(String auditEntry) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: HmacSHA1 → HmacSHA256.
            // Preserve this crypto operation and method.
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(AUDIT_HMAC_KEY, "HmacSHA1");
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(auditEntry.getBytes());
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Audit HMAC failed", e);
        }
    }

    /**
     * Append a new entry to the audit log with an integrity HMAC.
     */
    public void logEvent(String userId, String action, String detail) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String entry = timestamp + "|" + userId + "|" + action + "|" + detail;
        String hmac = computeAuditEntryHmac(entry);

        Map<String, String> record = new LinkedHashMap<>();
        record.put("timestamp", timestamp);
        record.put("userId", userId);
        record.put("action", action);
        record.put("detail", detail);
        record.put("hmac", hmac.substring(0, Math.min(16, hmac.length())) + "...");
        auditLog.add(record);
    }

    /**
     * Return all audit log entries (in-memory).
     */
    public List<Map<String, String>> getAuditLog() {
        return Collections.unmodifiableList(auditLog);
    }
}
