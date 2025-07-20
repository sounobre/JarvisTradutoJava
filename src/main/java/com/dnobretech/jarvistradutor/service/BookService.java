package com.dnobretech.jarvistradutor.service;

import com.dnobretech.jarvistradutor.model.Book;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    Book uploadBook(MultipartFile file, String language);
}
