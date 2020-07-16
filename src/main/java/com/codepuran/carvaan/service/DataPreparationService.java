package com.codepuran.carvaan.service;

import com.codepuran.carvaan.dto.FileSplitDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class DataPreparationService {

    public void prepareData() throws IOException {
        var whereToSplitFile = new int[] {2,3,91,141};
        Resource resource = new ClassPathResource("data/Saregama_Carvaan_Songlist_2.0.pdf");
        File file = resource.getFile();
        if(file.exists()) {
            PDDocument document = PDDocument.load(file);
            List<FileSplitDto> fileSplitDtos = getSplitterDetail();
            for(FileSplitDto fileSplitDto : fileSplitDtos) {
                Splitter splitter = new Splitter();
                splitter.setStartPage(fileSplitDto.getFirstPage());
                splitter.setEndPage(fileSplitDto.getLastPage());
                splitter.setSplitAtPage(fileSplitDto.getLastPage());
                List<PDDocument> splitFile =  splitter.split(document);
                for(PDDocument pdDocument : splitFile) {
                    File newFile = new File("processed/"+ fileSplitDto.getName()+".pdf");
                    pdDocument.save(newFile);
                    log.debug("-----------------------------------------------------------");
                    log.debug(newFile.getAbsolutePath());
                    log.debug(newFile.getPath());
                    pdDocument.close();
                }
            }
            document.close();
        }
    }

    public void processArtistes() throws IOException {
        // List of file in directory
        List<File> artistesFiles = listFilesInDirectory("data/processed/artistes");
        int i = 0;
        for(File file: artistesFiles) {
            //TODO read pdf file data as text
            PDDocument document = PDDocument.load(file);
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String pdfData = pdfTextStripper.getText(document);
            log.debug(pdfData);
            //TODO make a list of it

            document.close();
            /*++i;
            if(i==1){
                break;
            }*/
        }
        //TODO Save prepared list to db
    }

    public List<File> listFilesInDirectory(String pathRelativeResourceDirectory) throws IOException {
        Resource resource = new ClassPathResource(pathRelativeResourceDirectory);
        var resourcePath =  resource.getURI().getPath();
        log.debug(resourcePath);
        resourcePath = resourcePath.substring(1,resourcePath.length());
        log.debug("Updated Path: "+ resourcePath);
        List<File> filesInDirectory = null;
        try (Stream<Path> walk = Files.walk(Paths.get(resourcePath))) {
            /*List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
           result.forEach(System.out::println);*/
           filesInDirectory =  walk.filter(Files::isRegularFile).map(x -> x.toFile()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesInDirectory;
    }

    List<FileSplitDto> getSplitterDetail() {
        List<FileSplitDto> splitDtos = new ArrayList<>();
        splitDtos.add(FileSplitDto.builder().name("Index").firstPage(2).lastPage(2).build());
        splitDtos.add(FileSplitDto.builder().name("Lata Mangeshkar").firstPage(3).lastPage(14).build());
        splitDtos.add(FileSplitDto.builder().name("Kishore Kumar").firstPage(15).lastPage(20).build());
        splitDtos.add(FileSplitDto.builder().name("Asha Bhosle").firstPage(21).lastPage(26).build());
        splitDtos.add(FileSplitDto.builder().name("Mohammed Rafi").firstPage(27).lastPage(34).build());
        splitDtos.add(FileSplitDto.builder().name("Mukesh").firstPage(35).lastPage(37).build());
        splitDtos.add(FileSplitDto.builder().name("Hemant Kumar & Geeta Dutt").firstPage(38).lastPage(38).build());
        splitDtos.add(FileSplitDto.builder().name("Manna Dey").firstPage(39).lastPage(40).build());
        splitDtos.add(FileSplitDto.builder().name("Jagjit Singh").firstPage(41).lastPage(43).build());
        splitDtos.add(FileSplitDto.builder().name("Talat Mahmood").firstPage(44).lastPage(45).build());
        splitDtos.add(FileSplitDto.builder().name("S.D. Burman").firstPage(46).lastPage(47).build());
        splitDtos.add(FileSplitDto.builder().name("R.D. Burman").firstPage(48).lastPage(51).build());
        splitDtos.add(FileSplitDto.builder().name("Laxmikant-Pyarelal").firstPage(52).lastPage(56).build());//TODO
        splitDtos.add(FileSplitDto.builder().name("Kalyanji-Anandji").firstPage(57).lastPage(59).build());//TODO
        splitDtos.add(FileSplitDto.builder().name("Naushad").firstPage(60).lastPage(60).build());//TODO
        splitDtos.add(FileSplitDto.builder().name("Shankar-Jaikishan").firstPage(61).lastPage(65).build());//TODO
        splitDtos.add(FileSplitDto.builder().name("O.P. Nayyar").firstPage(66).lastPage(67).build());
        splitDtos.add(FileSplitDto.builder().name("Gulzar").firstPage(68).lastPage(69).build());
        splitDtos.add(FileSplitDto.builder().name("Madan Mohan").firstPage(70).lastPage(71).build());
        splitDtos.add(FileSplitDto.builder().name("Kaifi Azmi - Javed Akhtar").firstPage(72).lastPage(73).build());
        splitDtos.add(FileSplitDto.builder().name("Sahir Ludhianvi").firstPage(74).lastPage(75).build());
        splitDtos.add(FileSplitDto.builder().name("Anand Bakshi").firstPage(76).lastPage(81).build());
        splitDtos.add(FileSplitDto.builder().name("Majrooh Sultanpuri").firstPage(82).lastPage(85).build());
        splitDtos.add(FileSplitDto.builder().name("Top 300").firstPage(86).lastPage(89).build());
        splitDtos.add(FileSplitDto.builder().name("Romantic").firstPage(91).lastPage(100).build());
        splitDtos.add(FileSplitDto.builder().name("Sad").firstPage(101).lastPage(106).build());
        splitDtos.add(FileSplitDto.builder().name("Happy").firstPage(107).lastPage(117).build());
        splitDtos.add(FileSplitDto.builder().name("Shakti").firstPage(124).lastPage(127).build());
        splitDtos.add(FileSplitDto.builder().name("Ghazal").firstPage(118).lastPage(123).build());
        splitDtos.add(FileSplitDto.builder().name("Spiritual").firstPage(128).lastPage(129).build());
        splitDtos.add(FileSplitDto.builder().name("Film Instrumental").firstPage(130).lastPage(135).build());
        splitDtos.add(FileSplitDto.builder().name("Sufi").firstPage(136).lastPage(137).build());
        splitDtos.add(FileSplitDto.builder().name("Hindustani Classical Instrumental").firstPage(138).lastPage(140).build());
        splitDtos.add(FileSplitDto.builder().name("Geetmala").firstPage(141).lastPage(150).build());
        return splitDtos;
    }
}
