package com.exjobb.backend.service.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class StorageService {

    @Value("${gcs.bucket.name}")
    private String bucketName;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    /**
     * Laddar upp en fil (som byte-array) till Google Cloud Storage och returnerar dess offentliga URL.
     * @param fileData Filens innehåll som bytes.
     * @param fileName Det önskade filnamnet i bucketen.
     * @param contentType Filens MIME-typ (t.ex. "image/png").
     * @return Den offentliga URL:en till den uppladdade filen.
     */
    public String uploadFile(byte[] fileData, String fileName, String contentType) throws IOException {
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();


        storage.create(blobInfo, fileData);


        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }
}