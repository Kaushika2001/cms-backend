-- V2__add_missing_lookup_tables.sql
-- Add missing lookup tables and initial data

-- Create CardRequestType lookup table if it doesn't exist
CREATE TABLE IF NOT EXISTS CardRequestType (
    Code VARCHAR(20) PRIMARY KEY,
    Description VARCHAR(100) NOT NULL,
    CONSTRAINT chk_request_type CHECK (Code IN ('ACTI', 'CDCL'))
);

-- Insert initial data for CardRequestType
INSERT INTO CardRequestType (Code, Description) VALUES
    ('ACTI', 'Card Activation Request'),
    ('CDCL', 'Card Closure Request')
ON CONFLICT (Code) DO NOTHING;

-- Create RequestStatus lookup table if it doesn't exist
CREATE TABLE IF NOT EXISTS RequestStatus (
    StatusCode VARCHAR(20) PRIMARY KEY,
    Description VARCHAR(100) NOT NULL,
    CONSTRAINT chk_request_status_code CHECK (StatusCode IN ('PEND', 'APPR', 'RJCT'))
);

-- Insert initial data for RequestStatus
INSERT INTO RequestStatus (StatusCode, Description) VALUES
    ('PEND', 'Request Pending'),
    ('APPR', 'Request Approved'),
    ('RJCT', 'Request Rejected')
ON CONFLICT (StatusCode) DO NOTHING;

-- Create CardRequest table if it doesn't exist
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
CREATE INDEX IF NOT EXISTS idx_card_request_card_number ON CardRequest(CardNumber);
CREATE INDEX IF NOT EXISTS idx_card_request_status ON CardRequest(RequestStatusCode);
CREATE INDEX IF NOT EXISTS idx_card_request_created_time ON CardRequest(CreatedTime);
