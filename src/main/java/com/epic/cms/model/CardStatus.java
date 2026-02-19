package com.epic.cms.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardStatus {
    
    private String statusCode;
    private String description;
}
