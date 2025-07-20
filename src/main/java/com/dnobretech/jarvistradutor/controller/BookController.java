package com.dnobretech.jarvistradutor.controller;

import com.dnobretech.jarvistradutor.dto.BookDTO;
import com.dnobretech.jarvistradutor.model.Book;
import com.dnobretech.jarvistradutor.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Livros", description = "Endpoints para gerenciamento de livros")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Faz upload de um novo livro")
    @PostMapping("/upload")
    public ResponseEntity<BookDTO> uploadBook(@RequestParam("file") MultipartFile file,
                                              @RequestParam("language") String language) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Book book = bookService.uploadBook(file, language);
        BookDTO dto = BookDTO.fromEntity(book);
        return ResponseEntity.ok(dto);
    }
}