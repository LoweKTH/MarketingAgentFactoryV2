package com.exjobb.backend.service.media;

import com.exjobb.backend.service.storage.StorageService;
import com.google.cloud.aiplatform.v1.*;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service("vertexImageGenerator")
public class VertexImageService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(VertexImageService.class);

    private final String vertexProjectId;
    private final String vertexLocation;
    private final String imagenModelId;
    private final StorageService storageService;

    public VertexImageService(
            @Value("${spring.ai.vertex.ai.gemini.project-id}") String vertexProjectId,
            @Value("${spring.ai.vertex.ai.gemini.location}") String vertexLocation,
            @Value("${google.api.imagen.model_id}") String imagenModelId,
            StorageService storageService
    ) {
        this.vertexProjectId = vertexProjectId;
        this.vertexLocation = vertexLocation;
        this.imagenModelId = imagenModelId;
        this.storageService = storageService;
    }

    @Override
    public String generateImage(String imagePrompt) {
        logger.info("VERTEX IMAGE SERVICE: Generating image with prompt '{}'", imagePrompt);

        String location = this.vertexLocation;
        String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);

        try (PredictionServiceClient client = PredictionServiceClient.create(
                PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build())) {

            final String instanceJson = String.format("{\"prompt\": \"%s\"}", escapeJson(imagePrompt));
            com.google.protobuf.Value.Builder instanceBuilder = com.google.protobuf.Value.newBuilder();
            JsonFormat.parser().merge(instanceJson, instanceBuilder);

            final String parametersJson = "{\"sampleCount\": 1, \"aspectRatio\": \"1:1\"}";
            com.google.protobuf.Value.Builder parametersBuilder = com.google.protobuf.Value.newBuilder();
            JsonFormat.parser().merge(parametersJson, parametersBuilder);

            EndpointName endpointName = EndpointName.ofProjectLocationPublisherModelName(
                    this.vertexProjectId, location, "google", this.imagenModelId);

            PredictRequest request = PredictRequest.newBuilder()
                    .setEndpoint(endpointName.toString())
                    .addInstances(instanceBuilder.build())
                    .setParameters(parametersBuilder.build())
                    .build();


            PredictResponse response = client.predict(request);
            String base64ImageData = response.getPredictions(0).getStructValue().getFieldsMap().get("bytesBase64Encoded").getStringValue();
            byte[] imageBytes = Base64.getDecoder().decode(base64ImageData);




            String fileName = "image_" + System.currentTimeMillis() + ".png";


            String publicUrl = storageService.uploadFile(imageBytes, fileName, "image/png");

            logger.info("Successfully generated and uploaded image to GCS: {}", publicUrl);


            return "IMAGE_RESULT:::" + publicUrl;

        } catch (Exception e) {
            logger.error("Failed to generate image via Vertex AI: {}", e.getMessage(), e);
            throw new RuntimeException("Image generation failed. Reason: " + e.getMessage());
        }
    }


    private String escapeJson(String input) {
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}