package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * BankController – REST API for the NovaPay mobile banking frontend.
 *
 * All endpoints here are consumed by the single-page banking UI (index.html).
 * They must feel like a real banking API.
 */
@RestController
@RequestMapping("/api")
public class BankController {

    @Autowired private AuthService authService;
    @Autowired private TransferService transferService;
    @Autowired private SlipService slipService;
    @Autowired private AuditService auditService;
    @Autowired private DeviceService deviceService;
    @Autowired private PartnerService partnerService;
    @Autowired private ProfileService profileService;

    // ─────────────────────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("username", "alice");
        String password = body.getOrDefault("password", "");

        // Simulate basic credential check (demo only)
        boolean valid = ("alice".equals(userId) && "pass123".equals(password))
                     || ("bob".equals(userId)   && "pass456".equals(password))
                     || ("demo".equals(userId)  && "demo".equals(password));

        if (!valid) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("success", false);
            err.put("message", "Invalid username or password.");
            return err;
        }

        String token = authService.createSessionToken(userId);
        auditService.logEvent(userId, "LOGIN", "Successful login");

        Map<String, Object> profile = profileService.getProfile(userId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("token", token);
        resp.put("userId", userId);
        resp.put("fullName", profile.get("fullName"));
        resp.put("tier", profile.get("tier"));
        resp.put("accountNumber", profile.get("accountNumber"));
        return resp;
    }

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/{userId}")
    public Map<String, Object> getDashboard(@PathVariable String userId) {
        Map<String, Object> profile = profileService.getProfile(userId);
        List<Map<String, Object>> transactions = profileService.getRecentTransactions(userId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("profile", profile);
        resp.put("recentTransactions", transactions);
        resp.put("notifications", getNotifications());
        return resp;
    }

    // ─────────────────────────────────────────────────────────────
    // TRANSFER
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/transfer")
    public Map<String, Object> transfer(@RequestBody Map<String, String> body) {
        String from  = body.getOrDefault("fromAccount", "NP-0001-2024");
        String to    = body.getOrDefault("toAccount",   "NP-0002-2024");
        String amount = body.getOrDefault("amount",     "100.00");
        String note  = body.getOrDefault("note",        "");
        String userId = body.getOrDefault("userId",     "alice");

        Map<String, Object> result = transferService.executeTransfer(from, to, amount, note);
        auditService.logEvent(userId, "TRANSFER", "Amount: " + amount + " → " + to);
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // E-SLIP
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/slip")
    public Map<String, Object> getSlip(@RequestBody Map<String, String> body) {
        String reference = body.getOrDefault("reference", "TXN-DEMO");
        String from      = body.getOrDefault("from",      "NP-0001-2024");
        String to        = body.getOrDefault("to",        "NP-0002-2024");
        String amount    = body.getOrDefault("amount",    "0.00");
        String timestamp = body.getOrDefault("timestamp", java.time.LocalDateTime.now().toString());
        String note      = body.getOrDefault("note",      "");

        return slipService.buildSignedSlip(reference, from, to, amount, timestamp, note);
    }

    // ─────────────────────────────────────────────────────────────
    // PROFILE
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/profile/{userId}")
    public Map<String, Object> getProfile(@PathVariable String userId) {
        return profileService.getProfile(userId);
    }

    // ─────────────────────────────────────────────────────────────
    // DEVICES
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/devices/{userId}")
    public List<Map<String, String>> getDevices(@PathVariable String userId) {
        List<Map<String, String>> devices = deviceService.getDevicesForUser(userId);
        if (devices.isEmpty()) {
            // Auto-register a demo device on first call
            deviceService.registerDevice(userId, "iPhone 15 Pro", "Apple iPhone");
        }
        return deviceService.getDevicesForUser(userId);
    }

    @PostMapping("/devices/register")
    public Map<String, Object> registerDevice(@RequestBody Map<String, String> body) {
        String userId      = body.getOrDefault("userId",      "alice");
        String deviceName  = body.getOrDefault("deviceName",  "My Phone");
        String deviceModel = body.getOrDefault("deviceModel", "Unknown");
        Map<String, Object> result = deviceService.registerDevice(userId, deviceName, deviceModel);
        auditService.logEvent(userId, "DEVICE_REGISTER", "Device: " + deviceName);
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/notifications")
    public List<Map<String, Object>> getNotificationsEndpoint() {
        return getNotifications();
    }

    // ─────────────────────────────────────────────────────────────
    // PARTNER KEY EXCHANGE (internal / settlement)
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/partner/exchange")
    public Map<String, Object> partnerExchange(@RequestBody Map<String, String> body) {
        String partnerId = body.getOrDefault("partnerId", "BANK-9001");
        return partnerService.performPartnerKeyExchange(partnerId);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private List<Map<String, Object>> getNotifications() {
        List<Map<String, Object>> notes = new ArrayList<>();
        String[][] data = {
            {"info",    "Your salary has been credited",                  "2 hours ago"},
            {"warning", "Unusual login detected from new device",         "Yesterday"},
            {"success", "Your transfer to Jane was successful",           "Jan 13"},
            {"info",    "New statement for December 2024 is available",   "Jan 1"},
        };
        for (String[] row : data) {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("type",    row[0]);
            n.put("message", row[1]);
            n.put("time",    row[2]);
            notes.add(n);
        }
        return notes;
    }
}
