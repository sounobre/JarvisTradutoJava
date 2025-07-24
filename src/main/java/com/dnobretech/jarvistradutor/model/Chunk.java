package com.dnobretech.jarvistradutor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idChunk;
    private String filePath;
    private Boolean processed;
    @Column(columnDefinition = "TEXT") // Para garantir que textos grandes sejam armazenados
    private String txChunk;


    @ManyToOne
    @JoinColumn(name = "idBook", nullable = false)
    private Book book;
}
