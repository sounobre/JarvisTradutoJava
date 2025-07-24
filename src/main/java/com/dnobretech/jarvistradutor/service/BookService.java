package com.dnobretech.jarvistradutor.service;

import com.dnobretech.jarvistradutor.dto.BookDTO;
import com.dnobretech.jarvistradutor.dto.ContentBlockDTO;
import com.dnobretech.jarvistradutor.model.Book;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface BookService {
    Book uploadBook(MultipartFile file, String language, Long authorId) throws IOException;
    List<BookDTO> listAll();
    Optional<BookDTO> findById(Long id);
    boolean deleteById(Long id);
    List<ContentBlockDTO> getContentBlocks(Long bookId);

    Book updateBook(Long id, BookDTO bookDTO);
    void sliceAndProcessBook(Book book, File file, int chunkSize) throws IOException;
}
