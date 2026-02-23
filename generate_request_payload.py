#!/usr/bin/env python3
import json
import base64
from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes

# Transport key from application.yaml
transport_key_b64 = "bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c="
transport_key = base64.b64decode(transport_key_b64)

# Create the card request DTO
request_dto = {
    "cardNumber": "4532019482960366",
    "requestReasonCode": "ACTI",
    "remark": "Customer requested card activation"
}

# Convert to JSON
json_str = json.dumps(request_dto)
print(f"Original JSON: {json_str}\n")

# Encrypt with AES-256-GCM
iv = get_random_bytes(12)  # 96 bits
cipher = AES.new(transport_key, AES.MODE_GCM, nonce=iv)
ciphertext, tag = cipher.encrypt_and_digest(json_str.encode('utf-8'))

# Combine ciphertext and tag
ciphertext_with_tag = ciphertext + tag

# Encode as base64
iv_b64 = base64.b64encode(iv).decode('utf-8')
ciphertext_b64 = base64.b64encode(ciphertext_with_tag).decode('utf-8')

# Create encrypted data in format {iv}.{ciphertext}
encrypted_data = f"{iv_b64}.{ciphertext_b64}"

# Create the payload
encrypted_payload = {
    "encryptedData": encrypted_data
}

print(f"Encrypted Payload:")
print(json.dumps(encrypted_payload, indent=2))

print(f"\ncURL command:")
print(f"curl -X POST http://localhost:8080/api/v1/requests/encrypted \\")
print(f"  -H 'Content-Type: application/json' \\")
print(f"  -d '{json.dumps(encrypted_payload)}'")
