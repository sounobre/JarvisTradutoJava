package com.dnobretech.jarvistradutor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@ToString
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuthor;

    private String name;
    private String biography; // Opcional
    private String email; // Também é opcional

    @ToString.Exclude
    @OneToMany(mappedBy = "author")
    private List<Book> books; // Para referência

}
