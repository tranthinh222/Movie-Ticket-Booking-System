package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.util.constant.FilmStatusEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Order(5)
public class FilmSeeder implements CommandLineRunner {
        private final FilmRepository filmRepository;

        public FilmSeeder(FilmRepository filmRepository) {
                this.filmRepository = filmRepository;
        }

        @Override
        public void run(String... args) {
                if (filmRepository.count() == 0) {
                        Film film1 = Film.builder()
                                        .name("AVATAR 3: LỬA VÀ TRO TÀN")
                                        .director("James Cameron")
                                        .actors("Giovanni Ribisi, Kate Winslet, Zoe Saldaña.")
                                        .duration(148L)
                                        .price(75000L)
                                        .description("Avatar: Lửa Và Tro Tàn lấy bối cảnh một năm sau khi gia đình Sully định cư tại bộ tộc Metkayina. Jake (Sam Worthington) và Neytiri (Zoe Saldaña) cùng các thành viên đang phải vật lộn với nỗi đau sau cái chết của Neteyam (Jamie Flatters). Tuy nhiên, thời gian đau buồn không kéo dài lâu khi Đại tá Quaritch (Stephen Lang) vẫn sống sót và chuẩn bị một cuộc tấn công quy mô lớn khác. Mối thù cá nhân giờ đây bùng nổ thành cuộc chiến định đoạt vận mệnh cả hành tinh, khi Quaritch liên minh với Tộc Tro (Mangkwan) - bộ tộc Na'vi hung hãn đại diện cho mặt tối của Pandora dưới sự dẫn dắt của nữ thủ lĩnh Varang đầy thù hận.")
                                        .genre("Hành Động, Khoa Học Viễn Tưởng, Phiêu Lưu, Thần thoại")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 19))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285819/uploads/qwjrkasfgpqscd1mbmri.jpg")
                                        .build();

                        Film film2 = Film.builder()
                                        .name("THIÊN ĐƯỜNG MÁU")
                                        .director("Hoàng Tuấn Cường")
                                        .actors("Quang Tuấn, Hoài Lâm, Quách Ngọc Ngoan, Sỹ Toàn, Thanh Hương, Bích Ngọc, Lê Minh Thuấn, NSƯT Hạnh Thuý, Hoàng Yến, Lâm Thanh Sơn, Đình Hiếu, Hoàng Trinh...")
                                        .duration(113L)
                                        .price(60000L)
                                        .description("Thiên Đường Máu là phim điện ảnh đầu tiên về nạn lừa đảo người Việt ra nước ngoài. Tin lời hứa \"việc nhẹ lương cao\", không ít thanh niên bị đưa đến những \"đặc khu\", nơi họ trải qua cảnh giam lỏng và bị ép buộc phải gọi điện để lừa ngược lại chính đồng bào mình. Nhiều người trong số đó đã tìm cách đào thoát khỏi địa ngục mà họ đã trót dấn thân vào.")
                                        .genre("Hành Động, Tâm Lý")
                                        .language("Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 31))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285823/uploads/zhuktsvahnrakvbshizm.jpg")
                                        .build();

                        Film film3 = Film.builder()
                                        .name("LUPIN ĐỆ TAM: LÂU ĐÀI CAGLIOSTRO")
                                        .director("Miyazaki Hayao")
                                        .actors("Yasuo Yamada, Eiko Masuyama, Kiyoshi Kobayashi")
                                        .duration(100L)
                                        .price(54000L)
                                        .description("Trong hành trình đến Công quốc Cagliostro, siêu trộm Lupin III tình cờ cứu một cô dâu xinh đẹp tên Clarisse, người đang bị Bá tước Cagliostro ép cưới để chiếm đoạt quyền lực. Khi tìm cách giải cứu Clarice, Lupin phát hiện bí mật đen tối — một nhà máy sản xuất tiền giả tồn tại suốt 400 năm. Cùng với đồng đội và cả Thanh tra Zenigata, anh quyết tâm lật đổ âm mưu của Bá tước. Cuộc đối đầu đỉnh điểm diễn ra trong lễ cưới giả mạo, nơi bí mật hoàng tộc và sự thật quá khứ được phơi bày.")
                                        .genre("Gia đình, Hoạt Hình, Phiêu Lưu")
                                        .language("Tiếng Nhật - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 26))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285828/uploads/mykdgxrfvy6yl4lojcjr.jpg")
                                        .build();

                        Film film4 = Film.builder()
                                        .name("PHI VỤ ĐỘNG TRỜI 2")
                                        .director("Jared Bush, Byron Howard")
                                        .actors("Jason Bateman, Quinta Brunson, Fortune Feimster")
                                        .duration(107L)
                                        .price(98000L)
                                        .description("Trong bộ phim \"Zootopia 2 - Phi Vụ Động Trời 2\" từ Walt Disney Animation Studios, hai thám tử Judy Hopps (lồng tiếng bởi Ginnifer Goodwin) và Nick Wilde (lồng tiếng bởi Jason Bateman) bước vào hành trình truy tìm một sinh vật bò sát bí ẩn vừa xuất hiện tại Zootopia và khiến cả vương quốc động vật bị đảo lộn. Để phá được vụ án, Judy và Nick buộc phải hoạt động bí mật tại những khu vực mới lạ của thành phố – nơi mối quan hệ đồng nghiệp của họ bị thử thách hơn bao giờ hết.")
                                        .genre("Gia Đình, Hành Động, Phiêu Lưu, Thần Thoại")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt; Lồng tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 11, 28))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285834/uploads/abblpoveapwlywbvcsc6.jpg")
                                        .build();

                        Film film5 = Film.builder()
                                        .name("TOM & JERRY: CHIẾC LA BÀN KỲ BÍ")
                                        .director("Gang Zhang")
                                        .actors("Eric Bauza, Ben Diskin, Janice Kawaye")
                                        .duration(98L)
                                        .price(82000L)
                                        .description("ĐẦU NĂM CƯỜI ĐÃ - TOM & JERRY ĐẠI NÁO RẠP VIỆT Một chiếc la bàn bí ẩn bất ngờ mở ra cánh cổng kỳ diệu - nơi đầy ắp thử thách, tiếng cười và những màn rượt đuổi “kinh điển” cộp mác Tom & Jerry. Để trở về nhà, cặp đôi oan gia buộc phải hợp tác trước khi chiếc la bàn phá vỡ trật tự của mọi thế giới. Một chuyến phiêu lưu mở vận may, mở tiếng cười, khởi đầu năm mới thật tưng bừng cho cả gia đình.")
                                        .genre("Gia Đình, Hài, Hoạt Hình, Phiêu Lưu, Thần Thoại")
                                        .language("Lồng tiếng Việt, phụ đề tiếng Anh")
                                        .releaseDate(LocalDate.of(2026, 1, 1))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285845/uploads/hys4gqydisyfoopavmwx.jpg")
                                        .build();

                        Film film6 = Film.builder()
                                        .name("BIỆT ĐỘI YOYO: GIẢI CỨU GIÁNG SINH")
                                        .director("Damjan Mitrevski, Ricard Cussó")
                                        .actors("")
                                        .duration(96L)
                                        .price(62000L)
                                        .description("Ngay ngày đầu đi làm, Yoyo bàng hoàng nhận ra Bắc Cực không còn ánh sáng phép màu, mà bị thay thế bởi robot vô cảm và những dây chuyền lạnh lẽo. Khi một hacker bí ẩn chiếm quyền điều khiển xưởng quà, toàn bộ Giáng Sinh đứng trước nguy cơ bị xóa sổ, và Yoyo trở thành hy vọng cuối cùng. Cùng biệt đội của mình, cậu lao vào hành trình tìm lại ông già Noel, vượt qua bão tuyết, những cuộc đua kỳ lạ cũng như phải đối mặt với quá khứ đau lòng. Từ nhà xưởng hiện đại đến rừng băng kỳ ảo, mỗi bước đi là một thử thách, mỗi sai lầm là một bài học. Và chính trong hành trình ấy, Yoyo khám phá ra rằng phép màu thật sự luôn bắt đầu từ chính niềm tin bên trong mỗi con người.")
                                        .genre("Gia đình, Hoạt Hình, Phiêu Lưu")
                                        .language("Lồng tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 24))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285852/uploads/embijf6l75swietcfl3y.jpg")
                                        .build();

                        Film film7 = Film.builder()
                                        .name("SPONGEBOB: LỜI NGUYỀN HẢI TẶC")
                                        .director("Derek Drymon")
                                        .actors("Tom Kenny, Clancy Brown, Rodger Bumpass, Bill Fagerbakke, Carolyn Lawrence, Mr. Lawrence, George Lopez, Isis “Ice Spice” Gaston, Arturo Castro, Sherry Cola with Regina Hall and Mark Hamill")
                                        .duration(96L)
                                        .price(78000L)
                                        .description("SpongeBob phiêu lưu xuống đáy đại dương để đối mặt với hồn ma của Người Hà Lan bay, vượt qua thử thách và khám phá những bí ẩn dưới biển.")
                                        .genre("Hài, Hoạt Hình, Phiêu Lưu")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 26))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285856/uploads/unxacvupaq98umkvsm02.jpg")
                                        .build();

                        Film film8 = Film.builder()
                                        .name("NHÀ HAI CHỦ")
                                        .director("Trần Duy Linh & Phạm Trung Hiếu")
                                        .actors("Trâm Anh, Kim Hải, Tạ Lâm, Kim Phương ,Mộc Trà,…")
                                        .duration(102L)
                                        .price(58000L)
                                        .description("Một gia đình nhỏ vì không đủ điều kiện đã phải mua một căn nhà mà người dân xung quanh đồn đoán rằng có nhiều điều kỳ lạ tâm linh liệu gia đình sẽ đối mặt với ngôi nhà nhiều chủ sẽ như thế nào?")
                                        .genre("Kinh Dị")
                                        .language("Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 26))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285859/uploads/iuymggbynknyrwrcrcxq.jpg")
                                        .build();

                        Film film9 = Film.builder()
                                        .name("HÀNG XÓM CỦA TÔI TOTORO")
                                        .director("Miyazaki Hayao")
                                        .actors("Hitoshi Takagi, Noriko Hidaka, Chika Sakamoto,...")
                                        .duration(87L)
                                        .price(76000L)
                                        .description("Hai chị em Satsuki và Mei cùng cha chuyển về sống tại một vùng ngoại ô xanh mát. Họ tình cờ gặp gỡ sinh vật huyền bí mang tên Totoro, và từ đó bắt đầu những trải nghiệm kỳ diệu. Nhưng một ngày, Khi Mei mang bắp đến bệnh viện thăm mẹ nhưng bị lạc trên đường, Satsuki lo lắng phải tìm đến Totoro để nhờ giúp đỡ. Ngay lập tức, một chú mèo khổng lồ có 12 chân với thân hình như chiếc xe buýt xuất hiện. Đôi mắt nó sáng rực như đèn pha, lao nhanh qua những đường dây điện và khu rừng rậm, chạy như gió để tìm đến Mei.")
                                        .genre("Hoạt Hình, Phiêu Lưu, Thần Thoại")
                                        .language("Tiếng Nhật - Lồng tiếng/Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 19))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285863/uploads/fdjjiov1qst3p5vv4jzr.jpg")
                                        .build();

                        Film film10 = Film.builder()
                                        .name("5 CENTIMET TRÊN GIÂY")
                                        .director("Shinkai Makoto")
                                        .actors("")
                                        .duration(76L)
                                        .price(58000L)
                                        .description("Câu chuyện cảm động về Takaki và Akari, đôi bạn thuở thiếu thời dần bị chia cắt bởi thời gian và khoảng cách. Qua ba giai đoạn khác nhau trong cuộc đời, hành trình khắc họa những ký ức, cuộc hội ngộ và sự xa cách của cặp đôi, với hình ảnh hoa anh đào rơi – 5cm/giây – như ẩn dụ cho tình yêu mong manh và thoáng chốc của tuổi trẻ.")
                                        .genre("Hoạt Hình")
                                        .language("Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 5))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285865/uploads/if0ajvpug9fjiymxpwod.jpg")
                                        .build();

                        Film film11 = Film.builder()
                                        .name("MA TRẬN: HỒI SINH")
                                        .director("Lana Wachowski")
                                        .actors("Keanu Reeves, Carrie-Anne Moss, Yahya Abdul-Mateen II")
                                        .duration(148L)
                                        .price(75000L)
                                        .description("Bị ám ảnh bởi những ký ức rời rạc trong một thế giới ngỡ như bình thường, Thomas Anderson buộc phải lựa chọn đi theo thỏ trắng một lần nữa. Để tìm ra sự thật về thực tại và ảo ảnh, Neo phải dấn thân sâu vào Ma Trận - nơi giờ đây đã trở nên tinh vi, nguy hiểm hơn bao giờ hết, và đối mặt với một kẻ thù mới đang đe dọa tự do của cả nhân loại lẫn máy móc.")
                                        .genre("Hành Động, Khoa Học Viễn Tưởng")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 20))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285869/uploads/hfzft3guuejyip3p1ohd.jpg")
                                        .build();

                        Film film12 = Film.builder()
                                        .name("FAST & FURIOUS 11")
                                        .director("Louis Leterrier")
                                        .actors("Vin Diesel, Michelle Rodriguez, Jason Momoa")
                                        .duration(145L)
                                        .price(70000L)
                                        .description("Chặng đường cuối cùng của gia đình Dominic Toretto chính thức bắt đầu với những thử thách khốc liệt chưa từng có. Bóng ma từ quá khứ trỗi dậy với dã tâm xóa sổ mọi thứ Dom trân trọng, buộc anh phải thực hiện một cuộc đua sinh tử xuyên lục địa.")
                                        .genre("Hành Động, Tội Phạm, Phiêu Lưu")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 2, 14))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285871/uploads/cbg726nkalpp6l4585qj.jpg")
                                        .build();

                        Film film13 = Film.builder()
                                        .name("THE MARVELS")
                                        .director("Nia DaCosta")
                                        .actors("Brie Larson, Teyonah Parris, Iman Vellani")
                                        .duration(130L)
                                        .price(72000L)
                                        .description("Một sự cố vũ trụ kỳ bí khiến sức mạnh của Captain Marvel, Ms. Marvel và Monica Rambeau bị liên kết với nhau, khiến họ hoán đổi vị trí mỗi khi sử dụng năng lực. Bộ ba bất đắc dĩ này buộc phải học cách phối hợp ăn ý để cứu vũ trụ khỏi một đế chế Kree đang trỗi dậy.")
                                        .genre("Hành Động, Khoa Học Viễn Tưởng")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 1, 15))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285875/uploads/gxqwksol2mrmmicxndxl.jpg")
                                        .build();

                        Film film14 = Film.builder()
                                        .name("OPPENHEIMER")
                                        .director("Christopher Nolan")
                                        .actors("Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.")
                                        .duration(180L)
                                        .price(80000L)
                                        .description("Một thiên sử thi bi tráng đi sâu vào tâm trí của J. Robert Oppenheimer - cha đẻ của bom nguyên tử. Bộ phim khắc họa cuộc đua chế tạo vũ khí hủy diệt trong Thế chiến II và nỗi dằn vặt đạo đức khủng khiếp của một thiên tài khi nhận ra mình đã trao cho nhân loại chìa khóa để tự hủy diệt chính mình.")
                                        .genre("Tiểu Sử, Chính Kịch, Lịch Sử")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 1, 1))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285882/uploads/rbs4igzzw3mtxsl9atk3.jpg")
                                        .build();

                        Film film15 = Film.builder()
                                        .name("VỆ BINH DẢI NGÂN HÀ 3")
                                        .director("James Gunn")
                                        .actors("Chris Pratt, Zoe Saldana, Dave Bautista, Bradley Cooper")
                                        .duration(150L)
                                        .price(73000L)
                                        .description("Nhóm Vệ Binh dải ngân hà phải tập hợp lần cuối trong một nhiệm vụ đau lòng để bảo vệ Rocket khỏi bóng ma quá khứ đen tối liên quan đến High Evolutionary. Peter Quill cùng đồng đội phải đối mặt với nguy cơ tan rã vĩnh viễn trong một hành trình đầy nước mắt, tiếng cười và những lời chia tay không thể tránh khỏi.")
                                        .genre("Hành Động, Phiêu Lưu, Hài Hước")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 1, 10))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285885/uploads/fquy2qlhhdwpbb6jiiup.jpg")
                                        .build();

                        Film film16 = Film.builder()
                                        .name("TRANSFORMERS: QUÁI THÚ TRỖI DẬY")
                                        .director("Steven Caple Jr.")
                                        .actors("Anthony Ramos, Dominique Fishback, Peter Cullen")
                                        .duration(145L)
                                        .price(71000L)
                                        .description("Cuộc chiến không còn gói gọn giữa Autobots và Decepticons. Một thế lực cổ đại tàn bạo là Unicron đang đến gần, đe dọa nuốt chửng Trái Đất. Các Autobots buộc phải liên minh với Maximals - những robot biến hình thành thú - để tham gia vào một cuộc chiến toàn cầu nhằm bảo vệ hành tinh khỏi sự diệt vong.")
                                        .genre("Hành Động, Khoa Học Viễn Tưởng")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 2, 5))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285891/uploads/dhljxwuwfyyv3qmvekow.jpg")
                                        .build();

                        Film film17 = Film.builder()
                                        .name("DUNE: HÀNH TINH CÁT - PHẦN HAI")
                                        .director("Denis Villeneuve")
                                        .actors("Timothee Chalamet, Zendaya, Rebecca Ferguson")
                                        .duration(160L)
                                        .price(78000L)
                                        .description("Paul Atreides chính thức hợp nhất với người Fremen và Chani trên con đường báo thù những kẻ đã hủy hoại gia tộc mình. Đứng trước ngã rẽ định mệnh, Paul phải đấu tranh giữa tình yêu và trách nhiệm của 'Đấng Cứu Thế' để ngăn chặn một tương lai tàn khốc mà chỉ mình anh có thể nhìn thấy, mở ra cuộc thánh chiến rung chuyển cả vũ trụ.")
                                        .genre("Khoa Học Viễn Tưởng, Phiêu Lưu, Hành Động")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 2, 28))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285894/uploads/u87bywyutih2avmlolyf.jpg")
                                        .build();

                        Film film18 = Film.builder()
                                        .name("THẾ GIỚI KHỦNG LONG: LÃNH ĐỊA")
                                        .director("Colin Trevorrow")
                                        .actors("Chris Pratt, Bryce Dallas Howard, Laura Dern")
                                        .duration(147L)
                                        .price(72000L)
                                        .description("Bốn năm sau sự kiện tại đảo Nublar, loài khủng long giờ đây sống và săn mồi ngay cạnh con người trên khắp thế giới. Sự cân bằng mong manh này sẽ định hình lại tương lai: Liệu con người có thể tiếp tục là kẻ thống trị, hay sẽ bị thay thế bởi những sinh vật đáng sợ nhất lịch sử trong một kỷ nguyên hỗn mang mới?")
                                        .genre("Hành Động, Phiêu Lưu, Khoa Học Viễn Tưởng")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 11, 20))
                                        .status(FilmStatusEnum.STOPPED)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285898/uploads/tb22qnegi3l7xgnoqy9c.jpg")
                                        .build();

                        Film film19 = Film.builder()
                                        .name("Nàng Tiên Cá")
                                        .director("Rob Marshall")
                                        .actors("Halle Bailey, Jonah Hauer-King, Melissa McCarthy")
                                        .duration(125L)
                                        .price(68000L)
                                        .description("Nàng tiên cá Ariel, con gái út của vua Triton, luôn khao khát khám phá thế giới bên kia mặt biển. Bất chấp sự ngăn cấm, cô đã đánh đổi giọng hát tuyệt đẹp của mình với phù thủy Ursula để lấy đôi chân con người. Hành trình tìm kiếm tình yêu đích thực và sự tự do của Ariel sẽ dẫn đến những hậu quả không ngờ cho cả hai thế giới.")
                                        .genre("Thần Thoại, Phim Ca Nhạc, Gia Đình")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 3, 8))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285901/uploads/xdvodcdjblixh4lftfip.jpg")
                                        .build();

                        Film film20 = Film.builder()
                                        .name("Nhiệm Vụ: Bất Khả Thi - Nghiệp Báo Cuối Cùng")
                                        .director("Christopher McQuarrie")
                                        .actors("Tom Cruise, Hayley Atwell, Ving Rhames, Simon Pegg")
                                        .duration(165L)
                                        .price(78000L)
                                        .description("Ethan Hunt và đội IMF bước vào trận chiến cuối cùng chống lại 'Thực Thể' - một trí tuệ nhân tạo có khả năng thao túng chân lý và định đoạt trật tự thế giới mới. Khi bóng ma từ quá khứ quay trở lại và cái giá của nhiệm vụ trở nên quá đắt, Ethan nhận ra rằng đây không chỉ là một nhiệm vụ bất khả thi, mà là một lời phán xét cuối cùng dành cho chính anh và những người anh yêu thương.")
                                        .genre("Hành Động, Phiêu Lưu, Gián Điệp")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 1, 30))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285903/uploads/wczeai8utfgcmy91bxph.webp")
                                        .build();

                        Film film21 = Film.builder()
                                        .name("AI THƯƠNG AI MẾN")
                                        .director("Thu Trang")
                                        .actors("Ngọc Thuận, Thu Trang, Trâm Anh, Võ Điền Gia Huy, Khả Như, La Thành, Trương Minh Thảo, Tiến Luật và một số diễn viên khác.")
                                        .duration(112L)
                                        .price(60000L)
                                        .description("Lấy bối cảnh miền Tây sông nước, Ai Thương Ai Mến xoay quanh Hai Mến — người phụ nữ mất cha mẹ, một mình gồng gánh gia đình giữa nợ nần và những bi kịch dồn dập. Trong hành trình ấy, cô gặp cậu Khả (Ngọc Thuận) – công tử ăn chơi vô tình đem lòng thương mến, tạo nên mối tình vừa ngọt ngào vừa đau đớn. Song song đó, chuyện tình thơm lành của đôi trẻ Chờ – Thương trở thành điểm sáng trước khi cả ba tuyến nhân vật bước vào những thử thách chạm đến trái tim khán giả.")
                                        .genre("Gia đình, Hài, Tâm Lý")
                                        .language("Tiếng Việt - Phụ đề Tiếng Anh")
                                        .releaseDate(LocalDate.of(2026, 1, 1))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285906/uploads/sj5sgqyuqllvgveqsyvk.png")
                                        .build();

                        Film film22 = Film.builder()
                                        .name("CHỌN CHỒNG NƠI CHÍN SUỐI")
                                        .director("David Freyne")
                                        .actors("Miles Teller; Elizabeth Olsen; Callum Turner")
                                        .duration(114L)
                                        .price(65000L)
                                        .description("Tại 'thế giới bên kia', nơi mỗi linh hồn phải lựa chọn 'người đồng hành vĩnh cửu', Joan rơi vào drama tình ái khó gỡ bậc nhất lịch sử suối vàng: Chọn người chồng đã gắn bó cả một đời, hay mối tình đầu đã chờ cô vài chục năm tại nơi đây?")
                                        .genre("Hài, Tình Cảm")
                                        .language("Tiếng Anh - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 26))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285908/uploads/x5l3c4h5xr8y8xa4apoo.jpg")
                                        .build();

                        Film film23 = Film.builder()
                                        .name("ĐẠI THOẠI TÂY DU PHẦN 2: TIÊN LÝ KỲ DUYÊN")
                                        .director("Jeffrey Lau")
                                        .actors("Stephen Chow, Athena Chu, Law Ka-ying, Karen Mok")
                                        .duration(100L)
                                        .price(68000L)
                                        .description("Phiên bản 'độc' nhất của Tây Du Ký. Câu chuyện khai mở quá khứ chấn động của Tề thiên Đại Thánh Tôn Ngộ Không. Áng tình lãng mạn và khốc liệt nhất lần đầu ra rạp Việt Nam sau 30 năm chinh phục khán giả toàn cầu.")
                                        .genre("Hài, Hành Động, Thần Thoại, Tình Cảm")
                                        .language("Tiếng Quảng Đông - Lồng tiếng Việt, phụ đề tiếng Anh")
                                        .releaseDate(LocalDate.of(2026, 1, 9))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285911/uploads/dwz2d5dmsfabs3u0rxaq.jpg")
                                        .build();

                        Film film24 = Film.builder()
                                        .name("ÁC LINH TRÙNG TANG")
                                        .director("Ivan Bandhito")
                                        .actors("Joshua Suherman, Egi Fedly")
                                        .duration(87L)
                                        .price(75000L)
                                        .description("Sau nhiều năm xa cách, bốn anh chị em trở về ngôi nhà cũ để tưởng niệm em út Sofi – cô bé chết thảm. Tuy nhiên khi cùng sống trong căn nhà thơ ấu, từng người bị ám ảnh bởi tội lỗi quá khứ. Sofi là người duy nhất muốn nói ra sự thật… và giờ linh hồn cô trở lại, buộc họ phải trả giá.")
                                        .genre("Kinh Dị")
                                        .language("Tiếng Indonesia, phụ đề tiếng Việt")
                                        .releaseDate(LocalDate.of(2026, 1, 9))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285914/uploads/ibdzemnawpx7hiwjemyk.jpg")
                                        .build();

                        Film film25 = Film.builder()
                                        .name("ARRIETTY")
                                        .director("Yonebayashi Hiromasa")
                                        .actors("Mirai Shida, Ryunosuke Kamiki, Tatsuya Fujiwara,...")
                                        .duration(94L)
                                        .price(50000L)
                                        .description("Arrietty, cô bé tí hon sống dưới sàn nhà của một ngôi nhà cũ ở ngoại ô Tokyo. Một đêm, cha cô đưa đi 'mượn' lần đầu tiên và bị cậu bé Sho phát hiện. Sự hiện diện của gia đình cô bị phát hiện, và họ buộc phải rời khỏi ngôi nhà dưới sàn.")
                                        .genre("Gia Đình, Hoạt Hình, Thần Thoại")
                                        .language("Tiếng Nhật - Phụ đề Tiếng Việt/Lồng Tiếng")
                                        .releaseDate(LocalDate.of(2026, 1, 9))
                                        .status(FilmStatusEnum.COMING_SOON)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285918/uploads/ma2kixd2xay7krrlifnq.jpg")
                                        .build();

                        Film film26 = Film.builder()
                                        .name("Nhà Bà Nữ")
                                        .director("Trấn Thành")
                                        .actors("Huỳnh Uyển Ân, Lê Giang, NSND Ngọc Giàu, Khả Như, Song Luân")
                                        .duration(102L)
                                        .price(65000L)
                                        .description("Phim xoay quanh gia đình của bà Nữ, người làm nghề bán bánh canh. Câu tagline chính 'Mỗi gia đình đều có những bí mật' chứa nhiều ẩn ý về nội dung bộ phim.")
                                        .genre("Gia Đình")
                                        .language("Tiếng Việt")
                                        .releaseDate(LocalDate.of(2023, 1, 22))
                                        .status(FilmStatusEnum.STOPPED)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285923/uploads/xtax0yh4jrcwqofacbac.jpg")
                                        .build();

                        Film film27 = Film.builder()
                                        .name("Lật Mặt 8: Vòng Tay Nắng")
                                        .director("Lý Hải")
                                        .actors("NSƯT Kim Phương, Long Đẹp Trai, NSƯT Tuyết Thu, Quách Ngọc Tuyên, Đoàn Thế Vinh")
                                        .duration(135L)
                                        .price(70000L)
                                        .description("Kể về sự khác biệt quan điểm giữa ba thế hệ ông bà cha mẹ con cháu. Ai cũng đúng ở góc nhìn của mình nhưng đứng trước hoài bão của tuổi trẻ, cuối cùng ai sẽ là người phải nghe theo người còn lại?")
                                        .genre("Gia Đình, Hài")
                                        .language("Tiếng Việt - Phụ đề Tiếng Anh")
                                        .releaseDate(LocalDate.of(2025, 4, 30))
                                        .status(FilmStatusEnum.STOPPED)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285926/uploads/viznuzxdhu6reoszfd8m.jpg")
                                        .build();

                        Film film28 = Film.builder()
                                        .name("HOÀNG TỬ QUỶ")
                                        .director("Trần Hữu Tấn")
                                        .actors("Anh Tú Atus, Lương Thế Thành, Hoàng Linh Chi, Huỳnh Thanh Trực, Rima Thanh Vy, Lê Hà Phương, Duy Luân,...")
                                        .duration(117L)
                                        .price(60000L)
                                        .description("Hoàng Tử Quỷ xoay quanh Thân Đức - một hoàng tử được sinh ra nhờ tà thuật. Thân Đức tham vọng giải thoát Quỷ Xương Cuồng và đụng độ trưởng làng Lỗ Đạt mạnh mẽ để đạt âm mưu của mình.")
                                        .genre("Kinh Dị")
                                        .language("Tiếng Việt - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 5))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285929/uploads/tzrqmljzw2n9vsu88xqr.webp")
                                        .build();

                        Film film29 = Film.builder()
                                        .name("CHỢ ĐEN THỜI TẬN THẾ")
                                        .director("HONG KI-WON")
                                        .actors("LEE JAE-IN, HONG KYUNG, JUNG MAN-SI, YU SU-BIN, KIM KUK-HEE, CHOI JUNG-UN")
                                        .duration(122L)
                                        .price(55000L)
                                        .description("Sau đại địa chấn xóa sổ thế giới, hy vọng mong manh len lỏi giữa tòa chung cư cuối cùng biến thành 'chợ đen'- nơi mạng đổi mạng và niềm tin là món đồ xa xỉ.")
                                        .genre("Hồi Hộp, Hành Động")
                                        .language("Tiếng Hàn - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 19))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285931/uploads/udh5rnyeax5mksygeskw.webp")
                                        .build();

                        Film film30 = Film.builder()
                                        .name("Cô Hầu Gái")
                                        .director("Paul Feig")
                                        .actors("Sydney Sweeney, Brandon Sklenar, Amanda Seyfried, ...")
                                        .duration(132L)
                                        .price(60000L)
                                        .description("Một thế giới hỗn loạn mở ra, nơi sự hoàn hảo chỉ là ảo giác. Millie trở thành bảo mẫu cho gia đình giàu có và sự thật dần được hé lộ.")
                                        .genre("Hồi Hộp")
                                        .language("Tiếng Mỹ - Phụ đề Tiếng Việt")
                                        .releaseDate(LocalDate.of(2025, 12, 26))
                                        .status(FilmStatusEnum.NOW_SHOWING)
                                        .thumbnail("https://res.cloudinary.com/djmcluh5n/image/upload/v1767285935/uploads/yaodlogtzj7fv8fwjvjs.webp")
                                        .build();

                        filmRepository.saveAll(List.of(film1, film2, film3, film4, film5, film6, film7, film8, film9,
                                        film10, film11, film12, film13, film14, film15, film16, film17, film18, film19,
                                        film20, film21, film22, film23, film24, film25, film26, film27, film28, film29,
                                        film30));

                        System.out.println("Seeded Films");
                } else {
                        System.out.println("Film already exist");
                }
        }
}
