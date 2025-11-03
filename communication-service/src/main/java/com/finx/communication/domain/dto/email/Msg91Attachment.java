package com.finx.communication.domain.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Msg91Attachment {
    private String filename;
    private String content; // Base64 encoded content
    private String contentType;
}
