package com.dnobretech.jarvistradutor.controller;

import com.dnobretech.jarvistradutor.dto.AuthorDTO;
import com.dnobretech.jarvistradutor.model.Author;
import com.dnobretech.jarvistradutor.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Autores", description = "Endpoints para gerenciamento de autores")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @Operation(summary = "Criar um novo autor")
    @PostMapping
    public ResponseEntity<AuthorDTO> createAuthor(@RequestBody AuthorDTO authorDTO) {
        Author author = AuthorDTO.toEntity(authorDTO);
        Author savedAuthor = authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthorDTO.fromEntity(savedAuthor));
    }

    @Operation(summary = "Listar todos os autores")
    @GetMapping
    public ResponseEntity<List<AuthorDTO>> listAllAuthors() {
        List<AuthorDTO> authors = authorService.listAll().stream()
                .map(AuthorDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(authors);
    }

    @Operation(summary = "Buscar um autor pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthor(@PathVariable Long id) {
        return authorService.findById(id)
                .map(author -> ResponseEntity.ok(AuthorDTO.fromEntity(author)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar dados de um autor")
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(@PathVariable Long id, @RequestBody AuthorDTO updatedAuthorDTO) {
        Author updatedAuthor = authorService.updateAuthor(id, AuthorDTO.toEntity(updatedAuthorDTO));
        return ResponseEntity.ok(AuthorDTO.fromEntity(updatedAuthor));
    }

}