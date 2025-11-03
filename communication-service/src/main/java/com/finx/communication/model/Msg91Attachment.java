package com.finx.communication.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Msg91Attachment {
    private String filePath;
    private String fileName;
}
