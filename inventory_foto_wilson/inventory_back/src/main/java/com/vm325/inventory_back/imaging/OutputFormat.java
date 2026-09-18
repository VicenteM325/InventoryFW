package com.vm325.inventory_back.imaging;

public enum OutputFormat {
    PDF("application/pdf", "pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

    private final String contentType;
    private final String fileExtension;

    OutputFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
