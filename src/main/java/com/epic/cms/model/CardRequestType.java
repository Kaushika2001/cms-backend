package com.epic.cms.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequestType {
    
    private String code;
    private String description;
}
