package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Order(6)
@RequiredArgsConstructor
public class FilmImageSeeder implements CommandLineRunner {
    private final Cloudinary cloudinary;
    private final FilmRepository filmRepository;

    @Override
    public void run(String... args) throws IOException {
        List<Film> films = filmRepository.findAll();

        if (films.isEmpty() || films.size() != 30) {
            return;
        }
        for (int i = 1; i <= 30; i++) {
            if (films.get(i - 1).getThumbnail() != null &&  !films.get(i - 1).getThumbnail().isBlank()) {
                continue;
            }

            File file = new File(
                    "src/main/resources/static/images/film/" + i + ".jpg"
            );

            Map uploadResult = cloudinary.uploader().upload(
                    file,
                    ObjectUtils.asMap(
                            "folder", "uploads",
                            "resource_type", "image"));

            films.get(i - 1).setThumbnail(uploadResult.get("secure_url").toString());
        }
        updateThumbnail(films);
        System.out.println("Uploaded images for films");
    }

    @Transactional
    public void updateThumbnail(List<Film> films) {
        filmRepository.saveAll(films);
    }
}
