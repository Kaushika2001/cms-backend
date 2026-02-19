package com.epic.cms.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for CardRequest with masked card number.
 * Used in GET requests to hide sensitive card number information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequestResponseDTO {

    private Integer requestId;
    private String cardNumber; // This will be masked (e.g., 123456******3456)
    private String requestReasonCode;
    private String requestReasonDescription;
    private String requestStatusCode;
    private String requestStatusDescription;
    private String remark;
    private LocalDateTime createdTime;
}
