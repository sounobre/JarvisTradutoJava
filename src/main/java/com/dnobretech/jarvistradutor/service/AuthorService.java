package com.dnobretech.jarvistradutor.service;

import com.dnobretech.jarvistradutor.model.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Author createAuthor(Author author);
    List<Author> listAll();
    Optional<Author> findById(Long id);
    // Adicione métodos como update e delete, se necessário
    Author updateAuthor(Long id, Author updatedAuthor);
}