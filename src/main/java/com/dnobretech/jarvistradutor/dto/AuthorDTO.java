package com.dnobretech.jarvistradutor.dto;

import com.dnobretech.jarvistradutor.model.Author;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorDTO {
    private Long id;
    private String name;
    private String biography;
    private String email;

    // Método para converter Author para AuthorDTO
    public static AuthorDTO fromEntity(Author author) {
        return AuthorDTO.builder()
                .id(author.getIdAuthor())
                .name(author.getName())
                .biography(author.getBiography())
                .email(author.getEmail())
                .build();
    }

    // Método para converter AuthorDTO para Author
    public static Author toEntity(AuthorDTO authorDTO) {
        Author author = new Author();
        author.setIdAuthor(authorDTO.getId());
        author.setName(authorDTO.getName());
        author.setBiography(authorDTO.getBiography());
        author.setEmail(authorDTO.getEmail());
        return author;
    }
}