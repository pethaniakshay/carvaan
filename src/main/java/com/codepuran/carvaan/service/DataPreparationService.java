package com.codepuran.carvaan.service;

import com.codepuran.carvaan.dto.FileSplitDto;
import com.codepuran.carvaan.dto.ParsedSongsDto;
import com.codepuran.carvaan.entity.*;
import com.codepuran.carvaan.repository.*;
import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataPreparationService {

    private final AlbumRepository albumRepository;

    private final ArtistesRepository artistesRepository;

    private final SongRepository songRepository;

    private final FilmRepository filmRepository;

    private final MoodRepository moodRepository;

    public void prepareData() throws IOException {
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

    public void processArtistes() throws IOException, CloneNotSupportedException {
        List<File> artistesFiles = listFilesInDirectory("data/processed/artistes");
        Pattern digitPattern = Pattern.compile("^\\d+\\.");
        Pattern filmPattern = Pattern.compile("^\\s?Film:");
        Pattern artistesPattern = Pattern.compile("^S?Artistes?:");
        Pattern albumPattern = Pattern.compile("^\\s?Album:");
        for(File file: artistesFiles) {
            String pdfData = getContentOfPDFAsString(file);
            String[] allLines = pdfData.split("\\r?\\n");
            String primaryArtiste = file.getName().split(".pdf")[0];
            String current = "";
            List<ParsedSongsDto> parsedSongsDtos = new ArrayList<>();
            ParsedSongsDto parsedSongDto = new ParsedSongsDto();
            parsedSongDto.setPrimaryArtiste(primaryArtiste);
            parsedSongDto.setFilled(false);
            for(String line : allLines) {
                Matcher filmMatcher = filmPattern.matcher(line);
                Matcher artisteMatcher = artistesPattern.matcher(line);
                Matcher digitMatcher = digitPattern.matcher(line);
                Matcher albumMatcher = albumPattern.matcher(line);
                if(digitMatcher.find()) {
                    current = "digit";
                    if(parsedSongDto.getFilled()) {
                        //parsedSongsDtos.add((ParsedSongsDto) parsedSongDto.clone());
                        addParseSongDtoToListAndCleanItBeforeForArtistes(parsedSongsDtos,parsedSongDto);
                        parsedSongDto = new ParsedSongsDto();
                        parsedSongDto.setPrimaryArtiste(primaryArtiste);
                        parsedSongDto.setFilled(false);
                    }
                    Pattern numberPattern = Pattern.compile("^\\d+");
                    Matcher numberMatcher = numberPattern.matcher(line);
                    if(numberMatcher.find()){
                        parsedSongDto.setNo(numberMatcher.group());
                    }
                    String name = line.split("^\\d+\\.")[1];
                    parsedSongDto.setName(name);
                } else if(filmMatcher.find()) {
                    current = "film";
                    String film = line.split("^\\s?Film:")[1];
                    parsedSongDto.setFilm(film);
                } else if(albumMatcher.find()){
                    current = "album";
                    String album = line.split("^\\s?Album:")[1];
                    parsedSongDto.setAlbum(album);
                } else if(artisteMatcher.find()) {
                    current = "artiste";
                    parsedSongDto.setFilled(true);
                    String artiste = line.split("^S?Artistes?:")[1];
                    parsedSongDto.setRawArtistes(artiste);
                } else {
                    switch (current) {
                        case "digit" -> parsedSongDto.setName(parsedSongDto.getName() + line);
                        case "film" -> parsedSongDto.setFilm(parsedSongDto.getFilm() + line);
                        case "album" -> parsedSongDto.setAlbum(parsedSongDto.getAlbum() + line);
                        case "artiste" -> parsedSongDto.setRawArtistes(parsedSongDto.getRawArtistes() + line);
                        default -> log.warn("Akshay, You need to look here || Line: {}", line);
                    }
                }
            }
            addParseSongDtoToListAndCleanItBeforeForArtistes(parsedSongsDtos,parsedSongDto);
            log.debug("Primary Artiste: {} || Songs Size: {}", primaryArtiste, parsedSongsDtos.size());
            saveArtistesSongsToDatabase(parsedSongsDtos);
        }
    }

    private void saveArtistesSongsToDatabase(List<ParsedSongsDto> parsedSongsDtos) {
        saveAlbums(parsedSongsDtos);
        saveArtistes(parsedSongsDtos);
        saveFilm(parsedSongsDtos);
        saveSongs(parsedSongsDtos);
    }

    private void saveSongs(List<ParsedSongsDto> parsedSongsDtos) {

        List<Film> films = filmRepository.findAll();
        List<Album> albums = albumRepository.findAll();
        List<Artiste> artistes = artistesRepository.findAll();
        List<Mood> moods = moodRepository.findAll();

        for(ParsedSongsDto parsedSongsDto: parsedSongsDtos) {
            List<Song> songs = songRepository.findByName(parsedSongsDto.getName());
            Song song = null;
            for(Song eachSong: songs) {
                if(eachSong.getFilm() != null && parsedSongsDto.getFilm() != null) {
                    if(eachSong.getFilm() != null && eachSong.getFilm().getName().equals(parsedSongsDto.getFilm())) {
                        song = eachSong;
                    } else if(eachSong.getAlbum()!= null && eachSong.getAlbum().getName().equals(parsedSongsDto.getAlbum())) {
                        song = eachSong;
                    }
                }
            }
            if(song == null) {
                song = new Song();
            }

            song.setName(parsedSongsDto.getName());

            if(parsedSongsDto.getFilm() != null) {
                Optional<Film> filmOptional = films.stream().filter(o -> o.getName().equals(parsedSongsDto.getFilm())).findFirst();
                Film film;
                if(filmOptional.isEmpty()) {
                    film = Film.builder()
                            .name(parsedSongsDto.getFilm())
                            .build();
                    film = filmRepository.saveAndFlush(film);
                    films.add(film);
                } else {
                    film = filmOptional.get();
                }
                song.setFilm(film);
            }

            if(parsedSongsDto.getAlbum() != null) {
                Optional<Album> albumOptional = albums.stream().filter(o -> o.getName().equals(parsedSongsDto.getAlbum())).findFirst();
                Album album;
                if(albumOptional.isEmpty()) {
                    album = Album.builder()
                            .name(parsedSongsDto.getAlbum())
                            .build();
                    album = albumRepository.saveAndFlush(album);
                    albums.add(album);
                } else {
                    album = albumOptional.get();
                }
                song.setAlbum(album);
            }

            if(parsedSongsDto.getArtistes() != null && !parsedSongsDto.getArtistes().isEmpty()) {
                Set<Artiste> artistesOfSongs = new HashSet<>();

                for(String artisteName : parsedSongsDto.getArtistes()) {
                    Optional<Artiste> artisteOptional = artistes.stream().filter(o -> o.getName().equals(artisteName)).findFirst();

                    Artiste artiste;

                    if(artisteOptional.isEmpty()) {
                        artiste = Artiste.builder()
                                .name(artisteName)
                                .isPrimary(false)
                                .build();
                        artiste = artistesRepository.saveAndFlush(artiste);
                        artistes.add(artiste);
                    } else {
                        artiste = artisteOptional.get();
                    }

                    artistesOfSongs.add(artiste);
                }

                Set<Artiste> existingArtistes = song.getArtistes();

                if(existingArtistes == null) {
                    existingArtistes = new HashSet<>();
                }
                existingArtistes.addAll(artistesOfSongs);
                song.setArtistes(existingArtistes);
            }

            if(parsedSongsDto.getMood() != null) {
                Optional<Mood> moodOptional = moods.stream().filter(o -> o.getName().equals(parsedSongsDto.getMood())).findFirst();
                Mood mood;
                if(moodOptional.isEmpty()) {
                    mood = Mood.builder()
                            .name(parsedSongsDto.getAlbum())
                            .build();
                    mood = moodRepository.saveAndFlush(mood);
                    moods.add(mood);
                } else {
                    mood = moodOptional.get();
                }

                if(song.getMood() != null) {
                    if(!song.getMood().getName().equals(mood.getName())){
                        log.warn("--------------------------------------------------------------------------------");
                        log.warn("Another Moved Found For Same Song Mood Found");
                        log.warn("Song: {}", song);
                        log.warn("ParsedSongDto: {}", parsedSongsDto);
                        log.warn("--------------------------------------------------------------------------------");
                    }
                } else {
                    song.setMood(mood);
                }
            }
            songRepository.saveAndFlush(song);
        }
    }

    private void saveAlbums(List<ParsedSongsDto> parsedSongsDtos) {
        Set<String> albumNames = parsedSongsDtos.stream().map(ParsedSongsDto::getAlbum).collect(Collectors.toSet());

        albumNames.remove(null);
        albumNames.remove("");

        List<Album> albums = new ArrayList<>();

        for(String albumName: albumNames) {
            albums.add(Album.builder()
                    .name(albumName)
                    .build());
        }

        for(Album album : albums) {
            Optional<Album> existingFilm = albumRepository.findByName(album.getName());
            if(existingFilm.isEmpty()) {
                albumRepository.saveAndFlush(album);
            }
        }
    }

    private void saveFilm(List<ParsedSongsDto> parsedSongsDtos) {
        Set<String> filmNames = parsedSongsDtos.stream().map(ParsedSongsDto::getFilm).collect(Collectors.toSet());

        filmNames.remove(null);
        filmNames.remove("");

        List<Film> films = new ArrayList<>();

        for(String filmName: filmNames) {
            films.add(Film.builder()
                    .name(filmName)
                    .build());
        }

        for(Film film : films) {
            Optional<Film> existingFilm = filmRepository.findByName(film.getName());
            if(existingFilm.isEmpty()) {
                filmRepository.saveAndFlush(film);
            }
        }
    }

    private void saveArtistes(List<ParsedSongsDto> parsedSongsDtos) {
        Set<String> primaryArtistesNames = parsedSongsDtos.stream().map(ParsedSongsDto::getPrimaryArtiste).collect(Collectors.toSet());

        primaryArtistesNames.remove(null);
        primaryArtistesNames.remove("");

        List<Artiste> artists = new ArrayList<>();

        for(ParsedSongsDto parsedSongsDto : parsedSongsDtos) {
            Set<String> artistesOfSongs = parsedSongsDto.getArtistes();
            if(artistesOfSongs == null) {
                artistesOfSongs = new HashSet<>();
            }
            for(String parsedArtistName: artistesOfSongs) {
                artists.add(Artiste.builder()
                        .isPrimary(primaryArtistesNames.contains(parsedArtistName))
                        .name(parsedArtistName.trim())
                        .build());
            }
        }

        artists = artists.stream().filter(distinctByKey(Artiste::getName)).collect(Collectors.toList());

        for(Artiste artiste : artists) {
            Optional<Artiste> existingFilm = artistesRepository.findByName(artiste.getName());
            if(existingFilm.isEmpty()) {
                artistesRepository.saveAndFlush(artiste);
            }
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private void addParseSongDtoToListAndCleanItBeforeForArtistes(List<ParsedSongsDto> parsedSongsDtos, ParsedSongsDto parsedSongsDto) throws CloneNotSupportedException {

        String songName = parsedSongsDto.getName();
        if(songName != null) {
            parsedSongsDto.setName(songName.replaceAll("( +)"," ").trim());
        } else {
            log.warn("Song Found Null || Artiste: {} || Record No: {}", parsedSongsDto.getName(), parsedSongsDto.getNo());
        }

        String filmName = parsedSongsDto.getFilm();
        if(filmName != null) {
            parsedSongsDto.setFilm(filmName.replaceAll("( +)"," ").trim());
        } else {
            if(parsedSongsDto.getAlbum() == null)
                log.warn("Film Found Null || Artiste: {} || Record No: {} || Song: {}", parsedSongsDto.getPrimaryArtiste(), parsedSongsDto.getNo(), parsedSongsDto.getName());
        }

        String album = parsedSongsDto.getAlbum();
        if(album != null) {
            parsedSongsDto.setAlbum(album.replaceAll("( +)"," ").trim());
        } else {
            if(parsedSongsDto.getFilm() == null)
                log.warn("Album Found Null || Artiste: {} || Record No: {} || Song: {}", parsedSongsDto.getPrimaryArtiste(), parsedSongsDto.getNo(), parsedSongsDto.getName());
        }

        String rawArtiste = parsedSongsDto.getRawArtistes();
        if(rawArtiste != null) {
            rawArtiste = rawArtiste.replaceAll("( +)"," ").trim();
            List<String> tempList = Arrays.asList(rawArtiste.split(","));
            Set<String> artisteSet = new HashSet<>();
            artisteSet.add(parsedSongsDto.getPrimaryArtiste());
            artisteSet.addAll(tempList);
            parsedSongsDto.setArtistes(artisteSet);
        } else {
            log.warn("Raw Artiste Found Null || Artiste: {} || Record No: {} || Song: {}", parsedSongsDto.getPrimaryArtiste(), parsedSongsDto.getNo(), parsedSongsDto.getName());
        }
        parsedSongsDtos.add((ParsedSongsDto) parsedSongsDto.clone());
    }

    private String getContentOfPDFAsString(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        String result = pdfTextStripper.getText(document);
        document.close();
        return result;
    }

    private List<File> listFilesInDirectory(String pathRelativeResourceDirectory) throws IOException {
        Resource resource = new ClassPathResource(pathRelativeResourceDirectory);
        var resourcePath =  resource.getURI().getPath();
        resourcePath = resourcePath.substring(1);
        List<File> filesInDirectory = null;
        try (Stream<Path> walk = Files.walk(Paths.get(resourcePath))) {
           filesInDirectory =  walk.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesInDirectory;
    }

    private List<FileSplitDto> getSplitterDetail() {
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
