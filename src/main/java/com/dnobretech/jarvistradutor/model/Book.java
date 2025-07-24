package com.dnobretech.jarvistradutor.model;

import com.sun.jdi.connect.Connector;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data // Já cria getters, setters, equals, hashCode, toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBook;

    private String filename;
    private String originalFilename;
    private String language;
    private String filePath;
    private LocalDateTime uploadDate;
    @Column(columnDefinition = "TEXT")
    private String textContent;
    @Lob
    private String contentBlocks;
    private Integer totalChunks;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Author author; // Relacionamento com Author

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Chunk> chunks;
}