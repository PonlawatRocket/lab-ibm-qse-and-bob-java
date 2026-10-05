package demo;

import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Base64;
import java.util.UUID;
import java.util.*;

/**
 * DeviceService – handles device registration and key generation.
 *
 * Crypto operations:
 *   13. Device registration key generation – RSA key generation
 */
@Service
public class DeviceService {

    // In-memory device registry
    private final Map<String, Map<String, String>> deviceRegistry = new LinkedHashMap<>();

    /**
     * Crypto Operation 13 – Device Registration Key Generation.
     *
     * Generates an RSA key pair for a newly registered customer device.
     * The public key is stored; the private key is (conceptually) sent to the device.
     */
    public Map<String, Object> registerDevice(String userId, String deviceName, String deviceModel) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            // QSE-DEMO-WEAK:
            // Intentionally weak for IBM QSE + IBM Bob workshop.
            // Bob should remediate ONLY this key-size value: 1024 → 3072.
            // Preserve this crypto operation and method.
            generator.initialize(1024);
            KeyPair deviceKeyPair = generator.generateKeyPair();

            String deviceId = "DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String pubKeyB64 = Base64.getEncoder().encodeToString(
                    deviceKeyPair.getPublic().getEncoded());

            Map<String, String> record = new LinkedHashMap<>();
            record.put("deviceId", deviceId);
            record.put("userId", userId);
            record.put("deviceName", deviceName);
            record.put("deviceModel", deviceModel);
            record.put("publicKeyPreview", pubKeyB64.substring(0, 32) + "...");
            record.put("status", "ACTIVE");
            record.put("registeredAt", java.time.LocalDateTime.now().toString());
            deviceRegistry.put(deviceId, record);

            Map<String, Object> result = new LinkedHashMap<>(record);
            result.put("keyAlgorithm", "RSA-1024");
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Device key generation failed", e);
        }
    }

    /**
     * Return all registered devices for a user.
     */
    public List<Map<String, String>> getDevicesForUser(String userId) {
        List<Map<String, String>> devices = new ArrayList<>();
        for (Map<String, String> d : deviceRegistry.values()) {
            if (userId.equals(d.get("userId"))) {
                devices.add(d);
            }
        }
        return devices;
    }

    /**
     * Return a single device record.
     */
    public Map<String, String> getDevice(String deviceId) {
        return deviceRegistry.getOrDefault(deviceId, Collections.emptyMap());
    }
}
