package com.finx.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "third_party_integration_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyIntegrationMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "integration_name", nullable = false, unique = true, length = 100)
    private String integrationName;

    @Column(name = "integration_type", length = 50)
    private String integrationType;

    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    @Column(name = "api_key_encrypted", length = 500)
    private String apiKeyEncrypted;

    @Column(name = "api_secret_encrypted", length = 500)
    private String apiSecretEncrypted;

    @Column(name = "config_json", columnDefinition = "jsonb")
    private String configJson;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get decrypted API key
     */
    public String getApiKey() {
        // Decryption will be handled in service layer
        return this.apiKeyEncrypted;
    }

    /**
     * Get decrypted API secret
     */
    public String getApiSecret() {
        // Decryption will be handled in service layer
        return this.apiSecretEncrypted;
    }

    /**
     * Get config value as String
     */
    public String getConfigValueAsString(String key) {
        if (configJson != null && configJson.contains("\"" + key + "\":")) {
            String search = "\"" + key + "\":\"";
            int startIndex = configJson.indexOf(search);
            if (startIndex != -1) {
                startIndex += search.length();
                int endIndex = configJson.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return configJson.substring(startIndex, endIndex);
                }
            }
        }
        return null;
    }
}