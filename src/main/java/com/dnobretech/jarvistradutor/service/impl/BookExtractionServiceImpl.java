package com.dnobretech.jarvistradutor.service.impl;

import com.dnobretech.jarvistradutor.dto.ContentBlockDTO;
import com.dnobretech.jarvistradutor.model.Book;
import com.dnobretech.jarvistradutor.service.BookExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BookExtractionServiceImpl implements BookExtractionService {

    @Override
    public String extractText(File file, String originalFilename) throws IOException {
        String fileNameLower = originalFilename.toLowerCase();
        if (fileNameLower.endsWith(".pdf")) {
            return extractPdf(file);
        } else if (fileNameLower.endsWith(".docx")) {
            return extractDocx(file);
        } else if (fileNameLower.endsWith(".txt")) {
            return extractTxt(file);
        } else if (fileNameLower.endsWith(".epub")) {
            try {
                return extractEpub(file);  // Chama o método abaixo!
            } catch (TikaException e) {
                throw new IOException("Erro ao extrair texto de arquivo EPUB", e);
            }
        } else {
            throw new IOException("Tipo de arquivo não suportado para extração de texto.");
        }
    }

    private String extractPdf(File file) throws IOException {
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            return extractor.getText();
        }
    }

    private String extractTxt(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    private String extractEpub(File file) throws IOException, TikaException {
        Tika tika = new Tika();
        return tika.parseToString(file);
    }

    @Override
    public List<ContentBlockDTO> extractBlocks(File file, String originalFilename, Long bookId) throws IOException {
        String fileNameLower = originalFilename.toLowerCase();
        if (fileNameLower.endsWith(".pdf")) {
            return extractPdfBlocks(file, bookId);
        }
        // Pode expandir depois para epub, docx, etc.
        throw new IOException("Tipo de arquivo não suportado para extração de blocos.");
    }



    private List<ContentBlockDTO> extractPdfBlocks(File file, Long bookId) throws IOException {
        List<ContentBlockDTO> blocks = new ArrayList<>();
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = doc.getNumberOfPages();
            for (int page = 0; page < pageCount; page++) {
                log.info("Começando página: {}", page);
                PDPage pdPage = doc.getPage(page);
                // Extrai texto da página
                log.info("Extraindo texto da página: {}", page);
                stripper.setStartPage(page + 1);
                stripper.setEndPage(page + 1);
                String pageText = stripper.getText(doc).trim();
                if (!pageText.isEmpty()) {
                    blocks.add(new ContentBlockDTO("text", pageText, null, null));
                }
                // Extrai imagens da página
                log.info("Extraindo imagens da página: {}", page);
                PDResources resources = pdPage.getResources();
                int imageIndex = 1;
                for (var xObjectName : resources.getXObjectNames()) {
                    if (resources.isImageXObject(xObjectName)) {
                        PDImageXObject image = (PDImageXObject) resources.getXObject(xObjectName);
                        // Cria diretório das imagens para o livro
                        File imgDir = new File("uploads/books/" + bookId + "/images/");
                        imgDir.mkdirs();
                        String imageName = "page-" + (page + 1) + "-img-" + (imageIndex++) + ".png";
                        File imageFile = new File(imgDir, imageName);
                        BufferedImage bimg = image.getImage();
                        ImageIO.write(bimg, "png", imageFile);
                        String imageUrl = "/uploads/books/" + bookId + "/images/" + imageName;
                        blocks.add(new ContentBlockDTO("image", null, imageUrl, null));
                    }
                }
            }
        }
        return blocks;
    }

    @Override
    public List<File> slicePdf(File file, int chunkSize) throws IOException {
        List<File> chunks = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file)) {
            int totalPages = document.getNumberOfPages();
            for (int i = 0; i < totalPages; i += chunkSize) {
                // Criar novo documento contendo o chunk
                PDDocument chunkDoc = new PDDocument();
                for (int j = 0; j < chunkSize && (i + j) < totalPages; j++) {
                    chunkDoc.addPage(document.getPage(i + j));
                }
                File chunkFile = new File(file.getParent(), "chunk_" + i + ".pdf");
                chunkDoc.save(chunkFile);
                chunks.add(chunkFile);
                chunkDoc.close();
            }
        }
        return chunks; // Lista de páginas/chunks gerados
    }

    @Override
    public List<File> sliceDocx(File file, int chunkSize) throws IOException {
        List<File> chunks = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);

            String[] paragraphs = extractor.getText().split(System.lineSeparator()); // Divide por parágrafo
            StringBuilder chunkContent = new StringBuilder();
            int count = 0;

            for (int i = 0; i < paragraphs.length; i++) {
                chunkContent.append(paragraphs[i]).append(System.lineSeparator());
                count++;

                if (count == chunkSize || i == paragraphs.length - 1) {
                    // Salvar o chunk em um novo arquivo temporário
                    File chunkFile = File.createTempFile("chunk_", ".docx");
                    Files.writeString(chunkFile.toPath(), chunkContent.toString());
                    chunks.add(chunkFile);

                    // Resetar as variáveis
                    chunkContent.setLength(0);
                    count = 0;
                }
            }
        }
        return chunks;
    }

    @Override
    public List<File> sliceTxt(File file, int chunkSize) throws IOException {
        List<File> chunks = new ArrayList<>();
        List<String> lines = Files.readAllLines(file.toPath());
        StringBuilder chunkContent = new StringBuilder();
        int count = 0;

        for (int i = 0; i < lines.size(); i++) {
            chunkContent.append(lines.get(i)).append(System.lineSeparator());
            count++;

            if (count == chunkSize || i == lines.size() - 1) {
                // Salvar o chunk num arquivo temporário
                File chunkFile = File.createTempFile("chunk_", ".txt");
                Files.writeString(chunkFile.toPath(), chunkContent.toString());
                chunks.add(chunkFile);

                // Resetar as variáveis
                chunkContent.setLength(0);
                count = 0;
            }
        }
        return chunks;
    }



}
