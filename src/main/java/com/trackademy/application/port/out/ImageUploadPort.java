package com.trackademy.application.port.out;

public interface ImageUploadPort {
    String uploadBase64Image(String base64Image, String fileName) throws Exception;
}
