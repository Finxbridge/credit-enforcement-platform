package com.finx.communication.domain.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MSG91 Email Reply-To DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg91EmailReplyTo {

    @NotBlank(message = "Reply-to email is required")
    @Email(message = "Invalid email format")
    private String email;
}
