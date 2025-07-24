package com.dnobretech.jarvistradutor.service.impl;

import com.dnobretech.jarvistradutor.model.Author;
import com.dnobretech.jarvistradutor.repository.AuthorRepository;
import com.dnobretech.jarvistradutor.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    @Override
    public List<Author> listAll() {
        return authorRepository.findAll();
    }

    @Override
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    @Override
    public Author updateAuthor(Long id, Author updatedAuthor) {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Autor não encontrado com o ID: " + id));

        existingAuthor.setName(updatedAuthor.getName());
        existingAuthor.setBiography(updatedAuthor.getBiography());
        existingAuthor.setEmail(updatedAuthor.getEmail());

        return authorRepository.save(existingAuthor);
    }

}