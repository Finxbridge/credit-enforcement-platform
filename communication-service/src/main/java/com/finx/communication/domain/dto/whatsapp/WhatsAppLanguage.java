package com.finx.communication.domain.dto.whatsapp;

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
