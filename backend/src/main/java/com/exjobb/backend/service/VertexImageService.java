package com.exjobb.backend.service;

import com.google.cloud.aiplatform.v1.*;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;

// Byt namn på er befintliga klass till detta, och använd denna logik
@Service("vertexImageGenerator")
public class VertexImageService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(VertexImageService.class);

    private final String imageStoragePath;
    private final String baseUrl;
    private final String vertexProjectId;
    private final String vertexLocation;

    public VertexImageService(
            @Value("${spring.ai.vertex.ai.gemini.project-id}") String vertexProjectId,
            @Value("${spring.ai.vertex.ai.gemini.location}") String vertexLocation,
            @Value("${app.image.storage.path:/tmp/generated-images/}") String imageStoragePath,
            @Value("${app.base.url:http://localhost:8080}") String baseUrl
    ) {
        this.vertexProjectId = vertexProjectId;
        this.vertexLocation = vertexLocation;
        this.imageStoragePath = imageStoragePath; // Detta fält har nu ett värde
        this.baseUrl = baseUrl;

        try {
            Files.createDirectories(Paths.get(this.imageStoragePath));
        } catch (IOException e) {
            logger.error("Could not create image storage directory: {}", this.imageStoragePath, e);
            throw new RuntimeException("Could not create image storage directory", e);
        }
    }

    @Override
    public String generateImage(String imagePrompt) {
        logger.info("--- VERTEX IMAGE SERVICE: Generating image with prompt '{}' ---", imagePrompt);

        String location = this.vertexLocation;
        String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);

        try (PredictionServiceClient client = PredictionServiceClient.create(
                PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build())) {

            final String instanceJson = String.format("{\"prompt\": \"%s\"}", escapeJson(imagePrompt));
            com.google.protobuf.Value.Builder instanceBuilder = com.google.protobuf.Value.newBuilder();
            JsonFormat.parser().merge(instanceJson, instanceBuilder);

            // Använd de förbättrade parametrarna från din första lösning
            final String parametersJson = "{\"sampleCount\": 1, \"aspectRatio\": \"1:1\"}";
            com.google.protobuf.Value.Builder parametersBuilder = com.google.protobuf.Value.newBuilder();
            JsonFormat.parser().merge(parametersJson, parametersBuilder);

            EndpointName endpointName = EndpointName.ofProjectLocationPublisherModelName(
                    this.vertexProjectId, location, "google", "imagegeneration@005");

            PredictRequest request = PredictRequest.newBuilder()
                    .setEndpoint(endpointName.toString())
                    .addInstances(instanceBuilder.build())
                    .setParameters(parametersBuilder.build())
                    .build();

            PredictResponse response = client.predict(request);
            String base64ImageData = response.getPredictions(0).getStructValue().getFieldsMap().get("bytesBase64Encoded").getStringValue();

            // Spara bilden till en fil
            String fileName = "image_" + System.currentTimeMillis() + ".png";
            Path filePath = Paths.get(imageStoragePath, fileName);
            byte[] imageBytes = Base64.getDecoder().decode(base64ImageData);
            Files.write(filePath, imageBytes);

            // Bygg och returnera den publika URL:en
            String imageUrl = baseUrl + "/api/images/" + fileName;

            logger.info("Successfully generated and saved image: {}", imageUrl);
            return "IMAGE_RESULT:::" + imageUrl;

        } catch (Exception e) {
            logger.error("Failed to generate image via Vertex AI: {}", e.getMessage(), e);
            throw new RuntimeException("Image generation failed. Reason: " + e.getMessage());
        }
    }

    // Er utmärkta hjälp-metod för att hantera " i prompten
    private String escapeJson(String input) {
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}