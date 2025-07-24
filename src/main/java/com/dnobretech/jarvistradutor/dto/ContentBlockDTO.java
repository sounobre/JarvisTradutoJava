package com.dnobretech.jarvistradutor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentBlockDTO {
    private String type; // "text" ou "image"
    private String content; // texto, se for um bloco de texto
    private String imageUrl; // URL relativa do arquivo, se for imagem
    private String caption; // legenda da imagem, se conseguir extrair

}
