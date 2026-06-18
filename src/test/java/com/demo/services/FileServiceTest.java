package com.demo.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de {@link FileService}.
 * Las rutas felices escriben de verdad en la carpeta "uploads" y se limpian en {@link #cleanUp()}.
 */
class FileServiceTest {

    private final FileService fileService = new FileService();
    private final List<Path> createdFiles = new ArrayList<>();

    @AfterEach
    void cleanUp() throws IOException {
        for (Path p : createdFiles) {
            Files.deleteIfExists(p);
        }
    }

    private Path resolveStored(String url) {
        String filename = url.substring("/uploads/".length());
        return Paths.get(FileService.UPLOAD_DIR).toAbsolutePath().normalize().resolve(filename);
    }

    @Test
    @DisplayName("store(null) devuelve null")
    void storeNullReturnsNull() {
        assertNull(fileService.store(null));
    }

    @Test
    @DisplayName("store de un archivo vacío devuelve null")
    void storeEmptyReturnsNull() {
        MultipartFile empty = new MockMultipartFile("imageFile", "foto.png", "image/png", new byte[0]);
        assertNull(fileService.store(empty));
    }

    @Test
    @DisplayName("store de un archivo válido lo guarda y devuelve /uploads/<uuid>.ext")
    void storeValidFilePersistsAndReturnsUrl() throws IOException {
        byte[] content = "contenido-imagen".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("imageFile", "foto.png", "image/png", content);

        String url = fileService.store(file);

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/"), "la url debe empezar por /uploads/");
        assertTrue(url.endsWith(".png"), "debe conservar la extensión original");

        Path stored = resolveStored(url);
        createdFiles.add(stored);
        assertTrue(Files.exists(stored), "el archivo debe existir en disco");
        assertArrayEquals(content, Files.readAllBytes(stored));
    }

    @Test
    @DisplayName("store de un archivo sin extensión genera un nombre sin punto")
    void storeFileWithoutExtension() {
        MultipartFile file = new MockMultipartFile(
                "imageFile", "sinextension", "image/png", "x".getBytes(StandardCharsets.UTF_8));

        String url = fileService.store(file);

        assertNotNull(url);
        createdFiles.add(resolveStored(url));
        String filename = url.substring("/uploads/".length());
        assertFalse(filename.contains("."), "no debe añadir extensión si el original no la tenía");
    }

    @Test
    @DisplayName("dos archivos con el mismo nombre generan rutas distintas (UUID)")
    void storeGeneratesUniqueNames() {
        MultipartFile a = new MockMultipartFile("imageFile", "foto.png", "image/png", "a".getBytes(StandardCharsets.UTF_8));
        MultipartFile b = new MockMultipartFile("imageFile", "foto.png", "image/png", "b".getBytes(StandardCharsets.UTF_8));

        String urlA = fileService.store(a);
        String urlB = fileService.store(b);
        createdFiles.add(resolveStored(urlA));
        createdFiles.add(resolveStored(urlB));

        assertNotEquals(urlA, urlB);
    }

    @Test
    @DisplayName("si transferTo lanza IOException, store la envuelve en RuntimeException")
    void storeWrapsIOExceptionInRuntime() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("foto.png");
        doThrow(new IOException("disco lleno")).when(file).transferTo(any(Path.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> fileService.store(file));
        assertEquals("No se pudo guardar el archivo", ex.getMessage());
        assertInstanceOf(IOException.class, ex.getCause());
    }
}
