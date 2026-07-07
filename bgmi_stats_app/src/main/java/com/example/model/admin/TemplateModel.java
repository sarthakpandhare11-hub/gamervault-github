package com.example.model.admin;

public class TemplateModel {
    private String templateId;
    private String templateName;
    private String imageUrl;
    private String aiPrompt;
    private long createdAt;

    private boolean isHardcoded = false;
    private String localAssetPath;

    public TemplateModel() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAiPrompt() {
        return aiPrompt;
    }

    public void setAiPrompt(String aiPrompt) {
        this.aiPrompt = aiPrompt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isHardcoded() {
        return isHardcoded;
    }

    public void setHardcoded(boolean isHardcoded) {
        this.isHardcoded = isHardcoded;
    }

    public String getLocalAssetPath() {
        return localAssetPath;
    }

    public void setLocalAssetPath(String localAssetPath) {
        this.localAssetPath = localAssetPath;
    }

}