package com.dnobretech.jarvistradutor.service;

import com.dnobretech.jarvistradutor.dto.ContentBlockDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface BookExtractionService {
    String extractText(File file, String originalFilename) throws IOException;
    List<ContentBlockDTO> extractBlocks(File file, String originalFilename, Long bookId) throws IOException;
    List<File> slicePdf(File file, int chunkSize) throws IOException;
    List<File> sliceDocx(File file, int chunkSize) throws IOException;
    List<File> sliceTxt(File file, int chunkSize) throws IOException;

}
