package com.finx.communication.domain.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MSG91 Email Attachment DTO
 * Supports both filePath (public URL) and file (Data URI format)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg91EmailAttachment {

    /**
     * Public URL path for file
     */
    private String filePath;

    /**
     * File in Data URI format: data:content/type;base64,<data>
     */
    private String file;

    /**
     * File name
     */
    private String fileName;
}
