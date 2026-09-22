package org.tafel.squating.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private String baseUrl;
    private String evidenceDirectory;
    private String mailRecipient;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEvidenceDirectory() {
        return evidenceDirectory;
    }

    public void setEvidenceDirectory(String evidenceDirectory) {
        this.evidenceDirectory = evidenceDirectory;
    }

    public String getMailRecipient() {
        return mailRecipient;
    }

    public void setMailRecipient(String mailRecipient) {
        this.mailRecipient = mailRecipient;
    }
}