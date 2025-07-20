package com.dnobretech.jarvistradutor.dto;

import com.dnobretech.jarvistradutor.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDTO {
    private Long id;
    private String originalFilename;
    private String language;
    private LocalDateTime uploadDate;

    // Adaptador simples: converte Book para BookDTO
    public static BookDTO fromEntity(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .originalFilename(book.getOriginalFilename())
                .language(book.getLanguage())
                .uploadDate(book.getUploadDate())
                .build();
    }
}
