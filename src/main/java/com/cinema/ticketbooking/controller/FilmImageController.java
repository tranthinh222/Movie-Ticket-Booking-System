package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.FilmImage;
import com.cinema.ticketbooking.domain.response.ResFilmImage;
import com.cinema.ticketbooking.repository.FilmImageRepository;
import com.cinema.ticketbooking.service.FilmImageService;
import com.cinema.ticketbooking.service.FilmService;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/v1")
public class FilmImageController {
    private final FilmImageService filmImageService;
    private final FilmService filmService;

    public  FilmImageController(FilmImageService filmImageService, FilmService filmService) {
        this.filmImageService = filmImageService;
        this.filmService = filmService;
    }
//    @PostMapping("/film-images/{id}")
//    public ResponseEntity<?> uploadImage(
//            @PathVariable Long id,
//            @RequestParam MultipartFile file
//    ) throws IOException {
//
//        FilmImage image = filmImageService.uploadFilmImage(id, file);
//
//        return ResponseEntity.ok(Map.of(
//                "imageId", image.getId(),
//                "url", image.getUrl()
//        ));
//    }

    @GetMapping("/film-images/{id}")
    public ResFilmImage getFilmImage(@PathVariable Long id) {
        return filmImageService.getImageByFilm(id);
    }

    @PostMapping("/film-images/{filmId}")
    public ResponseEntity<?> uploadOrReplaceImage(
            @PathVariable Long filmId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Film film = filmService.getFilmById(filmId);
        if (film == null)
        {
            throw new IdInvalidException("film with id " +  filmId + " not found");
        }
        FilmImage image = filmImageService.uploadOrReplace(film, file);

        return ResponseEntity.ok(Map.of(
                "imageId", image.getId(),
                "url", image.getUrl()
        ));
    }

    @DeleteMapping("/film-images/{filmId}")
    public ResponseEntity<?> deleteFilmImage(@PathVariable Long filmId) {

        filmImageService.deleteByFilmImageId(filmId);

        return ResponseEntity.ok(Map.of(
                "message", "Film image deleted successfully"
        ));
    }

}
