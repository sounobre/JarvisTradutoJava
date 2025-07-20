package com.dnobretech.jarvistradutor.repository;


import com.dnobretech.jarvistradutor.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {}
