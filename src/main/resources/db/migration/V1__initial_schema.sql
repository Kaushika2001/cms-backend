-- V1__initial_schema.sql
-- Create CardStatus lookup table
CREATE TABLE IF NOT EXISTS CardStatus (
    StatusCode VARCHAR(20) PRIMARY KEY,
    Description VARCHAR(100) NOT NULL,
    CONSTRAINT chk_card_status_code CHECK (StatusCode IN ('IACT', 'CACT', 'DACT'))
);

-- Create CardRequestType lookup table
CREATE TABLE IF NOT EXISTS CardRequestType (
    Code VARCHAR(20) PRIMARY KEY,
    Description VARCHAR(100) NOT NULL,
    CONSTRAINT chk_request_type CHECK (Code IN ('ACTI', 'CDCL'))
);

-- Create Card table
CREATE TABLE IF NOT EXISTS Card (
    CardNumber VARCHAR(16) PRIMARY KEY,
    ExpiryDate DATE NOT NULL,
    CardStatus VARCHAR(20) NOT NULL DEFAULT 'IACT',
    CreditLimit DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    CashLimit DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    AvailableCreditLimit DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    AvailableCashLimit DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    LastUpdateTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_status FOREIGN KEY (CardStatus) REFERENCES CardStatus(StatusCode),
    CONSTRAINT chk_card_number CHECK (LENGTH(CardNumber) >= 13 AND LENGTH(CardNumber) <= 16),
    CONSTRAINT chk_credit_limit CHECK (CreditLimit >= 0),
    CONSTRAINT chk_cash_limit CHECK (CashLimit >= 0),
    CONSTRAINT chk_available_credit CHECK (AvailableCreditLimit >= 0 AND AvailableCreditLimit <= CreditLimit),
    CONSTRAINT chk_available_cash CHECK (AvailableCashLimit >= 0 AND AvailableCashLimit <= CashLimit)
);

-- Create RequestStatus lookup table
CREATE TABLE IF NOT EXISTS RequestStatus (
    StatusCode VARCHAR(20) PRIMARY KEY,
    Description VARCHAR(100) NOT NULL,
    CONSTRAINT chk_request_status_code CHECK (StatusCode IN ('PEND', 'APPR', 'RJCT'))
);

-- Create CardRequest table
CREATE TABLE IF NOT EXISTS CardRequest (
    RequestId SERIAL PRIMARY KEY,
    CardNumber VARCHAR(16) NOT NULL,
    RequestReasonCode VARCHAR(20) NOT NULL,
    RequestStatusCode VARCHAR(20),
    Remark VARCHAR(500),
    CreatedTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_card FOREIGN KEY (CardNumber) REFERENCES Card(CardNumber) ON DELETE CASCADE,
    CONSTRAINT fk_request_type FOREIGN KEY (RequestReasonCode) REFERENCES CardRequestType(Code),
    CONSTRAINT fk_request_status FOREIGN KEY (RequestStatusCode) REFERENCES RequestStatus(StatusCode)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_card_status ON Card(CardStatus);
CREATE INDEX IF NOT EXISTS idx_card_update_time ON Card(LastUpdateTime);
CREATE INDEX IF NOT EXISTS idx_request_card ON CardRequest(CardNumber);
CREATE INDEX IF NOT EXISTS idx_request_created ON CardRequest(CreatedTime);
CREATE INDEX IF NOT EXISTS idx_request_status ON CardRequest(RequestStatusCode);

-- Insert initial lookup data for CardStatus
INSERT INTO CardStatus (StatusCode, Description) VALUES
('IACT', 'Card Inactive - Initial/Pending state'),
('CACT', 'Card Active - Normal active state'),
('DACT', 'Card Deactivated - Card has been deactivated')
ON CONFLICT (StatusCode) DO NOTHING;

-- Insert initial lookup data for CardRequestType
INSERT INTO CardRequestType (Code, Description) VALUES
('ACTI', 'Card Activation Request'),
('CDCL', 'Card Close Request')
ON CONFLICT (Code) DO NOTHING;

-- Insert initial lookup data for RequestStatus
INSERT INTO RequestStatus (StatusCode, Description) VALUES
('PEND', 'Pending - Awaiting approval'),
('APPR', 'Approved - Request has been approved'),
('RJCT', 'Rejected - Request has been rejected')
ON CONFLICT (StatusCode) DO NOTHING;

-- Add comments for documentation
COMMENT ON TABLE CardStatus IS 'Lookup table for card status codes';
COMMENT ON TABLE CardRequestType IS 'Lookup table for card request types';
COMMENT ON TABLE RequestStatus IS 'Lookup table for request status codes';
COMMENT ON TABLE Card IS 'Main card information table';
COMMENT ON TABLE CardRequest IS 'Card request/transaction history table';

COMMENT ON COLUMN Card.CardStatus IS 'Status: IACT (Inactive), CACT (Active), DACT (Deactivated)';
COMMENT ON COLUMN CardRequest.RequestReasonCode IS 'Type: ACTI (Activation), CDCL (Close)';
COMMENT ON COLUMN CardRequest.RequestStatusCode IS 'Status: PEND (Pending), APPR (Approved), RJCT (Rejected)';
