package com.demo.services;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @AfterAll
    static void cleanUpUploads() throws IOException {
        Path path = Paths.get(FileService.UPLOAD_DIR).toAbsolutePath().normalize();
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }

    @Test
    void storeNullFileReturnsNull() {
        String result = fileService.store(null);
        assertNull(result);
    }

    @Test
    void storeEmptyFileReturnsNull() {
        MultipartFile emptyFile = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);
        String result = fileService.store(emptyFile);
        assertNull(result);
    }

    @Test
    void storeSuccessWithExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagen.proyecto.png",
                "image/png",
                "contenido de prueba".getBytes()
        );

        String result = fileService.store(file);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));
        assertTrue(result.endsWith(".png"));

        String generatedFilename = result.substring("/uploads/".length());
        Path filePath = Paths.get(FileService.UPLOAD_DIR, generatedFilename).toAbsolutePath().normalize();
        assertTrue(Files.exists(filePath));
    }

    @Test
    void storeSuccessWithoutExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "archivo-sin-extension",
                "application/octet-stream",
                "datos".getBytes()
        );

        String result = fileService.store(file);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/"));

        String generatedFilename = result.substring("/uploads/".length());
        assertFalse(generatedFilename.contains("."));
        Path filePath = Paths.get(FileService.UPLOAD_DIR, generatedFilename).toAbsolutePath().normalize();
        assertTrue(Files.exists(filePath));
    }

    @Test
    void storeThrowsRuntimeExceptionOnIOException() throws IOException {
        MultipartFile mockedFile = mock(MultipartFile.class);
        when(mockedFile.isEmpty()).thenReturn(false);
        when(mockedFile.getOriginalFilename()).thenReturn("error.jpg");
        doThrow(new IOException("Error simulado de disco")).when(mockedFile).transferTo(any(Path.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileService.store(mockedFile));

        assertEquals("No se pudo guardar el archivo", exception.getMessage());
        verify(mockedFile).isEmpty();
        verify(mockedFile).getOriginalFilename();
    }
}
