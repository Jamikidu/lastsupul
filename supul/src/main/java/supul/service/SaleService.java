package supul.service;

import java.math.BigDecimal;



import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import supul.mapper.ThemaMapper;
import supul.model.Branch;
import supul.model.Reservation;
import supul.model.Thema;
import supul.model.User;
import supul.repository.BranchRepository;
import supul.repository.ReservationRepository;
import supul.repository.ThemaRepository;
import supul.repository.UserRepository;


 
@Service
public class SaleService {

	@Resource
	private ReservationRepository sales;
	
	@Resource
	private BranchRepository branch;
	@Resource
	ThemaRepository thema;
	@Autowired
	ThemaMapper themaMapper;
	@Autowired
	UserRepository userRepository;
	
	
	 public Page<User> searchUser(String userName,Pageable pageable) {
	        return userRepository.findByUserNameContaining(userName, pageable);
	    }
	
	
	 
	
	  public List<LocalTime> getTimetableByThemaId(int themaId) {
	        return themaMapper.timetableList(themaId);
	    }
	  ArrayList<String> chkrvstatus(LocalDate date, int thema_id ){
		  
		  return themaMapper.chkrvstatus(date);
	  };
	
	//노쇼 처리
	 public void updateNoShowStatus(int rvId, boolean noShow) {
	        Reservation reservation = sales.findById(rvId).orElse(null);
	        if (reservation != null) {
	            reservation.setNoShow(noShow);
	            sales.save(reservation);
	           
	        }
	    } 
	 
	//결제처리
	 public void updatePaidStatus(int rvId, boolean paid) {
	        Reservation reservation = sales.findById(rvId).orElse(null);
	        if (reservation != null) {
	            reservation.setPaid(paid);
	            sales.save(reservation);
	        }
	    }
	
	 public Reservation pay(int rvId) {
		 return sales.findByRvId(rvId);
	 }
	 
	//예약 리스트, 검색리스트
	
	 public Page<Reservation> searchTm(Pageable pageable) {
	        return sales.findAll(pageable);
	    }
	 public Page<Reservation> searchbr(Branch branch,Pageable pageable) {
	        return sales.findByBranch(branch,pageable);
	    }
	  @Transactional
	  public List<Reservation> searchTm1(String User) {
	        return sales.findByUserNameContaining(User);
	    }

	  public Page<Thema> themaList(Pageable pageable) {
			return thema.findAll(pageable);
		} 
	  
	  //브런치리스트, 검색리스트
	  public Page<Branch> brList(Pageable pageable) {
			return branch.findAll(pageable);
		}  
	  public Page<Branch> brSertchList(String name, Pageable pageable) {
			return branch.findByNameContaining(name, pageable);
		}
	  public List<Branch> brSertchList(String name) {
	        return  branch.findByNameContaining(name);
	    }
	  
	public Page<Reservation> paging(Pageable pageable) {
		return sales.findAll(pageable);
	}

	public BigDecimal totalSales(LocalDate startDate, LocalDate endDate) {
		return sales.TotalSales(startDate, endDate);
	}

	public List<Object[]> thema_price(LocalDate startDate, LocalDate endDate) {
		return sales.thema_priceDate(startDate, endDate);
	}
 
	public Object[] branch_Total(String branchName) {
		return sales.branchTotal(branchName);
	}

	public List<Object[]> branchThemaTotal(String branchName) {
		return sales.branchThemaTotal(branchName);
	}

	public List<Object[]> total(LocalDate startDate, LocalDate endDate) {
		return sales.Total(startDate, endDate);
	}
	public List<Object[]> themaTotal(LocalDate startDate, LocalDate endDate) {
		return sales.themaTotal(startDate, endDate);
	}
	public List<Object[]> branchTotal(LocalDate startDate, LocalDate endDate) {
		return sales.branchTotal(startDate, endDate);
	}
}
