package com.epic.cms.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequest {
    
    private Integer requestId;
    private String cardNumber;
    private String requestReasonCode;
    private String requestStatusCode;
    private String remark;
    private LocalDateTime createdTime;
}
