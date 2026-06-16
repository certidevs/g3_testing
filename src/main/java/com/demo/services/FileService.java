package com.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    public static final String UPLOAD_DIR = "src/main/resources/static/images";

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 🔹 Validar que sea una imagen
        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Solo se permiten imágenes");
        }
        // 🔹 Validar tamaño máximo (5 MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("La imagen es demasiado grande (máx. 5 MB)");
        }

        try {
            Path dir = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Files.createDirectories(dir); // crea la carpeta si no existe
            String original = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot > 0)
                ext = original.substring(dot);
            String filename = UUID.randomUUID() + ext;
            file.transferTo(dir.resolve(filename));
            return "/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }
    }
}