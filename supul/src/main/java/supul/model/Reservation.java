package supul.model;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.DateFormatter;

import org.apache.ibatis.type.Alias;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
@Alias("rvDTO")
@Entity
@Table(name="reservation")
@Data
public class Reservation {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="rv_id")
	    int rvId;
	    String rvNum;
	    @Column(name="user_name")
	    String userName;
	    @Column(name="thema_name")
	    String themaName;
	     
	    LocalTime time;
	    
	    LocalDate date;
	    
	    @Column(name="rv_people")
	    int rvPeople;
	    @Column(name="rv_price")
	    int rvPrice;    
	    int price;    
	    
	    @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "user_id")
	    User user;
	    
	    @Column(name = "rv_date")
	    LocalDateTime rvDate; 
	    
	    @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "branch_id")
	    Branch branch;
	    
	    @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "thema_id")
	    Thema thema;
	   

	    
	    
	    // 결제 상태 추가
	    @Column(nullable = false, columnDefinition = "boolean default false")
	    boolean paid;

	    // 노쇼 상태 추가
	    @Column(nullable = false, columnDefinition = "boolean default false")
	    boolean noShow;
	    
	    //취소
	    @Column(nullable = false, columnDefinition = "boolean default false")
	    boolean cancle;
	    @Column(nullable = false, columnDefinition = "boolean default false")
	    boolean payCancle;
	    
	    
	    // 리뷰  상태 추가(true가 작성)
	    @Column(nullable = false, columnDefinition = "boolean default false")
	    boolean reviewYN;
	    
	    @OneToOne(mappedBy = "reservation",fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REMOVE})
	    Pay pay;
	    
	  
	    @OneToOne(mappedBy = "reservation",fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REMOVE})
	    RvPay rvpay;
	        
	     
	    
	    
	    
	    
	    
	    
	    public String getReserChkStr() {

	    	String res = date+"_"+time+"_"+thema.getThemaId();
	    	
	    	return res;
	    	
	    	 
	    }
	    
	    
	    //통계 
	    public static Map<String, Integer> MonthlyStats(List<Reservation> rv) {
	        Map<String, Integer> monthlyStats = new HashMap<>();
	        
	        for (Reservation reservation : rv) {
	            LocalDateTime rvDate = reservation.getRvDate();
	            String month = DateTimeFormatter.ofPattern("yyyy-MM").format(rvDate);
	            int price = reservation.getPrice();
	            
	            monthlyStats.put(month, monthlyStats.getOrDefault(month, 0) + price);
	        }
	        
	        return monthlyStats;
	    }
	    
	    public static Map<String, Integer> ReservationCount(List<Reservation> reservations) {
	        Map<String, Integer> reservationCount = new HashMap<>();
	        
	        for (Reservation reservation : reservations) {
	            String themaName = reservation.getThemaName();
	            reservationCount.put(themaName, reservationCount.getOrDefault(themaName, 0) + 1);
	        }
	        
	        return reservationCount;
	    }
	   
	    public static int NoShowCount(List<Reservation> reservations) {
	        int noShowCount = 0;
	        
	        for (Reservation reservation : reservations) {
	            if (reservation.isNoShow()) {
	                noShowCount++;
	            }
	        }
	        
	        return noShowCount;
	    }
	    
	    
	    
		@Override
		public String toString() {
			return "Reservation [rvId=" + rvId + ", price=" + price + ", rvDate=" + rvDate + "]";
		}
		
	     
	    
}
