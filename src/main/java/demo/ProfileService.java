package demo;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * ProfileService – customer profile and security status.
 *
 * Crypto operations:
 *   15. Profile data checksum – SHA-1 MessageDigest
 */
@Service
public class ProfileService {

    // Simulated in-memory customer profiles
    private static final Map<String, Map<String, Object>> PROFILES = new LinkedHashMap<>();

    static {
        Map<String, Object> alice = new LinkedHashMap<>();
        alice.put("userId", "alice");
        alice.put("fullName", "Alice Sutton");
        alice.put("email", "alice.sutton@example.com");
        alice.put("phone", "+1 (555) 012-3456");
        alice.put("tier", "Gold");
        alice.put("accountNumber", "NP-0001-2024");
        alice.put("savingsBalance", 48750.00);
        alice.put("currentBalance", 12340.50);
        alice.put("cardLast4", "4782");
        alice.put("lastLogin", "2025-01-15 09:34:22");
        PROFILES.put("alice", alice);

        Map<String, Object> bob = new LinkedHashMap<>();
        bob.put("userId", "bob");
        bob.put("fullName", "Bob Chen");
        bob.put("email", "bob.chen@example.com");
        bob.put("phone", "+1 (555) 987-6543");
        bob.put("tier", "Platinum");
        bob.put("accountNumber", "NP-0002-2024");
        bob.put("savingsBalance", 125000.00);
        bob.put("currentBalance", 33200.75);
        bob.put("cardLast4", "9341");
        bob.put("lastLogin", "2025-01-15 11:22:05");
        PROFILES.put("bob", bob);
    }

    /**
     * Crypto Operation 15 – Profile Data Checksum.
     *
     * Computes a SHA-1 checksum over serialized profile data to detect changes.
     */
    public String computeProfileChecksum(String profileData) {
        try {
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this algorithm string: SHA-1 → SHA-256.
            // Preserve this crypto operation and method.
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(profileData.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Profile checksum failed", e);
        }
    }

    /**
     * Deduct amount from the sender's currentBalance in-memory.
     */
    public void deductBalance(String userId, double amount) {
        Map<String, Object> profile = PROFILES.get(userId);
        if (profile == null) return;
        double current = ((Number) profile.get("currentBalance")).doubleValue();
        profile.put("currentBalance", current - amount);
    }

    /**
     * Return profile for a user, including a freshly computed checksum.
     */
    public Map<String, Object> getProfile(String userId) {
        Map<String, Object> profile = PROFILES.getOrDefault(userId, defaultProfile(userId));
        String profileData = profile.toString();
        String checksum = computeProfileChecksum(profileData);

        Map<String, Object> result = new LinkedHashMap<>(profile);
        result.put("profileChecksum", checksum.substring(0, 12) + "...");
        return result;
    }

    private Map<String, Object> defaultProfile(String userId) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", userId);
        profile.put("fullName", "Demo User");
        profile.put("email", "demo@novapay.example.com");
        profile.put("phone", "+1 (555) 000-0000");
        profile.put("tier", "Standard");
        profile.put("accountNumber", "NP-0000-2024");
        profile.put("savingsBalance", 1000.00);
        profile.put("currentBalance", 500.00);
        profile.put("cardLast4", "0000");
        profile.put("lastLogin", "2025-01-01 00:00:00");
        return profile;
    }

    /**
     * Return list of recent transactions for a user (simulated).
     */
    public List<Map<String, Object>> getRecentTransactions(String userId) {
        List<Map<String, Object>> txns = new ArrayList<>();

        String[][] data = {
            {"TXN-A0012", "Netflix Subscription",   "-12.99",  "2025-01-15", "debit",  "Entertainment"},
            {"TXN-A0011", "Salary Deposit",          "+5200.00","2025-01-14", "credit", "Income"},
            {"TXN-A0010", "Starbucks Coffee",        "-6.50",   "2025-01-14", "debit",  "Food & Drink"},
            {"TXN-A0009", "Amazon Purchase",         "-84.30",  "2025-01-13", "debit",  "Shopping"},
            {"TXN-A0008", "Transfer to Jane",        "-500.00", "2025-01-13", "debit",  "Transfer"},
            {"TXN-A0007", "Electricity Bill",        "-120.00", "2025-01-12", "debit",  "Utilities"},
            {"TXN-A0006", "Freelance Payment",       "+850.00", "2025-01-11", "credit", "Income"},
            {"TXN-A0005", "Grocery Store",           "-67.45",  "2025-01-10", "debit",  "Shopping"},
        };

        for (String[] row : data) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("id", row[0]);
            t.put("description", row[1]);
            t.put("amount", row[2]);
            t.put("date", row[3]);
            t.put("type", row[4]);
            t.put("category", row[5]);
            txns.add(t);
        }
        return txns;
    }
}
