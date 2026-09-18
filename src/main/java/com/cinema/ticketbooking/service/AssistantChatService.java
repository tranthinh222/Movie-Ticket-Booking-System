package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.request.*;
import com.cinema.ticketbooking.domain.response.*;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.text.Normalizer;

@Service
public class AssistantChatService {
  private static final Set<String> GREETINGS = Set.of(
      "hello", "hi", "hey", "helo", "xin chao", "chao", "chao ban", "chao bot",
      "chao chatbot", "chao ad", "chao admin", "hello bot", "hi bot", "hey bot",
      "xin chao ban", "xin chao bot", "chao cinemovie", "hello cinemovie");

  private boolean isGreeting(String message) {
    if (message == null) return false;
    String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D')
        .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ")
        .trim().replaceAll("\\s+", " ");
    return GREETINGS.contains(normalized);
  }

  private final MovieRecommendationService movies;
  private final SeatRecommendationService seats;
  private final FilmRepository films;

  public AssistantChatService(MovieRecommendationService movies, SeatRecommendationService seats,
      FilmRepository films) {
    this.movies = movies;
    this.seats = seats;
    this.films = films;
  }

  @Transactional(readOnly = true)
  public ResAssistantChatDto chat(ReqAssistantChatDto request) {
    if (isGreeting(request.getMessage())) {
      return new ResAssistantChatDto(
          "Xin chào! Tôi là trợ lý CineMovie. Bạn muốn tìm phim, xem thông tin phim hay chọn ghế? Hãy chọn mục hỗ trợ và bộ lọc bên dưới rồi gửi yêu cầu nhé.",
          List.of(), List.of());
    }
    switch (request.getTopic()) {
      case SEATS:
        if (request.getSeats() == null)
          return new ResAssistantChatDto(
              "Bạn hãy chọn một suất chiếu trên trang đặt vé, rồi gửi lại yêu cầu gợi ý ghế nhé.", List.of(),
              List.of());
        var groups = seats.recommend(request.getSeats());
        return new ResAssistantChatDto(groups.isEmpty()
            ? "Không có nhóm ghế liền nhau phù hợp. Bạn có thể đổi số người, ngân sách hoặc suất chiếu."
            : "Đây là các phương án ghế còn trống cho suất bạn đã chọn. Ghế chưa được giữ; hãy xác nhận trên sơ đồ đặt vé.",
            List.of(), groups);
      case MOVIE_DETAILS:
        if (request.getFilmId() == null)
          return new ResAssistantChatDto(
              "Bạn hãy mở trang chi tiết một phim rồi hỏi lại để tôi cung cấp thông tin đúng phim nhé.", List.of(),
              List.of());
        var f = films.findById(request.getFilmId()).orElseThrow(() -> new BadRequestException("Phim không tồn tại."));
        String reply = f.getName() + "\nThể loại: " + (f.getGenre() == null ? "Chưa cập nhật" : f.getGenre())
            + "\nThời lượng: " + (f.getDuration() == null ? "Chưa cập nhật" : f.getDuration() + " phút")
            + "\nNgôn ngữ: " + (f.getLanguage() == null ? "Chưa cập nhật" : f.getLanguage());
        return new ResAssistantChatDto(reply, List.of(new ResMovieRecommendationDto(f.getId(), f.getName(),
            f.getThumbnail(), f.getDuration(), f.getGenre(), "Xem nội dung và suất chiếu trên trang phim.")),
            List.of());
      default:
        var result = movies
            .recommend(request.getPreferences() == null ? new ReqMovieRecommendationDto() : request.getPreferences());
        return new ResAssistantChatDto(
            result.isEmpty()
                ? "Chưa có phim có suất chiếu phù hợp với bộ lọc. Bạn thử đổi ngày, thể loại hoặc thời lượng nhé."
                : "Các phim dưới đây có suất chiếu phù hợp với bộ lọc của bạn. Chọn phim để xem chi tiết và đặt vé.",
            result, List.of());
    }
  }
}
