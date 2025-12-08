package com.finx.templatemanagementservice.domain.dto.comm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WhatsApp Language configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppLanguage {

    private String code; // e.g., "en_US", "en"

    @Builder.Default
    private String policy = "deterministic";
}
