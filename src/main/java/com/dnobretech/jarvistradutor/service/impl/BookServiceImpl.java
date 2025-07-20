package com.dnobretech.jarvistradutor.service.impl;

import com.dnobretech.jarvistradutor.model.Book;
import com.dnobretech.jarvistradutor.repository.BookRepository;
import com.dnobretech.jarvistradutor.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    @Value("${upload.path}")
    private String uploadPath;

    private final BookRepository bookRepository;

    @Override
    public Book uploadBook(MultipartFile file, String language) {
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(file.getOriginalFilename().lastIndexOf(".")))
                .orElse("");
        String storedFileName = UUID.randomUUID() + ext;

        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        File destination = new File(dir, storedFileName);
        try {
            file.transferTo(destination);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo", e);
        }

        Book book = Book.builder()
                .filename(storedFileName)
                .originalFilename(file.getOriginalFilename())
                .filePath(destination.getAbsolutePath())
                .language(language)
                .uploadDate(LocalDateTime.now())
                .build();

        return bookRepository.save(book);
    }
}
