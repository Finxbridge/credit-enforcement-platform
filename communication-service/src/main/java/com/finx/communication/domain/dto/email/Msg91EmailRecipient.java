package com.finx.communication.domain.dto.email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MSG91 Email Recipient Group DTO
 * Contains list of recipients and their specific variables
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg91EmailRecipient {

    @Valid
    @NotEmpty(message = "At least one recipient is required")
    private List<Msg91EmailTo> to;

    /**
     * Variables specific to this recipient group
     * Example: {"name": "Naveen", "OTP": "123456"}
     */
    private Map<String, Object> variables;
}
