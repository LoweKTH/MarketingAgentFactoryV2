package com.exjobb.backend.service;


public interface ImageGenerationService {

    /**
     * Generates an image based on a text prompt and returns a data URL (e.g., "data:image/png;base64,...")
     * or a publicly accessible URL to the image.
     * @param imagePrompt A detailed description of the image to generate.
     * @return A string containing the image data URL or a link to the image.
     */
    String generateImage(String imagePrompt);
}
