package com.codepuran.carvaan.controller;

import com.codepuran.carvaan.dto.GenericResponse;
import com.codepuran.carvaan.service.DataPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/prepare")
@RequiredArgsConstructor
public class DataPreparationController {

    private final DataPreparationService dataPreparationService;

    @GetMapping("/split-files")
    public GenericResponse splitDataFile() throws IOException {
        dataPreparationService.prepareData();
        return GenericResponse
                .builder()
                .message("Data preparation completed successfully")
                .build();
    }

    @GetMapping("/artistes")
    public GenericResponse processArtistesSongs() throws IOException, CloneNotSupportedException {
        dataPreparationService.processArtistes();
        return GenericResponse
                .builder()
                .message("Artistes added successfully")
                .build();
    }
}
