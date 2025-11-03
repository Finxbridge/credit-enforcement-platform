package com.finx.communication.domain.dto.otp;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendRequest {

    @NotBlank(message = "Mobile number is required")
    private String mobile;

    private String email;

    private String channel; // SMS, WHATSAPP, EMAIL (default: SMS)

    private String purpose; // LOGIN, RESET_PASSWORD, TRANSACTION

    private String templateId; // OTP template ID

    private Integer otpExpiry; // In seconds (default: 60)

    private Map<String, Object> params; // Dynamic parameters like Param1, Param2 for template
}
