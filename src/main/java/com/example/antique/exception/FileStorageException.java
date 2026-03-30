package com.example.antique.exception;

/**
 * Exception tùy chỉnh cho các lỗi liên quan đến lưu trữ file.
 */
public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super(message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
