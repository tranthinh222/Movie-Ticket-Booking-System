package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.FilmImage;
import com.cinema.ticketbooking.domain.response.ResFilmImage;
import com.cinema.ticketbooking.repository.FilmImageRepository;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmImageService {

    private final Cloudinary cloudinary;
    private final FilmRepository filmRepository;
    private final FilmImageRepository filmImageRepository;


//    public FilmImage uploadFilmImage(
//            Long filmId,
//            MultipartFile file
//    ) throws IOException {
//
//        Film film = filmRepository.findById(filmId)
//                .orElseThrow(() -> new RuntimeException("Film not found"));
//
//        Map res = cloudinary.uploader().upload(
//                file.getBytes(),
//                ObjectUtils.asMap(
//                        "folder", "films/" + filmId,
//                        "resource_type", "image"
//                )
//        );
//
//        FilmImage image = new FilmImage();
//        image.setFilm(film);
//        image.setPublicId(res.get("public_id").toString());
//        image.setUrl(res.get("secure_url").toString());
//
//        return filmImageRepository.save(image);
//    }

    public ResFilmImage getImageByFilm(Long filmId) {

        FilmImage image = filmImageRepository.findByFilmId(filmId)
                .orElseThrow(() ->
                        new RuntimeException("Film image not found"));

        ResFilmImage res = new ResFilmImage();
        res.setId(image.getId());
        res.setUrl(image.getUrl());
        return res;
    }

    public FilmImage uploadOrReplace(
            Film film,
            MultipartFile file
    ) throws IOException {


        filmImageRepository.findByFilmId(film.getId())
                .ifPresent(old -> {
                    try {
                        cloudinary.uploader().destroy(old.getPublicId(), ObjectUtils.emptyMap());
                        filmImageRepository.delete(old);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Map res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "films/" + film.getId())
        );

        FilmImage image = new FilmImage();
        image.setFilm(film);
        image.setPublicId(res.get("public_id").toString());
        image.setUrl(res.get("secure_url").toString());

        return filmImageRepository.save(image);
    }

    public void deleteByFilmImageId(Long filmId) {

        Optional<FilmImage> image = filmImageRepository.findByFilmId(filmId);
        if  (image == null) {
            throw new IdInvalidException("film image with id " +  filmId + " not found");
        }

        try {
            cloudinary.uploader().destroy(
                    image.get().getPublicId(),
                    ObjectUtils.emptyMap()
            );

            filmImageRepository.delete(image.get());

        } catch (Exception e) {
            throw new RuntimeException("Delete film image failed", e);
        }
    }

}
