package com.finx.templatemanagementservice.domain.dto.comm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for WhatsApp Media Upload (MSG91)
 * Used for uploading media (IMAGE, VIDEO, DOCUMENT) to get header_handle for template creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMediaUploadResponse {

    private String status;

    private boolean hasError;

    private MediaData data;

    private String errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaData {
        /**
         * The header_handle URL to use in template creation
         * This is the media reference returned by MSG91
         */
        private String url;
    }

    /**
     * Get the header_handle value for template creation
     */
    public String getHeaderHandle() {
        return data != null ? data.getUrl() : null;
    }
}
