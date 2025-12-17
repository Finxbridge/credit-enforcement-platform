package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailReceiptRequest {

    @NotBlank(message = "Primary email is required")
    @Email(message = "Invalid email format")
    private String primaryEmail;

    private List<@Email(message = "Invalid CC email format") String> ccEmails;

    private String subject;

    private String message;

    private Boolean attachPdf;
}
