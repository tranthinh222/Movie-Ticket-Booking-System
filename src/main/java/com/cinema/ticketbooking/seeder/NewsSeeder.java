package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.News;
import com.cinema.ticketbooking.repository.NewsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component @Profile("local") @Order(7)
public class NewsSeeder implements CommandLineRunner {
    private final NewsRepository repository;
    private final ObjectMapper mapper;
    public NewsSeeder(NewsRepository repository, ObjectMapper mapper) { this.repository = repository; this.mapper = mapper; }
    @Override @Transactional
    public void run(String... args) throws Exception {
        try (var input = new ClassPathResource("data/news-demo.json").getInputStream()) {
            List<News> articles = mapper.readValue(input, new TypeReference<List<News>>() {});
            int added = 0;
            for (News article : articles) {
                if (article.getSeedKey() == null || article.getSeedKey().isBlank()) {
                    throw new IllegalStateException("Demo news requires a stable seedKey");
                }
                if (repository.existsBySeedKey(article.getSeedKey())) continue;
                var legacy = repository.findFirstByTitleAndSeedKeyIsNull(article.getTitle());
                if (legacy.isPresent()) {
                    // Link old demo rows without overwriting edits or changing their IDs.
                    legacy.get().setSeedKey(article.getSeedKey());
                    repository.save(legacy.get());
                } else {
                    repository.save(article);
                    added++;
                }
            }
            System.out.println("Local demo: added " + added + " news articles");
        }
    }
}
