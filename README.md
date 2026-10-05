# NovaPay Mobile Banking
## IBM QSE + IBM Bob Workshop Demo

---

## What is this?

**NovaPay** is a realistic mobile banking demo application built for the **IBM Quantum Safe Explorer (QSE)** + **IBM Bob** workshop.

It is designed to satisfy two goals simultaneously:

| Goal | Description |
|---|---|
| **Client-facing demo** | A polished, realistic mobile banking UI that can be shown to banking clients |
| **Workshop tool** | A backend with intentionally weak cryptography for QSE scanning + Bob remediation |

The main URL (`/`) opens a real-looking mobile banking app.
The operator panel (`/demo`) is for workshop use only and is not linked from the banking UI.

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run

```bash
# Windows
run.cmd

# Unix/macOS
mvn spring-boot:run
```

Then open: **http://localhost:8080**

**Demo credentials:**
| Username | Password |
|---|---|
| alice | pass123 |
| bob   | pass456 |

---

## Demo-Day UI Flow

The app presents a complete mobile banking experience:

| Screen | How to reach |
|---|---|
| **Login** | Landing page (`/`) |
| **Home Dashboard** | After login – balance, quick actions, recent transactions |
| **Accounts** | Bottom nav → Accounts tab |
| **Send Money** | Home quick action or centre FAB |
| **Transfer Success** | After submitting a transfer |
| **E-Slip / Receipt** | "View E-Slip" on success screen |
| **Profile & Security** | Bottom nav → Profile |
| **Registered Devices** | Profile → Manage Devices |
| **Notifications** | Bell icon or bottom nav → Alerts |

---

## Operator / Workshop Page

```
http://localhost:8080/demo
```

The operator panel shows:

- Workshop metrics (findings before/after Bob)
- Weak → strong algorithm mapping table
- Full crypto asset inventory (16 operations)
- Banking feature → crypto operation map
- Live runnable demo scenarios (login, transfer, e-slip, device, partner, audit, profile)
- Live audit log with HMAC entries

This page is **not linked** from the customer-facing banking UI.

---

## Crypto Asset Inventory

All 16 cryptographic operations, each intentionally weak, each in its own dedicated method:

| # | Operation | Class | Method | Weak Value | Bob Change |
|---|---|---|---|---|---|
| 1 | Login Key Generation | AuthService | generateLoginKeyPair | RSA 1024 | RSA 3072 |
| 2 | Login Token Signing | AuthService | signLoginToken | SHA1withRSA | SHA256withRSA |
| 3 | Login Token Verification | AuthService | verifyLoginToken | SHA1withRSA | SHA256withRSA |
| 4 | Transfer Key Generation | TransferService | generateTransferKeyPair | RSA 1024 | RSA 3072 |
| 5 | Transfer Auth Signing | TransferService | signTransferAuthorization | SHA1withRSA | SHA256withRSA |
| 6 | Transfer Verification | TransferService | verifyTransferAuthorization | SHA1withRSA | SHA256withRSA |
| 7 | Transaction Integrity HMAC | TransferService | computeTransactionHmac | HmacSHA1 | HmacSHA256 |
| 8 | E-Slip Key Generation | SlipService | generateSlipKeyPair | RSA 1024 | RSA 3072 |
| 9 | E-Slip Signing | SlipService | signESlip | SHA1withRSA | SHA256withRSA |
| 10 | E-Slip Verification | SlipService | verifyESlip | SHA1withRSA | SHA256withRSA |
| 11 | Audit-Log HMAC | AuditService | computeAuditEntryHmac | HmacSHA1 | HmacSHA256 |
| 12 | Transaction Checksum | TransferService | computeTransactionChecksum | SHA-1 | SHA-256 |
| 13 | Statement Checksum | SlipService | computeStatementChecksum | SHA-1 | SHA-256 |
| 14 | Device Registration Key Gen | DeviceService | registerDevice | RSA 1024 | RSA 3072 |
| 15 | Partner Key Exchange | PartnerService | performPartnerKeyExchange | DH 1024 | DH 2048 |
| 16 | Profile Data Checksum | ProfileService | computeProfileChecksum | SHA-1 | SHA-256 |

---

## Planned QSE Scan Story

1. Build and run the app as-is (`mvn spring-boot:run`)
2. Point IBM QSE at the source tree: `src/main/java/demo/`
3. QSE scans all `.java` files in `demo/` flat package
4. Expected findings: **~20–25** cryptographic vulnerabilities

QSE will identify:
- RSA key sizes of 1024 bits (AuthService, TransferService, SlipService, DeviceService)
- DH key size of 1024 bits (PartnerService)
- `SHA1withRSA` signatures (6 occurrences across 3 services)
- `HmacSHA1` MACs (AuditService, TransferService)
- `SHA-1` digests (TransferService, SlipService, ProfileService)

---

## Future Bob Remediation Approach

Bob remediates **only** key sizes and algorithm/configuration strings.

### Before Bob → After Bob

```java
// RSA key size
generator.initialize(1024);        // → generator.initialize(3072);

// DH key size
dhGen.initialize(1024);            // → dhGen.initialize(2048);

// RSA signatures
Signature.getInstance("SHA1withRSA");   // → Signature.getInstance("SHA256withRSA");

// HMAC
Mac.getInstance("HmacSHA1");       // → Mac.getInstance("HmacSHA256");
// + SecretKeySpec("HmacSHA1")     // → SecretKeySpec("HmacSHA256")

// Message digest
MessageDigest.getInstance("SHA-1"); // → MessageDigest.getInstance("SHA-256");
```

### What Bob preserves (unchanged)

- Every class name and file
- Every method name and signature
- Every JCA/JCE API call pattern
- Every business purpose and flow
- The flat `demo/` package structure

---

## Workshop Metrics

| Metric | Before Bob | After Bob |
|---|---:|---:|
| QSE Findings (vulnerabilities) | ~20–25 | ~4–7 |
| Crypto Assets | 16 | 16 |
| Distinct algorithms | 5 | 5 |
| Classes changed by Bob | 0 | 7 |
| Lines changed by Bob | 0 | ~16 |

---

## Why the Java Structure is Shallow

All Java source files are directly under:

```
src/main/java/demo/
```

All classes use:

```java
package demo;
```

**Reason:** IBM QSE maps findings to source file paths. A flat structure means every finding resolves to a simple `demo/ClassName.java` path with no sub-package hierarchy to navigate. This makes the QSE → Bob workflow cleaner and faster in a live workshop setting.

---

## Project Structure

```
mobile-banking-qse-demo/
├── pom.xml
├── run.cmd
├── README.md
└── src/main/
    ├── java/demo/
    │   ├── DemoApplication.java       ← Spring Boot entry point
    │   ├── BankController.java        ← REST API for banking UI
    │   ├── DemoController.java        ← Operator panel API + /demo redirect
    │   ├── AuthService.java           ← Login key gen, token sign/verify
    │   ├── TransferService.java       ← Transfer sign/verify, HMAC, checksum
    │   ├── SlipService.java           ← E-slip sign/verify, statement checksum
    │   ├── AuditService.java          ← Audit-log HMAC
    │   ├── DeviceService.java         ← Device registration key gen
    │   ├── PartnerService.java        ← Partner DH key exchange
    │   └── ProfileService.java        ← Profile checksum, transaction data
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html             ← Banking SPA
            ├── app.js                 ← Frontend logic
            ├── styles.css             ← Banking design system
            └── demo.html             ← Operator panel (not linked from UI)
```

---

## Important Notes

- **No production use.** This is an educational workshop demo with intentionally weak cryptography.
- **In-memory only.** All data is lost on restart. No database required.
- **No real authentication.** The login flow is simulated for demo purposes.
- The subtle footer on the banking UI reads: *"Internal workshop demo · Educational use only"*
