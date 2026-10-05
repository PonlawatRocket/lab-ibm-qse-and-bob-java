package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * DemoController – serves the operator/workshop pages.
 *
 * The /demo page is for internal IBM QSE + IBM Bob workshop operators only.
 * It is NOT linked from the main banking UI.
 *
 * Routes:
 *   GET  /demo           → operator overview page (HTML redirect to /demo.html is not needed;
 *                          we serve operator data via /api/demo/* and the static demo.html page)
 *   GET  /api/demo/info  → JSON summary of crypto operations for the operator panel
 *   POST /api/demo/run   → trigger a named crypto scenario for live demo
 */
@Controller
public class DemoController {

    @Autowired private AuthService authService;
    @Autowired private TransferService transferService;
    @Autowired private SlipService slipService;
    @Autowired private AuditService auditService;
    @Autowired private DeviceService deviceService;
    @Autowired private PartnerService partnerService;
    @Autowired private ProfileService profileService;

    /**
     * Serve the demo/operator page.
     * Static file at /static/demo.html is served automatically by Spring Boot,
     * so this just provides a clean redirect.
     */
    @GetMapping("/demo")
    public String demoPage() {
        return "redirect:/demo.html";
    }

    /**
     * JSON: full crypto asset inventory for the operator panel.
     */
    @GetMapping("/api/demo/info")
    @ResponseBody
    public Map<String, Object> demoInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("appName", "NovaPay Mobile Banking");
        info.put("purpose", "IBM QSE + IBM Bob Workshop Demo");
        info.put("note",    "This endpoint is for workshop operators only.");

        info.put("cryptoAssets", buildCryptoAssetInventory());
        info.put("weakAlgorithmTable", buildWeakAlgorithmTable());
        info.put("featureToCryptoMap", buildFeatureToCryptoMap());
        return info;
    }

    /**
     * Run a named crypto scenario (for live demo of findings).
     */
    @PostMapping("/api/demo/run")
    @ResponseBody
    public Map<String, Object> runScenario(@RequestBody Map<String, String> body) {
        String scenario = body.getOrDefault("scenario", "login");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", scenario);

        switch (scenario) {
            case "login" -> {
                String token = authService.createSessionToken("demo-user");
                result.put("operation", "Login Token Signing (SHA1withRSA / RSA-1024)");
                result.put("token", token);
                result.put("status", "completed");
            }
            case "transfer" -> {
                Map<String, Object> tx = transferService.executeTransfer(
                        "NP-0001-2024", "NP-0002-2024", "1000.00", "Demo transfer");
                result.put("operation", "Transfer Auth Sign + HMAC + Checksum");
                result.put("transfer", tx);
                result.put("status", "completed");
            }
            case "slip" -> {
                Map<String, Object> slip = slipService.buildSignedSlip(
                        "TXN-DEMO-001", "NP-0001-2024", "NP-0002-2024",
                        "1000.00", java.time.LocalDateTime.now().toString(), "Demo e-slip");
                result.put("operation", "E-Slip Signing + Statement Checksum");
                result.put("slip", slip);
                result.put("status", "completed");
            }
            case "device" -> {
                Map<String, Object> dev = deviceService.registerDevice(
                        "demo-user", "Demo Phone", "Android 14");
                result.put("operation", "Device Registration Key Generation (RSA-1024)");
                result.put("device", dev);
                result.put("status", "completed");
            }
            case "partner" -> {
                Map<String, Object> kx = partnerService.performPartnerKeyExchange("DEMO-BANK-001");
                result.put("operation", "Partner Key Exchange (DH-1024)");
                result.put("keyExchange", kx);
                result.put("status", "completed");
            }
            case "audit" -> {
                auditService.logEvent("demo-user", "DEMO_RUN", "Audit HMAC scenario executed");
                result.put("operation", "Audit Log HMAC (HmacSHA1)");
                result.put("auditLog", auditService.getAuditLog());
                result.put("status", "completed");
            }
            case "profile" -> {
                Map<String, Object> profile = profileService.getProfile("alice");
                result.put("operation", "Profile Checksum (SHA-1)");
                result.put("profile", profile);
                result.put("status", "completed");
            }
            default -> {
                result.put("status", "unknown scenario");
            }
        }
        return result;
    }

    /**
     * Return audit log for operator inspection.
     */
    @GetMapping("/api/demo/audit")
    @ResponseBody
    public List<Map<String, String>> getAuditLog() {
        return auditService.getAuditLog();
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private List<Map<String, Object>> buildCryptoAssetInventory() {
        List<Map<String, Object>> assets = new ArrayList<>();

        Object[][] data = {
            {1,  "Login Key Generation",          "AuthService",     "generateLoginKeyPair",          "RSA",           "1024",       "KeyPairGenerator.initialize(1024)",     "3072"},
            {2,  "Login Token Signing",            "AuthService",     "signLoginToken",                "SHA1withRSA",   "RSA-1024",   "Signature.getInstance(\"SHA1withRSA\")", "SHA256withRSA"},
            {3,  "Login Token Verification",       "AuthService",     "verifyLoginToken",              "SHA1withRSA",   "RSA-1024",   "Signature.getInstance(\"SHA1withRSA\")", "SHA256withRSA"},
            {4,  "Transfer Key Generation",        "TransferService", "generateTransferKeyPair",       "RSA",           "1024",       "KeyPairGenerator.initialize(1024)",     "3072"},
            {5,  "Transfer Auth Signing",          "TransferService", "signTransferAuthorization",     "SHA1withRSA",   "RSA-1024",   "Signature.getInstance(\"SHA1withRSA\")", "SHA256withRSA"},
            {6,  "Transfer Verification",          "TransferService", "verifyTransferAuthorization",   "SHA1withRSA",   "RSA-1024",   "Signature.getInstance(\"SHA1withRSA\")", "SHA256withRSA"},
            {7,  "Transaction Integrity HMAC",     "TransferService", "computeTransactionHmac",        "HmacSHA1",      "—",          "Mac.getInstance(\"HmacSHA1\")",         "HmacSHA256"},
            {8,  "E-Slip Key Generation",          "SlipService",     "generateSlipKeyPair",           "RSA",           "1024",       "KeyPairGenerator.initialize(1024)",     "3072"},
            {9,  "E-Slip Signing",                 "SlipService",     "signESlip",                     "SHA1withRSA",   "RSA-1024",   "Signature.getInstance(\"SHA1withRSA\")", "SHA256withRSA"},
            {10, "E-Slip Verification",            "SlipService",     "verifyESlip",                   "SHA1withRSA",   "RSA-1024",   "Signature.getInstance(\"SHA1withRSA\")", "SHA256withRSA"},
            {11, "Audit-Log HMAC",                 "AuditService",    "computeAuditEntryHmac",         "HmacSHA1",      "—",          "Mac.getInstance(\"HmacSHA1\")",         "HmacSHA256"},
            {12, "Transaction Checksum",           "TransferService", "computeTransactionChecksum",    "SHA-1",         "—",          "MessageDigest.getInstance(\"SHA-1\")",  "SHA-256"},
            {13, "Statement Checksum",             "SlipService",     "computeStatementChecksum",      "SHA-1",         "—",          "MessageDigest.getInstance(\"SHA-1\")",  "SHA-256"},
            {14, "Device Registration Key Gen",    "DeviceService",   "registerDevice",                "RSA",           "1024",       "KeyPairGenerator.initialize(1024)",     "3072"},
            {15, "Partner Key Exchange",           "PartnerService",  "performPartnerKeyExchange",     "DH",            "1024",       "dhGen.initialize(1024)",                "2048"},
            {16, "Profile Data Checksum",          "ProfileService",  "computeProfileChecksum",        "SHA-1",         "—",          "MessageDigest.getInstance(\"SHA-1\")",  "SHA-256"},
        };

        for (Object[] row : data) {
            Map<String, Object> asset = new LinkedHashMap<>();
            asset.put("id",            row[0]);
            asset.put("name",          row[1]);
            asset.put("class",         row[2]);
            asset.put("method",        row[3]);
            asset.put("algorithm",     row[4]);
            asset.put("keySize",       row[5]);
            asset.put("weakCode",      row[6]);
            asset.put("bobRemediation",row[7]);
            assets.add(asset);
        }
        return assets;
    }

    private List<Map<String, String>> buildWeakAlgorithmTable() {
        List<Map<String, String>> table = new ArrayList<>();
        String[][] rows = {
            {"RSA key size",    "1024",          "3072"},
            {"DH key size",     "1024",          "2048"},
            {"RSA signature",   "SHA1withRSA",   "SHA256withRSA"},
            {"HMAC",            "HmacSHA1",      "HmacSHA256"},
            {"Message digest",  "SHA-1",         "SHA-256"},
        };
        for (String[] row : rows) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("category", row[0]);
            entry.put("weakValue", row[1]);
            entry.put("afterBob",  row[2]);
            table.add(entry);
        }
        return table;
    }

    private Map<String, List<String>> buildFeatureToCryptoMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Login",                  List.of("Login Key Generation", "Login Token Signing", "Login Token Verification"));
        map.put("Fund Transfer",          List.of("Transfer Key Generation", "Transfer Auth Signing", "Transfer Verification", "Transaction Integrity HMAC", "Transaction Checksum"));
        map.put("E-Slip / Receipt",       List.of("E-Slip Key Generation", "E-Slip Signing", "E-Slip Verification", "Statement Checksum"));
        map.put("Audit Trail",            List.of("Audit-Log HMAC"));
        map.put("Device Registration",    List.of("Device Registration Key Gen"));
        map.put("Partner Settlement",     List.of("Partner Key Exchange"));
        map.put("Profile / Security",     List.of("Profile Data Checksum"));
        return map;
    }
}
