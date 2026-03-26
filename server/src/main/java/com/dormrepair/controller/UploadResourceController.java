package com.dormrepair.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UploadResourceController {

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getUpload(@PathVariable String filename) {
        List<Path> candidates = List.of(
                Paths.get("uploads", filename).toAbsolutePath().normalize(),
                Paths.get("server", "uploads", filename).toAbsolutePath().normalize(),
                Paths.get("../uploads", filename).toAbsolutePath().normalize(),
                Paths.get("../server", "uploads", filename).toAbsolutePath().normalize()
        );

        for (Path path : candidates) {
            if (Files.exists(path) && Files.isRegularFile(path)) {
                Resource resource = new FileSystemResource(path);
                MediaType mediaType = MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM);
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                        .body(resource);
            }
        }

        return ResponseEntity.notFound().build();
    }
}
