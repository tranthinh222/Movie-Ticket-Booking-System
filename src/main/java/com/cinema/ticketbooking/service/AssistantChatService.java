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
import java.util.Objects;
import java.text.Normalizer;

@Service
public class AssistantChatService {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssistantChatService.class);

  private <T> T timed(String stage, java.util.function.Supplier<T> action) {
    long start = System.nanoTime();
    boolean success = false;
    try {
      T result = action.get();
      success = true;
      return result;
    } finally {
      log.info("Assistant stage={} durationMs={} success={}", stage, (System.nanoTime() - start) / 1_000_000, success);
    }
  }

  private static final Set<String> GREETINGS = Set.of(
      "hello", "hi", "hey", "helo", "xin chao", "chao", "chao ban", "chao bot",
      "chao chatbot", "chao ad", "chao admin", "hello bot", "hi bot", "hey bot",
      "xin chao ban", "xin chao bot", "chao cinemovie", "hello cinemovie");

  private boolean isGreeting(String message) {
    if (message == null)
      return false;
    String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D')
        .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ")
        .trim().replaceAll("\\s+", " ");
    return GREETINGS.contains(normalized);
  }

  private final MovieRecommendationService movies;
  private final SeatRecommendationService seats;
  private final FilmRepository films;
  private final GeminiIntentService gemini;
  private final DiscountService discounts;

  public AssistantChatService(MovieRecommendationService movies, SeatRecommendationService seats,
      FilmRepository films, GeminiIntentService gemini, DiscountService discounts) {
    this.movies = movies;
    this.seats = seats;
    this.films = films;
    this.gemini = gemini;
    this.discounts = discounts;
  }

  public ResAssistantChatDto chat(ReqAssistantChatDto request) {
    var memory = request.getMemory();
    if (memory != null && ((memory.intent() == AiIntentDto.Intent.SEATS && !Objects
        .equals(request.getMemoryShowTimeId(), request.getSeats() == null ? null : request.getSeats().getShowTimeId()))
        || (memory.intent() == AiIntentDto.Intent.MOVIE_DETAILS
            && !Objects.equals(request.getMemoryFilmId(), request.getFilmId())))) {
      request.setMemory(null);
      request.setHistory(List.of());
    }
    var response = timed("chat", () -> process(request));
    return new ResAssistantChatDto(response.reply(), response.movies(), response.seatGroups(), request.getMemory(),
        response.discounts());
  }

  private ResAssistantChatDto process(ReqAssistantChatDto request) {
    if (isGreeting(request.getMessage())) {
      return new ResAssistantChatDto(
          "Xin chào! Tôi là trợ lý CineMovie. Bạn muốn tìm phim, xem thông tin phim chọn ghế hay xem ưu đãi? Hãy chọn mục hỗ trợ và bộ lọc bên dưới rồi gửi yêu cầu nhé.",
          List.of(), List.of());
    }
    if (request.isUseAi()) {
      var intent = timed("gemini", () -> gemini.interpret(request));
      if (intent.intent() != AiIntentDto.Intent.GREETING && intent.intent() != AiIntentDto.Intent.CLARIFY && intent.intent() != AiIntentDto.Intent.OFF_TOPIC)
        request.setMemory(intent);
      switch (intent.intent()) {
        case GREETING:
          return new ResAssistantChatDto(
              "Xin chào! Tôi có thể giúp bạn tìm phim, gợi ý ghế và xem ưu đãi. Bạn muốn xem thể loại nào?", List.of(),
              List.of());
        case OFF_TOPIC:
          return new ResAssistantChatDto(
              "Tôi là trợ lý CineMovie, hỗ trợ tìm phim, xem thông tin phim, tra cứu ưu đãi và gợi ý ghế. Câu hỏi này nằm ngoài phạm vi hỗ trợ. Bạn có thể hỏi: “Tôi có 150k, xem phim gì?” hoặc “Có mã giảm giá nào?”.",
              List.of(), List.of());
        case CLARIFY:
          return new ResAssistantChatDto(
              "Bạn hãy nêu rõ muốn tìm phim theo thể loại, thời lượng/ngày xem, hỏi phim đang mở chọn ghế theo số người/ngân sách hoặc hỏi về mã ưu đãi. Điều kiện rạp, khu vực, giờ cụ thể và tìm phim theo tên chưa được hỗ trợ trong chat.",
              List.of(), List.of());
        case MOVIES:
        case MORE_MOVIES:
          request.setTopic(ReqAssistantChatDto.Topic.MOVIES);
          var preferences = new ReqMovieRecommendationDto();
          preferences.setGenre(intent.genre());
          preferences.setMaxDuration(intent.maxDuration());
          preferences.setDate(intent.date());
          preferences.setBudget(intent.budget());
          request.setPreferences(preferences);
          break;
        case DISCOUNTS:
          request.setTopic(ReqAssistantChatDto.Topic.DISCOUNTS);
          break;
        case MOVIE_DETAILS:
          request.setTopic(ReqAssistantChatDto.Topic.MOVIE_DETAILS);
          break;
        case SEATS:
          request.setTopic(ReqAssistantChatDto.Topic.SEATS);
          if (request.getSeats() != null) {
            if (intent.people() == null)
              return new ResAssistantChatDto("Bạn muốn chọn ghế cho bao nhiêu người?", List.of(), List.of());
            request.getSeats().setPeople(intent.people());
            request.getSeats().setBudget(intent.budget());
          }
          break;
      }
    }
    if (!request.isUseAi()) {
      var prefs = request.getPreferences();
      var selected = request.getSeats();
      request.setMemory(new AiIntentDto(AiIntentDto.Intent.valueOf(request.getTopic().name()),
          request.getTopic() == ReqAssistantChatDto.Topic.MOVIES && prefs != null ? prefs.getGenre() : null,
          request.getTopic() == ReqAssistantChatDto.Topic.MOVIES && prefs != null ? prefs.getMaxDuration() : null,
          request.getTopic() == ReqAssistantChatDto.Topic.MOVIES && prefs != null ? prefs.getDate() : null,
          request.getTopic() == ReqAssistantChatDto.Topic.SEATS && selected != null ? selected.getPeople() : null,
          request.getTopic() == ReqAssistantChatDto.Topic.SEATS && selected != null ? selected.getBudget()
              : request.getTopic() == ReqAssistantChatDto.Topic.MOVIES && prefs != null ? prefs.getBudget() : null));
    }
    switch (request.getTopic()) {
      case DISCOUNTS:
        String code = request.getMemory() == null ? null : request.getMemory().discountCode();
        var offers = discounts.list(false).stream()
            .filter(d -> code == null || code.isBlank() || d.getCode().equalsIgnoreCase(code.trim()))
            .map(ResAssistantDiscountDto::from).toList();
        return new ResAssistantChatDto(offers.isEmpty()
            ? (code == null || code.isBlank() ? "Hiện chưa có ưu đãi còn hiệu lực."
                : "Mã " + code + " không tồn tại hoặc hiện không còn hiệu lực.")
            : "Đây là ưu đãi còn hiệu lực. Nhập mã ở bước thanh toán; mức giảm được tính theo đơn hàng và điều kiện của mã.",
            List.of(), List.of(), request.getMemory(), offers);
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
            + "\nGiá phim: "
            + (f.getPrice() == null ? "Chưa cập nhật"
                : String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0fđ", f.getPrice().doubleValue())
                    + "; giá vé cộng thêm giá loại ghế, chưa áp dụng ưu đãi.")
            + "\nNgôn ngữ: " + (f.getLanguage() == null ? "Chưa cập nhật" : f.getLanguage());
        return new ResAssistantChatDto(reply, List.of(new ResMovieRecommendationDto(f.getId(), f.getName(),
            f.getThumbnail(), f.getDuration(), f.getGenre(), "Xem nội dung và suất chiếu trên trang phim.")),
            List.of());
      default:
        var prefs = request.getPreferences() == null ? new ReqMovieRecommendationDto() : request.getPreferences();
        var result = timed("movie-query",
            () -> request.getMemory() != null && request.getMemory().intent() == AiIntentDto.Intent.MORE_MOVIES
                ? movies.recommend(prefs, Set.copyOf(request.getSeenFilmIds()))
                : movies.recommend(prefs));
        return new ResAssistantChatDto(
            result.isEmpty()
                ? "Chưa có phim có suất chiếu phù hợp với bộ lọc. Bạn thử đổi ngày, thể loại, thời lượng hoặc ngân sách nhé."
                : "Các phim dưới đây có suất chiếu phù hợp với bộ lọc của bạn. Chọn phim để xem chi tiết và đặt vé.",
            result, List.of());
    }
  }
}
