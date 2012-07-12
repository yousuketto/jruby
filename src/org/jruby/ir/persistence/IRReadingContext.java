package org.jruby.ir.persistence;

public enum IRReadingContext {
    INSTANCE;
    
    private String fileName;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }

}
