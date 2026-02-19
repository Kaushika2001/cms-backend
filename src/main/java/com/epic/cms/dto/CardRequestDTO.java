package com.epic.cms.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequestDTO {

    private Integer requestId;
    private String cardNumber;
    private String requestReasonCode;
    private String requestReasonDescription;
    private String requestStatusCode;
    private String requestStatusDescription;
    private String remark;
    private LocalDateTime createdTime;
}
