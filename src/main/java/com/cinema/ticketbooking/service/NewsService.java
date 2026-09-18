package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.News;
import com.cinema.ticketbooking.domain.request.ReqNewsDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.NewsRepository;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsService {
    private final NewsRepository repository;
    public NewsService(NewsRepository repository) { this.repository = repository; }

    public ResultPaginationDto list(String category, String search, Pageable pageable, boolean admin) {
        Specification<News> spec = (root, query, cb) -> cb.conjunction();
        if (!admin) spec = spec.and((root, query, cb) -> cb.isTrue(root.get("published")));
        if (category != null && !category.isBlank())
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        if (search != null && !search.isBlank()) {
            String term = "%" + search.trim().toLowerCase(java.util.Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), term, '\\'));
        }
        var sort = admin ? Sort.by(Sort.Direction.DESC, "createdAt", "id")
                : Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        var page = repository.findAll(spec, PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), sort));
        var meta = new ResultPaginationDto.Meta();
        meta.setCurrentPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize()); meta.setTotalItems(page.getTotalElements()); meta.setTotalPages(page.getTotalPages());
        var result = new ResultPaginationDto(); result.setMeta(meta); result.setData(page.getContent());
        return result;
    }

    public News get(Long id, boolean admin) {
        var news = repository.findById(id).orElseThrow(() -> new IdInvalidException("Không tìm thấy bài viết"));
        if (!admin && !news.isPublished()) throw new IdInvalidException("Không tìm thấy bài viết");
        return news;
    }

    @Transactional
    public News save(Long id, ReqNewsDto request) {
        var news = id == null ? new News() : get(id, true);
        news.setTitle(request.getTitle().trim()); news.setSummary(request.getSummary().trim());
        news.setContent(request.getContent()); news.setImage(request.getImage()); news.setCategory(request.getCategory());
        news.setFeatured(request.isFeatured()); news.setPublished(request.isPublished());
        return repository.save(news);
    }

    @Transactional
    public void delete(Long id) { repository.delete(get(id, true)); }
}
