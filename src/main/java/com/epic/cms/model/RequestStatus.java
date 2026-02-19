package com.epic.cms.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestStatus {
    
    private String statusCode;
    private String description;
}
