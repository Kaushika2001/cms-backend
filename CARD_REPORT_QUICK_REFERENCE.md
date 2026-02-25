# Card Report - Quick Reference Guide

## API Endpoints

### PDF Report
```
GET /api/v1/reports/cards/pdf
Authorization: Basic {base64(username:password)}
Response: application/pdf
```

### CSV Report
```
GET /api/v1/reports/cards/csv
Authorization: Basic {base64(username:password)}
Response: text/csv
```

---

## Report Fields

| # | Field Name | Type | Example | Source |
|---|------------|------|---------|--------|
| 1 | Masked Card Number | String | 589925******0233 | Card.CardNumber (decrypted & masked) |
| 2 | Expiry Date | Date | 2025-12-31 | Card.ExpiryDate |
| 3 | Credit Limit | Decimal | 5000.00 | Card.CreditLimit |
| 4 | Card Status | String | Card Active - Normal active state | CardStatus.Description |
| 5 | Cash Limit | Decimal | 1000.00 | Card.CashLimit |
| 6 | Available Credit Limit | Decimal | 3500.00 | Card.AvailableCreditLimit |
| 7 | Available Cash Limit | Decimal | 750.00 | Card.AvailableCashLimit |
| 8 | Last Update Time (Audit) | DateTime | 2026-02-25 10:15:30 | Card.LastUpdateTime |
| 9 | Last Update User (Audit) | String | admin | Card.LastUpdatedUser |

---

## Quick Test

### Using cURL (PDF)
```bash
curl -u admin:password \
  http://localhost:8080/api/v1/reports/cards/pdf \
  --output report.pdf
```

### Using cURL (CSV)
```bash
curl -u admin:password \
  http://localhost:8080/api/v1/reports/cards/csv \
  --output report.csv
```

### Using Browser
```
http://localhost:8080/api/v1/reports/cards/pdf
(Enter credentials when prompted)
```

---

## Files Modified/Created

### New Files (4)
1. `src/main/java/com/epic/cms/dto/CardReportDTO.java`
2. `src/main/java/com/epic/cms/service/CardReportService.java`
3. `src/main/java/com/epic/cms/service/impl/CardReportServiceImpl.java`
4. `src/main/java/com/epic/cms/controller/CardReportController.java`

### Modified Files (3)
1. `pom.xml` - Added iText7 and OpenCSV dependencies
2. `src/main/java/com/epic/cms/repository/ICardRepository.java` - Added findAllForReport()
3. `src/main/java/com/epic/cms/repository/CardRepository.java` - Implemented findAllForReport()

---

## Key Features

✅ **Masked Card Numbers** - Security first approach  
✅ **Audit Trail** - Last update time and user  
✅ **Status Descriptions** - Human-readable status from lookup table  
✅ **Professional PDF** - Formatted with headers and alternating rows  
✅ **Excel-Compatible CSV** - UTF-8 encoded, ready to import  
✅ **Timestamped Filenames** - card_report_YYYYMMDD_HHMMSS.pdf  
✅ **Authentication Required** - Secured with Spring Security  
✅ **Automatic Decryption** - Card numbers decrypted on-the-fly  

---

## Common Issues & Solutions

| Problem | Solution |
|---------|----------|
| 401 Unauthorized | Check username/password |
| Empty report | Add cards to database |
| Blank card numbers | Verify encryption key in application.yaml |
| CSV not opening in Excel | Use "Import Data" feature, select UTF-8 |
| PDF not rendering | Clear browser cache |

---

## Swagger Documentation

Access interactive API documentation:
```
http://localhost:8080/swagger-ui.html
Navigate to: "Card Reports" section
```

---

## Build & Run

```bash
# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run

# Test endpoints
curl -u admin:password http://localhost:8080/api/v1/reports/cards/pdf --output test.pdf
```

---

## Database Query (Behind the Scenes)

```sql
SELECT 
    c.CardNumber, 
    c.ExpiryDate, 
    c.CreditLimit, 
    c.CashLimit,
    c.AvailableCreditLimit, 
    c.AvailableCashLimit, 
    c.LastUpdateTime,
    c.LastUpdatedUser, 
    cs.Description as CardStatusDescription
FROM Card c
LEFT JOIN CardStatus cs ON c.CardStatus = cs.StatusCode
ORDER BY c.LastUpdateTime DESC
```

---

## Frontend Integration (Quick Example)

```javascript
// Download PDF Report
async function downloadPdfReport() {
    const response = await fetch('http://localhost:8080/api/v1/reports/cards/pdf', {
        headers: {
            'Authorization': 'Basic ' + btoa('username:password')
        }
    });
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'card_report.pdf';
    a.click();
}
```

---

## Audit Information

### What is Audited?
- **Last Update Time**: When the card was last modified
- **Last Update User**: Who made the modification

### Where is it Stored?
- Database: `Card.LastUpdateTime` and `Card.LastUpdatedUser`
- Application Logs: `[AUDIT]` entries in system.log

### Note on Users Table
There is NO separate Users table. The `lastUpdatedUser` field stores the username string directly. If null, displays as "System" in reports.

---

## Version

**Current Version**: 1.0  
**Release Date**: 2026-02-25  
**Compatible With**: Spring Boot 3.5.10, Java 17  

---

**For detailed documentation, see**: `CARD_REPORT_IMPLEMENTATION.md`  
**For frontend integration guide, see**: `FRONTEND_REPORT_INTEGRATION.md`
