package com.finx.templatemanagementservice.domain.dto.comm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Unified WhatsApp Template Creation Request via communication-service
 * Supports all template types:
 * - Text Header with Quick Reply/URL buttons
 * - Media Header (IMAGE, VIDEO, DOCUMENT) with Phone Number buttons
 * - Location Header with URL buttons
 *
 * Note: integrated_number is read from database config_json in communication-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppCreateTemplateRequest {

    @JsonProperty("template_name")
    private String templateName;

    private String language; // e.g., "en", "en_US", "hi"

    private String category; // MARKETING, UTILITY, AUTHENTICATION

    @JsonProperty("button_url")
    private Boolean buttonUrl; // Set to true if template has URL button

    private List<TemplateComponent> components;

    /**
     * Template Component - supports HEADER, BODY, FOOTER, BUTTONS
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateComponent {

        private String type; // HEADER, BODY, FOOTER, BUTTONS

        // For HEADER component
        private String format; // TEXT, IMAGE, VIDEO, DOCUMENT, LOCATION

        // For HEADER (TEXT) and BODY, FOOTER components
        private String text;

        // Example values for variables
        private ComponentExample example;

        // For BUTTONS component
        private List<TemplateButton> buttons;
    }

    /**
     * Component Example - for variable samples
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentExample {

        // For TEXT header: ["Sample header text"]
        @JsonProperty("header_text")
        private List<String> headerText;

        // For MEDIA header: ["header_handle_value"]
        @JsonProperty("header_handle")
        private List<String> headerHandle;

        // For BODY: [["var1_value", "var2_value", "var3_value"]]
        @JsonProperty("body_text")
        private List<List<String>> bodyText;
    }

    /**
     * Template Button - supports QUICK_REPLY, URL, PHONE_NUMBER
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateButton {

        private String type; // QUICK_REPLY, URL, PHONE_NUMBER

        private String text;

        // For URL button
        private String url;

        // For URL button with variable - example URLs
        private List<String> example;

        // For PHONE_NUMBER button
        @JsonProperty("phone_number")
        private String phoneNumber;
    }
}
