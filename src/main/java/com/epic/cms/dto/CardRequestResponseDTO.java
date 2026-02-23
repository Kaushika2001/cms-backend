package com.epic.cms.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for CardRequest with masked card number and masked card ID.
 * Used in GET requests to hide sensitive card number information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequestResponseDTO {

    private Integer requestId;
    private String maskedCardId; // Unique masked identifier (e.g., CRD-M-256AB400)
    private String cardNumber; // This will be masked for display (e.g., 123456******3456)
    private String requestReasonCode;
    private String requestReasonDescription;
    private String requestStatusCode;
    private String requestStatusDescription;
    private String remark;
    private LocalDateTime createdTime;
}
