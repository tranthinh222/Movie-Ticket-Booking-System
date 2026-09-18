package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.News;
import com.cinema.ticketbooking.domain.request.ReqNewsDto;
import com.cinema.ticketbooking.repository.NewsRepository;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(NewsService.class)
class NewsServiceIntegrationTest {
    @Autowired NewsService service;
    @Autowired NewsRepository repository;

    private ReqNewsDto article(String title, String category, boolean published) {
        var request = new ReqNewsDto();
        request.setTitle(title); request.setCategory(category); request.setPublished(published);
        request.setSummary("Summary"); request.setContent("Full article content");
        return request;
    }

    @Test
    void publicListAndDetailsHideDraftsWhileAdminCanManageThem() {
        var draft = service.save(null, article("Draft", "Tin mới", false));
        service.save(null, article("Published", "Review phim", true));
        assertEquals(1, service.list(null, null, PageRequest.of(0, 10), false).getMeta().getTotalItems());
        assertEquals(2, service.list(null, null, PageRequest.of(0, 10), true).getMeta().getTotalItems());
        assertThrows(IdInvalidException.class, () -> service.get(draft.getId(), false));
        var updated = article("Published draft", "Tin mới", true);
        service.save(draft.getId(), updated);
        assertEquals("Full article content", service.get(draft.getId(), false).getContent());
        service.delete(draft.getId());
        assertFalse(repository.existsById(draft.getId()));
    }

    @Test
    void categorySearchPaginationAndFeaturedOrderWorkAgainstDatabase() {
        service.save(null, article("Normal", "Tin mới", true));
        var input = article("Featured 100%", "Review phim", true);
        input.setFeatured(true);
        var featured = service.save(null, input);
        var first = service.list(null, null, PageRequest.of(0, 1), false);
        assertEquals(2, first.getMeta().getTotalPages());
        assertEquals(featured.getId(), ((List<News>) first.getData()).get(0).getId());
        assertEquals(1, service.list("Review phim", "100%", PageRequest.of(0, 10), false).getMeta().getTotalItems());
        assertEquals(0, service.list("Tin mới", "100%", PageRequest.of(0, 10), false).getMeta().getTotalItems());
    }
}
