package com.dnobretech.jarvistradutor.repository;

import com.dnobretech.jarvistradutor.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}