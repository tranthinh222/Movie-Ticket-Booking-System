package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    boolean existsBySeedKey(String seedKey);
    java.util.Optional<News> findFirstByTitleAndSeedKeyIsNull(String title);
}
