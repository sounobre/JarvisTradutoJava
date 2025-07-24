package com.dnobretech.jarvistradutor.service.impl;

import com.dnobretech.jarvistradutor.dto.BookDTO;
import com.dnobretech.jarvistradutor.dto.ContentBlockDTO;
import com.dnobretech.jarvistradutor.model.Author;
import com.dnobretech.jarvistradutor.model.Book;
import com.dnobretech.jarvistradutor.model.Chunk;
import com.dnobretech.jarvistradutor.repository.BookRepository;
import com.dnobretech.jarvistradutor.service.AuthorService;
import com.dnobretech.jarvistradutor.service.BookExtractionService;
import com.dnobretech.jarvistradutor.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    @Value("${upload.path}")
    private String uploadPath;

    private final BookRepository bookRepository;
    private final BookExtractionService bookExtractionService;
    private final AuthorService authorService;

    @Override
    public Book uploadBook(MultipartFile file, String language, Long authorId) throws IOException {
        // 1. Salvar o arquivo no diretório configurado
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        File savedFile = new File(uploadPath + "/" + UUID.randomUUID() + "_" + originalFilename);
        file.transferTo(savedFile);

        // 2. Salvar informações do livro no banco
        Author author = authorService.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Book book = Book.builder()
                .filename(savedFile.getName())
                .originalFilename(originalFilename)
                .language(language)
                .filePath(savedFile.getAbsolutePath())
                .uploadDate(LocalDateTime.now())
                .author(author)
                .textContent("") // Ainda será processado
                .contentBlocks("") // Ainda será gerado
                .build();
        bookRepository.save(book);

        // 3. Dividir o arquivo em chunks
        List<File> chunks;
        if ("pdf".equalsIgnoreCase(extension)) {
            chunks = bookExtractionService.slicePdf(savedFile, 10); // 10 pode ser o tamanho do chunk
        } else if ("docx".equalsIgnoreCase(extension)) {
            chunks = bookExtractionService.sliceDocx(savedFile, 10);
        } else if ("txt".equalsIgnoreCase(extension)) {
            chunks = bookExtractionService.sliceTxt(savedFile, 10);
        } else {
            throw new RuntimeException("Formato de arquivo não suportado: " + extension);
        }

        // Registrar cada chunk no banco
        List<Chunk> chunkEntities = new ArrayList<>();
        for (File chunkFile : chunks) {
            Chunk chunk = Chunk.builder()
                    .filePath(chunkFile.getAbsolutePath())
                    .processed(false)
                    .book(book) // Relacionamento com o livro
                    .build();
            chunkEntities.add(chunk);
        }
        book.setChunks(chunkEntities);
        book.setTotalChunks(chunks.size());
        bookRepository.save(book); // Atualiza com os chunks

        // 4. Processar os chunks
        StringBuilder fullText = new StringBuilder();
        for (Chunk chunk : book.getChunks()) {
            // Processar o chunk
            String chunkText = bookExtractionService.extractText(new File(chunk.getFilePath()), originalFilename);
            chunk.setTxChunk(chunkText);
            chunk.setProcessed(true);
            fullText.append(chunkText).append("\n");
        }

        // 5. Salvar o texto completo no livro e marcar como concluído
        book.setTextContent(fullText.toString());
        bookRepository.save(book);

        return book;
    }





    @Override
    public List<BookDTO> listAll() {
        return bookRepository.findAll().stream()
                .map(BookDTO::fromEntity)
                .toList();
    }

    @Override
    public Optional<BookDTO> findById(Long id) {
        return bookRepository.findById(id)
                .map(BookDTO::fromEntity);
    }

    @Override
    public boolean deleteById(Long id) {
        return bookRepository.findById(id).map(book -> {
            // remove arquivo físico
            File file = new File(book.getFilePath());
            if (file.exists()) file.delete();
            bookRepository.delete(book);
            return true;
        }).orElse(false);
    }

    @Override
    public List<ContentBlockDTO> getContentBlocks(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new RuntimeException("Livro não encontrado: " + bookId));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String contentBlocks = book.getContentBlocks();
            if (contentBlocks == null || contentBlocks.isEmpty()) {
                return List.of(); // vazio se não extraiu ainda
            }
            return Arrays.asList(mapper.readValue(contentBlocks, ContentBlockDTO[].class));
        } catch (Exception e) {
            log.warn("Erro convertendo os blocos do livro " + bookId, e);
            return List.of();
        }
    }

    @Override
    public Book updateBook(Long id, BookDTO bookDTO) {
        Book existingBook = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Livro não encontrado"));
        existingBook.setOriginalFilename(bookDTO.getOriginalFilename());
        existingBook.setLanguage(bookDTO.getLanguage());
        existingBook.setUploadDate(bookDTO.getUploadDate());
        existingBook.setTextContent(bookDTO.getTextContent());
        existingBook.setContentBlocks(bookDTO.getContentBlocks());
        return bookRepository.save(existingBook);
    }

    @Override
    public void sliceAndProcessBook(Book book, File file, int chunkSize) throws IOException {
        List<File> chunks;

        // Identificar o tipo do arquivo
        String ext = getFileExtension(file.getName());
        if ("pdf".equalsIgnoreCase(ext)) {
            chunks = bookExtractionService.slicePdf(file, chunkSize);
        } else if ("docx".equalsIgnoreCase(ext)) {
            chunks = bookExtractionService.sliceDocx(file, chunkSize);
        } else if ("txt".equalsIgnoreCase(ext)) {
            chunks = bookExtractionService.sliceTxt(file, chunkSize);
        } else {
            throw new IllegalArgumentException("Formato de arquivo não suportado para corte: " + ext);
        }

        // Extrair os blocos de texto de cada chunk
        List<ContentBlockDTO> allBlocks = new ArrayList<>();
        for (File chunk : chunks) {
            try {
                List<ContentBlockDTO> blocks = bookExtractionService.extractBlocks(chunk, file.getName(), book.getIdBook());
                allBlocks.addAll(blocks);
            } catch (IOException e) {
                log.error("Erro na extração do chunk: {}", chunk.getName(), e);
            }
        }

        // Salvar os blocos no banco em formato JSON
        saveContentBlocks(book, allBlocks);
    }


    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ""; // Sem extensão
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void saveContentBlocks(Book book, List<ContentBlockDTO> blocks) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String blocksJson = objectMapper.writeValueAsString(blocks);
        book.setContentBlocks(blocksJson); // Salva no campo contentBlocks (JSON)
        book.setTotalChunks(blocks.size()); // Define o total de partes
        bookRepository.save(book); // Atualiza o livro no banco
    }



}
