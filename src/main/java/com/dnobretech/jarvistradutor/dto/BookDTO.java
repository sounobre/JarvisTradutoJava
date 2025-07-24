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
    private String filename;
    private String originalFilename;
    private String language;
    private String textContent;
    private String contentBlocks;
    private LocalDateTime uploadDate;
    private AuthorDTO authorDTO;
    private Integer totalChunks;

    // Adaptador simples: converte Book para BookDTO
    public static BookDTO fromEntity(Book book) {
        return BookDTO.builder()
                .id(book.getIdBook())
                .originalFilename(book.getOriginalFilename())
                .language(book.getLanguage())
                .uploadDate(book.getUploadDate())
                .textContent(book.getTextContent())
                .contentBlocks(book.getContentBlocks())
                .authorDTO(AuthorDTO.fromEntity(book.getAuthor()))
                .build();
    }
}
