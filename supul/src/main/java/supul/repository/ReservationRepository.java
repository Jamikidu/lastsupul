package supul.repository;

import java.math.BigDecimal;


import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import supul.model.Branch;
import supul.model.Reservation;
import supul.model.Thema;
import supul.model.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

	boolean existsByUserId(int userId);

	// 검색 및 페이징처리
	Page<Reservation> findByUserNameContaining(String userName, Pageable pageable);

	Page<Reservation> findByUserNameContainingAndNoShow(String userName, Boolean NOShow, Pageable pageable);

	Page<Reservation> findByBranch(Branch branch, Pageable pageable);

	Page<Reservation> findByBranchAndNoShow(Branch branch, Boolean NOShow, Pageable pageable);

	Page<Reservation> findByThemaNameContaining(String thema, Pageable pageable);

	Page<Reservation> findByBranchAndUserNameContaining(Branch branch, String useraName, Pageable pageable);

	Page<Reservation> findByBranchAndNoShowAndUserNameContaining(Branch branch, Boolean NOShow, String useraName,
			Pageable pageable);

	List<Reservation> findByUserNameContaining(String themaName);

	Page<Reservation> findByDate(LocalDate today, Pageable pageable);

	List<Reservation> findByDate(LocalDate today);
	List<Reservation> findByCancle(Boolean cancle);

	Page<Reservation> findByNoShow(Boolean NOShow, Pageable pageable);

	Reservation findByRvId(int rvId);

	Reservation findByRvNum(String rvNum);

	List<Reservation> findByUser(User user);

	// 현재 날짜포함하고 이후의 예약 데이터를 조회하는 메서드
	@Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.date >= :nowDate ORDER BY r.date ASC")
	List<Reservation> findAllUpcomingReservationsForUser(int userId, LocalDate nowDate);

	// 현재 날짜포함하고 이후의 예약 데이터를 조회하는 메서드
	@Query("SELECT r FROM Reservation r WHERE r.date >= :nowDate ORDER BY r.date ASC")
	List<Reservation> findTodaydate(LocalDate nowDate);

	// 그냥 id로 모든예약 찾기
	List<Reservation> findAllByUserId(int userId);

	@Query("SELECT s.themaName ,SUM(s.price),s.rvDate FROM Reservation s WHERE s.date BETWEEN :startDate AND :endDate GROUP BY s.themaName,s.rvDate")
	List<Object[]> Total(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT s.branch.name, s.themaName ,SUM(s.price) FROM Reservation s WHERE s.date BETWEEN :startDate AND :endDate GROUP BY s.branch.name, s.themaName")
	List<Object[]> themaTotal(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT s.branch.name ,SUM(s.price) FROM Reservation s WHERE s.date BETWEEN :startDate AND :endDate GROUP BY s.branch.name")
	List<Object[]> branchTotal(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT SUM(s.price) FROM Reservation s WHERE s.date BETWEEN :startDate AND :endDate")
	BigDecimal TotalSales(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	// 테마별 총 금액
	@Query("SELECT s.thema, SUM(s.rvPrice) FROM Reservation s ")
	List<Object[]> getThema_total();

	// 지점 총 금액
	@Query("SELECT s.branch.name, SUM(s.rvPrice) FROM Reservation s GROUP BY s.branch.name")
	List<Object[]> getBranch_total();

	// 테마별 총금액, 가장 큰 금액쓴 사람 리스트
	@Query("SELECT s.themaName, SUM(s.price), MAX(s.userName)FROM Reservation s GROUP BY s.themaName")
	List<Object[]> thema_price();

	@Query("SELECT s.themaName, SUM(s.price), MAX(s.userName)FROM Reservation s WHERE s.date BETWEEN :startDate AND :endDate GROUP BY s.themaName")
	List<Object[]> thema_priceDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	// 날짜별 총금액 리스트
	@Query("SELECT s.date, SUM(s.price) FROM Reservation s GROUP BY s.date")
	List<Object[]> date_price();

	// 지점별 총금액 리스트
	@Query("SELECT b.name , SUM(s.price) " + "FROM Branch b " + "LEFT JOIN b.tm.sale s " + "GROUP BY b.name")
	List<Object[]> branchListTotal();

	// 지정한 지점 총금액 리스트
	@Query("SELECT b.name, SUM(s.price)FROM Branch b LEFT JOIN b.tm.sale s "
			+ "WHERE b.name = :branchName GROUP BY b.name")
	Object[] branchTotal(@Param("branchName") String branchName);
	// 지정한 지점 테마별 총금액

	@Query("SELECT b.name , t.title , SUM(r.price)" + "FROM Branch b " + "JOIN b.tm t " + "LEFT JOIN t.sale r "
			+ "WHERE b.name = :branchName " + "GROUP BY b.name, t.title " + "ORDER BY b.name, t.title")
	List<Object[]> branchThemaTotal(@Param("branchName") String branchName);

	List<Reservation> findByThemaThemaId(int themaId);

	List<Reservation> findAllByBranch(Branch branch);

}
