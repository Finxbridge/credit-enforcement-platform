package com.finx.communication.domain.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * SMS Recipient with dynamic variables
 * Supports any number of dynamic variables (VAR1, VAR2, VAR3, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRecipient {

    private String mobile;

    /**
     * Dynamic variables for template
     * Example: {"VAR1": "Naveen", "VAR2": "10000"}
     */
    private Map<String, Object> variables;
}
