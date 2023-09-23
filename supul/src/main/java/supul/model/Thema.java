package supul.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.ibatis.type.Alias;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import supul.model.board.ReviewBoard;

@Alias("themeDTO")
@Entity
@Table(name = "thema")
@Data
public class Thema {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "thema_id")
	int themaId;
	String title;
	String type;
	@Column(length = 9999)
	String content;
	int people;
	String poster;
	int horror;
	int activity;
	int playTime;
	int level;
	int price;
	@Column(name = "rv_price")
	int rvPrice;

	@ElementCollection
	@CollectionTable(name = "timetable_times", joinColumns = @JoinColumn(name = "timetable_id"))
	@Column(name = "time_slot")
	List<LocalTime> timetable;

	@Transient
	MultipartFile mmff;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "branch_id")
	Branch branch;

	@OneToMany(mappedBy = "thema", cascade = CascadeType.ALL)
	List<Reservation> sale;

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "thema")
	List<WishList> wishList;

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE }, mappedBy = "thema")
	List<ReviewBoard> review;

	@OneToMany(mappedBy = "thema")
	List<Ranking> ranking;



	public String getPoster() {
		if (poster == null || poster.trim().equals("") || poster.trim().equals("null")) {
			poster = null;
		}
		return poster;
	}

	public boolean isImg() {
		if (getPoster() == null) {
			return false;
		}
		return Pattern.matches(".{1,}[.](bmp|png|gif|jpg|jpeg)", poster.toLowerCase());
	}

	public ArrayList<String> getReserChkList() {

		ArrayList<String> res = new ArrayList<>();

		for (Reservation rs : sale) {
			res.add(rs.getReserChkStr());
		}

		return res;
	}

	public ArrayList<String> getWChkStrList() {

		ArrayList<String> res = new ArrayList<>();
		for (WishList ws : wishList) {
			res.add(ws.getWChkStr());
		}

		return res;

	}

	// 시간계산
	public boolean availabletime(LocalTime lt, LocalDate date) { // 예약시간과 현재시간과 차이 계산식

		LocalDate nowtime = LocalDate.now();

		if (nowtime.equals(date)) {// 달력이 현재날짜일때만 계산
			// 현재 시간과 예약 시간 사이의 차이(분 단위)를 계산합니다.
			long remaintime = ChronoUnit.MINUTES.between(LocalTime.now(), lt);

			// 예약 가능한지 확인합니다 (예: 현재 시간으로부터 30분 이상 남은 경우)
			return remaintime >= 30;
		} else {
			return true;
		}
	}

	public boolean time(LocalDate date) { // 예약시간과 현재시간과 차이 계산식

		if (getSale().isEmpty()) {
			for (LocalTime b : getTimetable()) {
				return true;
			}

		}

		for (Reservation c : getSale()) {
			for (LocalTime b : getTimetable()) {

				if (b.equals(c.getTime()) && c.getDate().equals(date)) {

					return false;
				}

			}

		}
		return false;

	}

	@Override
	public String toString() {
		return "Thema [themaId=" + themaId + ", title=" + title;
	}

}
