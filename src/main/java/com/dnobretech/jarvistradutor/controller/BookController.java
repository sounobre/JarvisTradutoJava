package com.dnobretech.jarvistradutor.controller;

import com.dnobretech.jarvistradutor.dto.BookDTO;
import com.dnobretech.jarvistradutor.dto.ContentBlockDTO;
import com.dnobretech.jarvistradutor.model.Book;
import com.dnobretech.jarvistradutor.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Livros", description = "Endpoints para gerenciamento de livros")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Faz upload de um novo livro")
    @PostMapping("/upload")
    public ResponseEntity<BookDTO> uploadBook(@RequestParam("file") MultipartFile file,
                                              @RequestParam("language") String language,
                                              @RequestParam("authorId") Long authorId) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Book book = bookService.uploadBook(file, language, authorId);
        BookDTO dto = BookDTO.fromEntity(book);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Listar todos os livros cadastrados")
    @GetMapping
    public ResponseEntity<List<BookDTO>> listAllBooks() {
        List<BookDTO> books = bookService.listAll();
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Buscar um livro pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remover um livro pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (bookService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/blocks")
    public ResponseEntity<List<ContentBlockDTO>> getBlocks(@PathVariable Long id) {
        List<ContentBlockDTO> blocks = bookService.getContentBlocks(id);
        return ResponseEntity.ok(blocks);
    }

    @Operation(summary = "Atualizar um livro existente")
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        Book updatedBook = bookService.updateBook(id, bookDTO);
        return ResponseEntity.ok(BookDTO.fromEntity(updatedBook));
    }


}