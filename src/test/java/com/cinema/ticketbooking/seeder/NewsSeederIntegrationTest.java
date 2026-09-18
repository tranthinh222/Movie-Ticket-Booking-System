package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.News;
import com.cinema.ticketbooking.repository.NewsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NewsSeederIntegrationTest {
    @Autowired NewsRepository repository;

    @Test
    void addsMissingDemoArticlesAndPreservesExistingEditsOnRestart() throws Exception {
        var existing = new News();
        existing.setTitle("Top 10 phim kinh dị đáng xem nhất tháng 10");
        existing.setSummary("Admin summary"); existing.setContent("Admin content");
        existing.setCategory("Tin mới"); existing.setPublished(false);
        existing = repository.saveAndFlush(existing);
        var id = existing.getId();
        var seeder = new NewsSeeder(repository, new ObjectMapper());
        seeder.run();
        repository.flush();
        assertEquals(27, repository.count());
        var linked = repository.findById(id).orElseThrow();
        assertEquals("news-demo-001", linked.getSeedKey());
        assertEquals("Admin content", linked.getContent());
        assertFalse(linked.isPublished());
        linked.setTitle("Edited title");
        repository.saveAndFlush(linked);
        seeder.run();
        repository.flush();
        assertEquals(27, repository.count());
        assertEquals("Edited title", repository.findById(id).orElseThrow().getTitle());
        assertEquals(20, repository.findAll().stream()
                .filter(n -> Integer.parseInt(n.getSeedKey().substring(10)) >= 8)
                .filter(n -> n.getImage().startsWith("https://res.cloudinary.com/"))
                .filter(n -> n.getContent().contains("\n\n")).count());
    }
}
