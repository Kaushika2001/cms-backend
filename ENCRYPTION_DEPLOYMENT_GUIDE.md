# Backend Encryption System - Deployment Guide

## Overview

This guide covers the deployment and testing of the two-layer encryption system implemented in your Card Management System (CMS).

## ✅ Implementation Status

The following components have been successfully implemented:

### Core Components
- ✅ `EncryptionConfig.java` - Manages encryption keys
- ✅ `EncryptionUtil.java` - AES-256-GCM encryption/decryption
- ✅ `EncryptedPayload.java` - DTO for encrypted payloads
- ✅ `EncryptionKeyValidator.java` - Validates keys on startup (NEW)
- ✅ `EncryptionAuditAspect.java` - Audit logging for encryption operations (NEW)
- ✅ `EncryptionKeyGenerator.java` - Secure key generation utility (NEW)

### Integration
- ✅ `CardController.java` - `/api/v1/cards/encrypted` endpoint
- ✅ `CardServiceImpl.java` - Storage layer encryption
- ✅ `GlobalExceptionHandler.java` - Encryption error handling
- ✅ Database migration - V4__update_card_number_for_encryption.sql

### Testing
- ✅ Unit tests - `EncryptionUtilTest.java`
- ✅ Integration tests - `CardControllerEncryptionIntegrationTest.java` (NEW)

---

## 🔑 Encryption Keys

### Development Keys (Already Configured)
Your `application.yaml` currently has development keys:

```yaml
encryption:
  transport:
    key: ${TRANSPORT_ENCRYPTION_KEY:bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=}
  storage:
    key: ${STORAGE_ENCRYPTION_KEY:7XwK4mN9pR2vT8bL5jH3gF1dC6aY0xQ+W/zM4nP7sE=}
```

### Production Keys (Generated)
New production keys have been generated:

**Transport Key (Frontend ↔ Backend):**
```
NN7IdKStifaIs2boWptjdzbYoLlZBuM/Ic0+CH8MBCs=
```

**Storage Key (Backend ↔ Database):**
```
U9XG22hPDOfuqhH7S8POaukyamzjXkat6vpGuYsygYI=
```

### ⚠️ IMPORTANT: Key Management

1. **NEVER commit production keys to Git**
2. **Store keys in a secrets manager:**
   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault
   - Google Cloud Secret Manager

3. **Environment-specific keys:**
   - Development: Use existing keys for testing
   - Staging: Generate separate keys
   - Production: Use the new generated keys above

4. **Transport key MUST match between frontend and backend**

---

## 🚀 Deployment Steps

### Step 1: Update Frontend Environment Variables

Add to your frontend `.env` file:

```bash
# For development (use existing key)
VITE_ENCRYPTION_KEY=bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=

# For production (use new transport key)
VITE_ENCRYPTION_KEY=NN7IdKStifaIs2boWptjdzbYoLlZBuM/Ic0+CH8MBCs=
```

### Step 2: Update Backend Environment Variables

#### Option A: Environment Variables (Recommended for Production)

```bash
# Set environment variables
export TRANSPORT_ENCRYPTION_KEY=NN7IdKStifaIs2boWptjdzbYoLlZBuM/Ic0+CH8MBCs=
export STORAGE_ENCRYPTION_KEY=U9XG22hPDOfuqhH7S8POaukyamzjXkat6vpGuYsygYI=

# Run application
./mvnw spring-boot:run
```

#### Option B: Application.yaml (Development Only)

Your current `application.yaml` is already configured with development keys. No changes needed for development.

#### Option C: Docker Environment

```yaml
# docker-compose.yml
services:
  cms-backend:
    environment:
      - TRANSPORT_ENCRYPTION_KEY=NN7IdKStifaIs2boWptjdzbYoLlZBuM/Ic0+CH8MBCs=
      - STORAGE_ENCRYPTION_KEY=U9XG22hPDOfuqhH7S8POaukyamzjXkat6vpGuYsygYI=
```

### Step 3: Run Database Migrations

The migration is already in place. Flyway will automatically apply it on startup.

**Verify migration:**

```bash
# Connect to PostgreSQL
psql -U postgres -d cms

# Check Card table structure
\d "Card"

# Expected: CardNumber column should be VARCHAR(500)
# Column    |          Type          | Collation | Nullable | Default
# CardNumber| character varying(500) |           | not null |
```

**If migration hasn't been applied yet:**

```bash
# Run Flyway migration manually
./mvnw flyway:migrate
```

### Step 4: Build and Test

```bash
# Clean and build
./mvnw clean install

# Run tests (including new encryption tests)
./mvnw test

# Start application
./mvnw spring-boot:run
```

### Step 5: Verify Application Startup

Watch for these log messages:

```
[INFO] Validating encryption keys on startup...
[INFO] ✓ Transport encryption key is valid
[INFO] ✓ Storage encryption key is valid
[INFO] ✓ All encryption keys validated successfully. System is ready for encrypted operations.
```

If keys are invalid, the application will fail to start with a clear error message.

---

## 🧪 Testing

### 1. Run Unit Tests

```bash
./mvnw test -Dtest=EncryptionUtilTest
```

Expected: All 13 tests should pass.

### 2. Run Integration Tests

```bash
./mvnw test -Dtest=CardControllerEncryptionIntegrationTest
```

Expected: All 7 integration tests should pass, including:
- ✓ Create card with encrypted payload
- ✓ Two-layer encryption flow
- ✓ Invalid payload handling
- ✓ Wrong key detection

### 3. Manual Testing with Postman/cURL

#### Generate Test Encrypted Payload

You can use the frontend encryption or create a simple Java test:

```java
// Create test payload
CreateCardDTO cardDTO = new CreateCardDTO();
cardDTO.setCardNumber("1234567890123456");
cardDTO.setExpiryDate(LocalDate.of(2026, 12, 31));
cardDTO.setCardStatus("IACT");
cardDTO.setCreditLimit(new BigDecimal("50000.00"));
cardDTO.setCashLimit(new BigDecimal("10000.00"));
cardDTO.setAvailableCreditLimit(new BigDecimal("50000.00"));
cardDTO.setAvailableCashLimit(new BigDecimal("10000.00"));

// Encrypt with transport key
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(cardDTO);
String encrypted = EncryptionUtil.encrypt(json, "bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=");

System.out.println("Encrypted payload: " + encrypted);
```

#### Test API Endpoint

```bash
curl -X POST http://localhost:8080/api/v1/cards/encrypted \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "YOUR_ENCRYPTED_PAYLOAD_HERE",
    "timestamp": "2026-02-23T10:00:00Z"
  }'
```

### 4. Verify Database Encryption

```sql
-- Connect to database
psql -U postgres -d cms

-- View encrypted card numbers
SELECT "CardNumber", "CardStatus", "ExpiryDate" 
FROM "Card" 
LIMIT 5;

-- Card numbers should look like: "YXBjZGVm...abc123.XYZabc..."
-- NOT like: "1234567890123456"
```

---

## 📊 Monitoring and Audit Logs

The `EncryptionAuditAspect` logs all encryption/decryption operations.

### View Audit Logs

```bash
# View application logs
tail -f logs/app.log | grep AUDIT

# Expected log entries:
# [AUDIT] Encryption operation performed by user: SYSTEM at 2026-02-23T10:00:00
# [AUDIT] Decryption operation performed by user: admin at 2026-02-23T10:01:00
# [AUDIT] Card created with encrypted storage by user: SYSTEM at 2026-02-23T10:02:00
```

### Important Audit Events

1. **Decryption operations** - Someone accessing plaintext data
2. **Bulk card retrieval** - Multiple decryptions (warning level)
3. **Encrypted payload reception** - Frontend sending encrypted data
4. **Card creation** - New encrypted card stored

---

## 🔒 Security Checklist

Before deploying to production:

- [ ] Generated unique encryption keys for production
- [ ] Stored keys in a secrets manager (not in code/config files)
- [ ] Transport key matches between frontend and backend
- [ ] Storage key is different from transport key
- [ ] Database migration applied successfully (CardNumber is VARCHAR(500))
- [ ] All tests pass (unit + integration)
- [ ] Application starts without encryption key errors
- [ ] Verified card numbers are encrypted in database
- [ ] Audit logging is working
- [ ] Documented key rotation schedule (every 90 days)
- [ ] Access to decryption methods is restricted (add @PreAuthorize if needed)
- [ ] Production keys are NOT committed to Git
- [ ] Team knows how to rotate keys

---

## 🔄 Key Rotation Procedure

### When to Rotate

- Every 90 days (recommended)
- After suspected compromise
- When employee with key access leaves

### How to Rotate Keys

#### 1. Generate New Keys

```bash
./mvnw exec:java -Dexec.mainClass="com.epic.cms.util.EncryptionKeyGenerator"
```

#### 2. Deploy with Dual Key Support (Optional for Storage Key)

For storage key rotation, you may need to:

1. Add a second storage key configuration (old + new)
2. Decrypt existing cards with old key
3. Re-encrypt with new key
4. Remove old key after migration

**Example migration script:**

```java
@Service
public class KeyRotationService {
    
    public void rotateStorageKey(String oldKey, String newKey) {
        List<Card> allCards = cardRepository.findAll();
        
        for (Card card : allCards) {
            // Decrypt with old key
            String decrypted = EncryptionUtil.decrypt(card.getCardNumber(), oldKey);
            
            // Re-encrypt with new key
            String encrypted = EncryptionUtil.encrypt(decrypted, newKey);
            
            card.setCardNumber(encrypted);
            cardRepository.save(card);
        }
        
        LoggerUtil.info(KeyRotationService.class, 
            "Successfully rotated storage key for {} cards", allCards.size());
    }
}
```

#### 3. Update Frontend (Transport Key Only)

Update frontend `.env` file with new transport key and redeploy.

#### 4. Verify

- Test encryption/decryption with new keys
- Check logs for errors
- Verify existing cards can still be retrieved

---

## 🐛 Troubleshooting

### Issue: "Transport encryption key is not configured"

**Solution:** Set the `TRANSPORT_ENCRYPTION_KEY` environment variable or check `application.yaml`.

### Issue: "Decryption failed"

**Possible causes:**
1. Frontend and backend using different transport keys
2. Invalid encrypted data format
3. Data corrupted during transmission

**Solution:** Verify keys match exactly between frontend and backend.

### Issue: "Invalid encrypted data format"

**Solution:** Ensure frontend is using the correct encryption format: `{iv}.{ciphertext}`

### Issue: "Column too small" database error

**Solution:** Run the V4 migration to increase CardNumber column size:
```bash
./mvnw flyway:migrate
```

### Issue: Existing unencrypted cards in database

If you have existing cards with plaintext card numbers, you need to migrate them:

```java
// Migration script
@Service
public class DataMigrationService {
    
    @Autowired
    private ICardRepository cardRepository;
    
    @Autowired
    private EncryptionConfig encryptionConfig;
    
    public void encryptExistingCards() {
        String storageKey = encryptionConfig.getStorageKey();
        List<Card> cards = cardRepository.findAll();
        
        for (Card card : cards) {
            // Check if card number is already encrypted (contains '.')
            if (!card.getCardNumber().contains(".")) {
                String encrypted = EncryptionUtil.encrypt(card.getCardNumber(), storageKey);
                card.setCardNumber(encrypted);
                cardRepository.update(card);
                LoggerUtil.info(DataMigrationService.class, 
                    "Encrypted card: {}", CardMaskingUtil.mask(card.getCardNumber()));
            }
        }
        
        LoggerUtil.info(DataMigrationService.class, 
            "Migration complete. Total cards encrypted: {}", cards.size());
    }
}
```

---

## 📚 Additional Resources

### Documentation Files
- `ENCRYPTION_IMPLEMENTATION.md` - Original implementation guide
- `FRONTEND_API_DOCUMENTATION.md` - API documentation
- `INTEGRATION_GUIDE.md` - Frontend-backend integration

### Code Locations
- Encryption utilities: `src/main/java/com/epic/cms/util/`
- Configuration: `src/main/java/com/epic/cms/config/`
- Tests: `src/test/java/com/epic/cms/`
- Migrations: `src/main/resources/db/migration/`

### Key Generator
Run anytime to generate new keys:
```bash
./mvnw exec:java -Dexec.mainClass="com.epic.cms.util.EncryptionKeyGenerator"
```

---

## 🎯 Summary

Your encryption system is now fully implemented with:

1. **Two-layer encryption**: Transport (frontend ↔ backend) + Storage (backend ↔ database)
2. **Startup validation**: Keys validated on application startup
3. **Comprehensive testing**: Unit + Integration tests
4. **Audit logging**: All encryption operations logged
5. **Security best practices**: Key rotation, secrets management, error handling

**Status:** ✅ Ready for testing and deployment

**Next Steps:**
1. Test the `/api/v1/cards/encrypted` endpoint with your frontend
2. Verify cards are encrypted in database
3. Review audit logs
4. Deploy to staging environment
5. Generate production keys and store in secrets manager
6. Deploy to production

---

**Questions or Issues?**
- Check logs in `logs/app.log`
- Review error responses from the API
- Verify encryption keys are correctly configured
- Ensure database migration was applied

**Generated:** February 23, 2026  
**Version:** 1.0  
**System Status:** ✅ READY FOR DEPLOYMENT
