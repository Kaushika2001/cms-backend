package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCardStatusDTO {

    @NotBlank(message = "Card number is required")
    private String cardNumber;  // Can be full card number, masked (453201******0366), or encrypted

    @NotBlank(message = "Card status is required")
    @Pattern(regexp = "IACT|CACT|DACT", message = "Card status must be IACT, CACT, or DACT")
    private String newStatus;
}
