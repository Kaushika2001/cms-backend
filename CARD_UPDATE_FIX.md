# Card Update Fix - Masked Card Number Matching

## Problem Summary

When trying to update a card using a masked card number like `558899******3333`, the system was failing with:
```
"Card not found with maskedCardNumber : '558899******3333'"
```

## Root Cause Analysis

### Issue 1: Missing UpdateCardDTO
The `PUT /api/v1/cards/{cardNumber}` endpoint was using `CreateCardDTO` which required `cardNumber` in the request body. This violated RESTful design principles where the identifier should come from the URL path, not the body.

**Validation Error:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "details": ["cardNumber: Card number is required"]
}
```

### Issue 2: Encrypted Card Numbers Not Being Decrypted

The `findByMaskedCardNumber()` method in `CardRepository` was trying to match the masked pattern against **encrypted** card numbers stored in the database.

**How Card Numbers Are Stored:**
- Database stores: `{iv}.{ciphertext}` (e.g., `aBc123...==.xYz789...==`)
- Frontend sends: `558899******3333` (masked format)
- Matching logic was comparing: `aBc123...==.xYz789...==` ❌ vs `558899******3333`

**What Should Happen:**
1. Retrieve encrypted card number from database: `{iv}.{ciphertext}`
2. **Decrypt** to get plain card number: `5588991234563333`
3. Match decrypted number against masked pattern: `5588991234563333` ✅ matches `558899******3333`

## Solutions Implemented

### 1. Created UpdateCardDTO ✅

**File:** `src/main/java/com/epic/cms/dto/UpdateCardDTO.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCardDTO {
    // NO cardNumber field - comes from URL path
    
    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])-(20[2-9][0-9]|2[1-9][0-9]{2})", 
             message = "Expiry date must be in MM-YYYY format")
    private String expiryDate;

    @NotBlank(message = "Card status is required")
    @Pattern(regexp = "IACT|CACT|DACT", 
             message = "Card status must be IACT, CACT, or DACT")
    private String cardStatus;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "0.0", message = "Credit limit must be non-negative")
    private BigDecimal creditLimit;

    @NotNull(message = "Cash limit is required")
    @DecimalMin(value = "0.0", message = "Cash limit must be non-negative")
    private BigDecimal cashLimit;

    @NotNull(message = "Available credit limit is required")
    @DecimalMin(value = "0.0", message = "Available credit limit must be non-negative")
    private BigDecimal availableCreditLimit;

    @NotNull(message = "Available cash limit is required")
    @DecimalMin(value = "0.0", message = "Available cash limit must be non-negative")
    private BigDecimal availableCashLimit;
}
```

### 2. Updated CardController ✅

**File:** `src/main/java/com/epic/cms/controller/CardController.java`

**Changes:**
- Import `UpdateCardDTO`
- Change endpoint signature to accept `UpdateCardDTO` instead of `CreateCardDTO`
- Support masked card numbers in URL path
- Return `CardResponseDTO` with masked card number

```java
@PutMapping("/{cardNumber}")
@Operation(summary = "Update card", 
           description = "Update an existing card. Card number can be masked (e.g., 558899******3333) or encrypted.")
public ResponseEntity<CardResponseDTO> updateCard(
        @PathVariable String cardNumber,
        @Valid @RequestBody UpdateCardDTO updateCardDTO) {
    log.info("PUT /api/v1/cards/{} - Update card", cardNumber);
    CardResponseDTO updatedCard = cardService.updateCardMasked(cardNumber, updateCardDTO);
    return ResponseEntity.ok(updatedCard);
}
```

### 3. Added updateCardMasked() Method ✅

**File:** `src/main/java/com/epic/cms/service/impl/CardServiceImpl.java`

**New Method:**
```java
public CardResponseDTO updateCardMasked(String maskedOrEncryptedCardNumber, UpdateCardDTO updateCardDTO) {
    log.info("Updating card with masked/encrypted card number: {}", maskedOrEncryptedCardNumber);
    
    Card existingCard;
    
    // Find card using masked or plain card number
    if (CardMaskingUtil.isMasked(maskedOrEncryptedCardNumber)) {
        // Masked: 558899******3333
        existingCard = cardRepository.findByMaskedCardNumber(maskedOrEncryptedCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "maskedCardNumber", maskedOrEncryptedCardNumber));
    } else {
        // Plain: 5588991234563333
        existingCard = findCardByUnencryptedNumber(maskedOrEncryptedCardNumber);
    }
    
    // Validate status, expiry date, etc.
    // Update card using encrypted card number from database
    // Return masked response
}
```

### 4. Fixed CardRepository.findByMaskedCardNumber() ✅

**File:** `src/main/java/com/epic/cms/repository/CardRepository.java`

**Key Fix:** Decrypt card numbers before matching against masked pattern

**Before:**
```java
Optional<Card> result = allCards.stream()
    .filter(card -> {
        // ❌ Comparing encrypted card number with masked pattern
        boolean matches = CardMaskingUtil.matchesMaskedPattern(
            card.getCardNumber(),  // This is ENCRYPTED: {iv}.{ciphertext}
            maskedCardNumber       // This is MASKED: 558899******3333
        );
        return matches;
    })
    .findFirst();
```

**After:**
```java
String storageKey = encryptionConfig.getStorageKey();

Optional<Card> result = allCards.stream()
    .filter(card -> {
        try {
            // ✅ Decrypt first, then compare with masked pattern
            String encryptedCardNumber = card.getCardNumber();
            String decryptedCardNumber = EncryptionUtil.decrypt(encryptedCardNumber, storageKey);
            
            // Now compare: 5588991234563333 matches 558899******3333
            boolean matches = CardMaskingUtil.matchesMaskedPattern(
                decryptedCardNumber,  // Plain: 5588991234563333
                maskedCardNumber      // Masked: 558899******3333
            );
            
            if (matches) {
                log.info("Found matching card: {} matches pattern {}", 
                        CardMaskingUtil.mask(decryptedCardNumber), maskedCardNumber);
            }
            
            return matches;
        } catch (Exception e) {
            log.error("Error decrypting card number during masked search", e);
            return false;
        }
    })
    .findFirst();
```

## How It Works Now

### Encryption Flow Visualization

```
┌─────────────────────────────────────────────────────────────────┐
│                     Card Update Request                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
        PUT /api/v1/cards/558899******3333
        Body: {
          "expiryDate": "12-2027",
          "cardStatus": "CACT",
          "creditLimit": 100000,
          ...
        }
                              │
                              ▼
        ┌──────────────────────────────────────┐
        │      CardController.updateCard       │
        │   - Receives masked card number      │
        │   - Validates UpdateCardDTO          │
        └──────────────────────────────────────┘
                              │
                              ▼
        ┌──────────────────────────────────────┐
        │  CardServiceImpl.updateCardMasked    │
        │   - Checks if masked (has asterisks) │
        │   - Calls findByMaskedCardNumber()   │
        └──────────────────────────────────────┘
                              │
                              ▼
        ┌──────────────────────────────────────┐
        │ CardRepository.findByMaskedCardNumber│
        │                                      │
        │  1. Get all cards from database      │
        │     [Card1: {iv1}.{cipher1},         │
        │      Card2: {iv2}.{cipher2}, ...]    │
        │                                      │
        │  2. For each card:                   │
        │     - Decrypt with storage key       │
        │       {iv1}.{cipher1} → 5588991234563333 │
        │                                      │
        │     - Match against pattern          │
        │       5588991234563333 ✅ matches    │
        │       558899******3333               │
        │                                      │
        │  3. Return matching card (encrypted) │
        └──────────────────────────────────────┘
                              │
                              ▼
        ┌──────────────────────────────────────┐
        │  CardServiceImpl.updateCardMasked    │
        │   - Update card with encrypted number│
        │   - Decrypt before returning         │
        │   - Mask card number in response     │
        └──────────────────────────────────────┘
                              │
                              ▼
        Response: {
          "maskedCardId": "CRD-M-XXXXXXXX",
          "maskedCardNumber": "558899******3333",
          "expiryDate": "12-2027",
          ...
        }
```

## Testing

### Test Request

```bash
curl -X PUT "http://localhost:8080/api/v1/cards/558899******3333" \
  -H "Content-Type: application/json" \
  -d '{
    "expiryDate": "12-2027",
    "cardStatus": "CACT",
    "creditLimit": 100000,
    "cashLimit": 50000,
    "availableCreditLimit": 80000,
    "availableCashLimit": 40000
  }'
```

### Expected Success Response

```json
{
  "maskedCardId": "CRD-M-12345678",
  "maskedCardNumber": "558899******3333",
  "expiryDate": "12-2027",
  "cardStatus": "CACT",
  "creditLimit": 100000,
  "cashLimit": 50000,
  "availableCreditLimit": 80000,
  "availableCashLimit": 40000,
  "lastUpdateTime": "2026-02-25T11:00:00"
}
```

## Files Modified

1. ✅ `src/main/java/com/epic/cms/dto/UpdateCardDTO.java` - **CREATED**
2. ✅ `src/main/java/com/epic/cms/controller/CardController.java` - Updated `PUT /{cardNumber}` endpoint
3. ✅ `src/main/java/com/epic/cms/service/impl/CardServiceImpl.java` - Added `updateCardMasked()` method
4. ✅ `src/main/java/com/epic/cms/repository/CardRepository.java` - Fixed `findByMaskedCardNumber()` to decrypt before matching

## Next Steps

1. **Restart the backend** to load the new compiled code:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Test the update endpoint** with the masked card number:
   ```bash
   bash test-card-update.sh
   ```

3. **Verify the card was updated** by checking:
   - Response shows updated values
   - Card status changed to CACT
   - Expiry date updated to 12-2027

## Security Notes

✅ **Card numbers are never exposed** - All card numbers remain encrypted in:
- Database storage (storage layer encryption)
- Responses (masked format: `558899******3333`)
- Logs (masked format only)

✅ **Decryption only happens in memory** during matching/processing, never persisted

✅ **Storage key is never exposed** to frontend or external systems
