package supul.controll;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import supul.mapper.BranchMapper;
import supul.mapper.ThemaMapper;
import supul.mapper.UserMapper;
import supul.model.Admin;
import supul.model.Branch;
import supul.model.ModiBranch;
import supul.model.PageData;
import supul.model.Pay;
import supul.model.Ranking;
import supul.model.Reservation;
import supul.model.Thema;
import supul.model.User;
import supul.repository.AdminRepository;
import supul.repository.BranchRepository;
import supul.repository.ModiBranchRepository;
import supul.repository.PayRepository;
import supul.repository.RankingRepository;
import supul.repository.ReservationRepository;
import supul.repository.ThemaRepository;
import supul.repository.UserRepository;
import supul.repository.board.NoticeRepository;

import supul.service.SaleService;

@Controller
@RequestMapping("admin")
public class AdminController {

	@Autowired
	AdminRepository adminRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ReservationRepository reservationRepository;
	@Autowired
	ThemaRepository themaRepository;
	@Autowired
	BranchRepository branchRepository;
	@Autowired
	RankingRepository rankingRepository;
	@Autowired
	PayRepository payRepository;
	@Autowired
	SaleService stat;
	@Autowired
	BranchMapper branchMapper;
	@Autowired
	UserMapper usermapper;
	@Autowired
	ThemaMapper themaMapper;
	@Autowired
	NoticeRepository boardNRepository;
	@Autowired
	ModiBranchRepository modiBranchRepository;

	@RequestMapping("")
	public String adminmainmm(Model model, HttpSession session,
			@PageableDefault(size = 20, sort = "rvId", direction = Direction.DESC) Pageable pageable,
			@RequestParam(name = "sortBy", defaultValue = "time") String sortBy) {
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by(Sort.Direction.ASC, sortBy));
		
	
		
		LocalDate today = LocalDate.now();
		Page<Reservation> todayReservations = reservationRepository.findByDate(today, pageable);

		

		// 지점별 매출 계산
		List<Branch> branches = branchRepository.findAll();
		Map<Branch, Map<Thema, Integer>> branchSales = new HashMap<>();
		Map<Thema, Integer> themeSales = new HashMap<>();
		for (Branch branch : branches) {
			System.out.println(branch);

			for (Thema theme : branch.getTm()) {

				int totalSales = 0;
				if (theme.getSale().isEmpty()) {
					System.out.println("이거:" + theme);
					themeSales.put(theme, totalSales);
				} else {

					for (Reservation reservation : theme.getSale()) {
						System.out.println(reservation);
						System.out.println("널아니냐?" + theme);
						totalSales += reservation.getRvPrice();
						System.out.println(totalSales);
						themeSales.put(theme, totalSales);
					}
				}

			}
			branchSales.put(branch, themeSales);
		}
		// 세션등록 ==start==
		model.addAttribute("userRole", session.getAttribute("userRole"));
		model.addAttribute("admin", session.getAttribute("admin"));
		model.addAttribute("adminBn", session.getAttribute("adminBn"));
		model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
		// 세션등록 ==end==
		
		model.addAttribute("branchSales", branchSales);
		model.addAttribute("todayReservations", todayReservations);
		model.addAttribute("themeSales", themeSales);
	
	
		return "admin/admin_main";
	}

	@GetMapping("/logout")
	public String adminLogout(HttpSession session) {
		session.invalidate();
		return "redirect:/main";
	}

	@GetMapping("/login")
	public String adminLoginForm() {
		return "admin/admin_login";
	}

	@PostMapping("/login")
	public String processAdminLoginForm(PageData pageData, Model model, @RequestParam String adminId,
			@RequestParam String adminPw, HttpSession session) {
		Admin admin = adminRepository.findByAdminId(adminId);
		System.out.println("어드민"+admin);
		
		
		if (session.getAttribute("user") != null || session.getAttribute("super") != null) {
			session.invalidate();
		}
		
		if (admin != null && admin.getAdminPw().equals(adminPw) && admin.getAdminId().equals(adminId)) {
			Branch branch = branchRepository.findByName(admin.getBranchName());
			session.setAttribute("adminId", admin.getAdminId());
			session.setAttribute("admin", admin);
			session.setAttribute("adminRole", "admin");
			session.setAttribute("adminBranchId", branch.getBranchId());
			session.setAttribute("adminBn", admin.getBranchName());
			session.setAttribute("adminName", admin.getName());
			return "redirect:/admin";
		}

		pageData.setMsg("아이디 또는 비밀번호가 틀렸습니다.");
		pageData.setGoUrl("/admin/login");

		// 관리자가 없거나 비밀번호가 일치하지 않는 경우
		return "inc/alert"; // 로그인 실패 시 로그인 페이지로 다시 이동
	}
	@GetMapping("/checkAdminId")
	@ResponseBody
    public boolean checkAdminId(@RequestParam String adminId) {
		
		Admin admin = adminRepository.findByAdminId(adminId); 
        return admin == null;
    }

    @GetMapping("/checkEmail")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        Admin admin = adminRepository.findByEmail(email);
        return admin == null;
    }

// ==========지점 정보 수정===========================

//	// 지점 정보 수정 폼 보기
//	@GetMapping("/branch/update/{branchId}")
//	public String adminBranchForm(@PathVariable("branchId") String branchId, Model model, HttpSession session) {
//		System.out.println(branchId);
//		if(branchId.equals(null)|| branchId.equals("null")) {
//			return "redirect:/admin/login";
//		}
//		Branch branch = null;
//		if(branchId !=null || branchId =="null") {
//		branch = branchMapper.selectById(Integer.parseInt(branchId));
//		System.out.println(branch.getBranchId());
//		model.addAttribute("branch", branch);}
//		// 세션등록 ==start==
//		model.addAttribute("userRole", session.getAttribute("userRole"));
//		model.addAttribute("admin", session.getAttribute("admin"));
//		model.addAttribute("adminBn", session.getAttribute("adminBn"));
//		model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
//		// 세션등록 ==end==
//		
//		return "super/branch/branchUpdate";
//	}
//
//	// 지점 정보 수정 처리
//	@PostMapping("/branch/update")
//	public String adminBranchReg(@ModelAttribute Branch branch, Model model, HttpSession session) {
//		branchMapper.updateBranch(branch);
//		// 세션등록 ==start==
//		model.addAttribute("userRole", session.getAttribute("userRole"));
//		model.addAttribute("admin", session.getAttribute("admin"));
//		model.addAttribute("adminBn", session.getAttribute("adminBn"));
//		model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
//		// 세션등록 ==end==
//		
//			int a = (int) session.getAttribute("adminBn");
//			
//			
//		
//		
//		return "redirect:/branch/update/"+a;
//	}
    
    // =============================== 정현 수정 시작 ===============================
    // 지점정보 수정요청 폼 보기
    @GetMapping("/branch/modiBranchForm/{branchId}")
    public String modiBranchForm(@PathVariable("branchId") String branchId, Model model, HttpSession session) {
       System.out.println("브런치아이디"+branchId);
       System.out.println("지점정보 수정 폼 진입");
       if(branchId.equals(null)|| branchId.equals("null")) {
          return "redirect:/admin/login";
       }
       Branch branch = null;
       if(branchId !=null || branchId =="null") {
       branch = branchMapper.selectById(Integer.parseInt(branchId));
       model.addAttribute("branch", branch);
       model.addAttribute("branchId", branchId);
       model.addAttribute("branchName", branch.getName());
       }
       
       // 세션등록 ==start==
       model.addAttribute("userRole", session.getAttribute("userRole"));
       model.addAttribute("admin", session.getAttribute("admin"));
       model.addAttribute("adminBn", session.getAttribute("adminBn"));
       model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
       // 세션등록 ==end==
       
       return "admin/modiBranchForm";
    }

    // 지점정보 수정요청 처리
    @PostMapping("/branch/modiBranchForm")
    public String modiBranchFormReg(@ModelAttribute ModiBranch modiBranch,
          Model model, HttpSession session,
          @RequestParam String originbranchId, @RequestParam String originbranchname) {
       System.out.println("지점정보 수정 처리 진입");
       System.out.println("요청된브런치:"+ modiBranch);
       
       Optional<ModiBranch> oModiBranch = modiBranchRepository.findByOriginbranchId(Integer.parseInt(originbranchId));
       System.out.println("존재하냐?"+oModiBranch);
       if(oModiBranch.isEmpty()) {
          System.out.println("요청한적없다 새로저장한다.");
            // 요청된 rqBranchId에 해당하는 엔티티가 존재하지 않는 경우
          modiBranch.setOriginbranchId(Integer.parseInt(originbranchId));
          modiBranch.setOriginbranchname(originbranchname);
          modiBranchRepository.save(modiBranch);
       }
       else {
            // 요청된 rqBranchId에 해당하는 엔티티가 이미 존재하는 경우
          System.out.println("요청 이미있다 업데이트한다.");
            ModiBranch originBranch = oModiBranch.get();
            originBranch.setPhone(modiBranch.getPhone());
            modiBranchRepository.save(originBranch);
       }
       // 세션등록 ==start==
       model.addAttribute("userRole", session.getAttribute("userRole"));
       model.addAttribute("admin", session.getAttribute("admin"));
       model.addAttribute("adminBn", session.getAttribute("adminBn"));
       model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
       // 세션등록 ==end==

       return "redirect:/admin";
    }
    
    // =============================== 정현 수정 끝 ===============================

	
	   @RequestMapping("rankList/{branchName}/{title}")
	   String list(Model model, Ranking rd, Branch bd, HttpSession session, Thema tm,
	         @PathVariable String branchName,
	         @PathVariable String title
	         ) {

	      // 랭킹 데이터 전체
		   List<Ranking> rankData = rankingRepository.findAll();
	      List<Branch> brnList = themaMapper.brList(bd);
	      List<Thema> themaList = themaRepository.findAll();
	       
	
	      
	      List<Ranking> filterRankData = new ArrayList<>();
	       for (Ranking rank : rankData) {
	          if ("전체".equals(branchName) || branchName.equals(rank.getBranchName())) {
	             if ("전체".equals(title) || title.equals(rank.getThemaName())) {
	                filterRankData.add(rank);
	             }
	          }
	       }
	      
	      System.out.println("themaList" + themaList);
	 

 
	      model.addAttribute("filterRankData", filterRankData);
	      model.addAttribute("rankData", rankData);
	      model.addAttribute("brnList", brnList);
	      model.addAttribute("themaList", themaList);
	      
	   
	      // 해당하는 아이디만 보이는 데이터
	      model.addAttribute("branchName", branchName); // 선택된 branchName을 모델에 추가
	      model.addAttribute("title", title);
	      // 세션등록 ==start==
	      model.addAttribute("userRole", session.getAttribute("userRole"));
	      model.addAttribute("admin", session.getAttribute("admin"));
	      model.addAttribute("adminBn", session.getAttribute("adminBn"));
	      model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
	      // 세션등록 ==end==
	      return "admin/admin_RankList";
	   }


	@GetMapping("rank/insert")
	public String insert(Model model, Ranking rd, Thema td, Branch bd, @RequestParam int rvId,
			@PageableDefault(size = 8, sort = "title", direction = Direction.DESC) Pageable pageable,
			HttpSession session) {

		List<Thema> themaList = themaRepository.findAll();
		
		
		List<Branch> brnList = themaMapper.brList(bd);
		List<Ranking> ranking = rankingRepository.findAll();
		Optional<Reservation> Orv = reservationRepository.findById(rvId);
		Reservation rv = Orv.get();
		
		
		
		
		
		model.addAttribute("brnList", brnList);
		model.addAttribute("themaList", themaList);
		model.addAttribute("rv", rv);
		model.addAttribute("pageData", new PageData());
		
		// 세션등록 ==start==
		model.addAttribute("userRole", session.getAttribute("userRole"));
		model.addAttribute("admin", session.getAttribute("admin"));
		model.addAttribute("adminBn", session.getAttribute("adminBn"));
		model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
		// 세션등록 ==end==

		return "admin/rankForm";
	}

	   @PostMapping("rank/insert")
	   String insertReg(Ranking rd, int rvId, PageData pageData,HttpSession session) {
	      Reservation rv = reservationRepository.findByRvId(rvId);
	      rd.setPay(rv.getPay());
	      rd.setThemaName(rv.getThemaName());
	      rd.setThema(rv.getThema());
	      rd.setUser(rv.getUser());
	 
	     
	     
	      String branchName = (String) session.getAttribute("adminBn");
	      rankingRepository.save(rd);
	     
	      List<Ranking> ranking = rankingRepository.findByThemaThemaId(rv.getThema().getThemaId());
	      ranking.sort(Comparator.comparing(Ranking::getTime));
	      for(Ranking rank :ranking) {
				rank.calcRank(ranking);
				System.out.println("rank"+rank.getThemaRank());
			    
			}
	  
	      pageData.setMsg("작성되었습니다.");
	      pageData.setGoUrl("/admin/rankList/"+branchName+"/전체");

	      return "inc/alert";
	   }
	
	
	@GetMapping("list")
	String rvlist(@RequestParam(required = false) String userName, @PageableDefault(size = 15,sort = "rvId" , direction = Direction.DESC ) Pageable pageable,
			@RequestParam(name = "sortBy", defaultValue = "rvDate") String sortBy, Model model, HttpSession session) {
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
	            Sort.by(Sort.Direction.DESC, sortBy)); 
		Page<Reservation> searchList;
		
		    searchList = stat.searchTm(pageable);
		    
	 List<Branch> branchList = branchRepository.findAll(); // BranchService를 이용하여 모든 지점을 가져오는 예시
	  model.addAttribute("branchList", branchList);
	 
		model.addAttribute("searchList", searchList);
		model.addAttribute("pageable", pageable);
		model.addAttribute("userName", userName);
		model.addAttribute("sortBy", sortBy);
		// 세션등록 ==start==
		model.addAttribute("userRole", session.getAttribute("userRole"));
		model.addAttribute("admin", session.getAttribute("admin"));
		model.addAttribute("adminBn", session.getAttribute("adminBn"));
		model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
		// 세션등록 ==end==
		return "admin/reservation";
	}
	
	
	@GetMapping("/rvList/{branchName}")
	public String getAll(@RequestParam(required = false) String userName,@PathVariable String branchName, @PageableDefault(size = 15,sort = "rvId" , direction = Direction.DESC ) Pageable pageable,
			@RequestParam(name = "sortBy", defaultValue = "rvId") String sortBy, Model model,HttpSession session,
			@RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection) {
		
	   
		Sort sort = Sort.by(sortDirection.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),sort      ); 
	   if(branchName.equals("null")) {
		   return "redirect:/admin/login";
	   }
		
		
	    List<Branch> branchList = branchRepository.findAll(); // BranchService를 이용하여 모든 지점을 가져오는 예시
		  model.addAttribute("branchList", branchList);
	   
			Branch branch =branchRepository.findByName(branchName);
			
			Page<Reservation> blackList= reservationRepository.findByNoShow(true, pageable);
			
			 Page<Reservation> searchList;
			 if (branchName.equals("전체") || branchName == null) {
				 	
			        if (userName == null || userName.isEmpty()|| userName.equals("null") ) {
			        	 searchList = reservationRepository.findAll(pageable);
			        	System.out.println("얘는 전체1"+branch+"1");
			           
			        } else {
			        	 searchList = reservationRepository.findByUserNameContaining(userName, pageable);
			            System.out.println("얘는 브런치이름1"+branch+"1");
			        }
			    } else {
			        if (userName == null || userName.isEmpty()|| userName.equals("null")) {
			            // 사용자 이름과 지점으로 검색하고 페이징을 적용합니다.
			        	searchList = stat.searchbr(branch, pageable);
			        	System.out.println("얘는 유"+userName);
			            System.out.println("얘는 브런치이름2"+branch+"2");
			        } else {
			        	searchList = reservationRepository.findByBranchAndUserNameContaining( branch, userName, pageable);
			            System.out.println(branch+"3");
			        }
			    }
	    

	
		model.addAttribute("tomorrow", LocalDate.now().plusDays(1));
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("searchList", searchList);
		model.addAttribute("blackList", blackList);
		model.addAttribute("pageable", pageable);
		model.addAttribute("userName", userName); 
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("sortDirection", sortDirection);
		model.addAttribute("branchName", branchName);
		// 세션등록 ==start==
		model.addAttribute("userRole", session.getAttribute("userRole"));
		model.addAttribute("admin", session.getAttribute("admin"));
		model.addAttribute("adminBn", session.getAttribute("adminBn"));
		model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
		// 세션등록 ==end==
		return "admin/reservation";
	}
	
	//노쇼처리
	 @PostMapping("/rvList/{branchName}/{rvId}/updateNoShow")
	   public String updateNoShowStatus(@PathVariable("rvId") int rvId, 
	           @PathVariable String branchName,
	           @RequestParam int page) {
	       Reservation reservation = reservationRepository.findByRvId(rvId);

	       if (reservation != null) {
	           // 사용자 정보 가져오기
	           User user = userRepository.findById(reservation.getUser().getId())
	                   .orElseThrow(() -> new IllegalArgumentException("해당 아이디가 존재하지 않습니다."));
	           
	           // 노쇼 상태를 토글 (true -> false, false -> true)
	           boolean newNoShowStatus = !reservation.isNoShow();
	           stat.updateNoShowStatus(rvId, newNoShowStatus);

	           if (user != null) {
	               int noShowCount = user.getNoShowCount();
	               // 노쇼 횟수 증가 또는 감소
	               if (newNoShowStatus) {
	                   // 새로운 상태가 노쇼인 경우
	                   noShowCount++;
	               } else {
	                   // 새로운 상태가 노쇼 취소인 경우
	                   noShowCount--;
	               }
	               user.setNoShowCount(noShowCount);
	               
	               // 노쇼 횟수가 3회 이상이면 예약 못하게 처리
	               if (noShowCount >= 3) {
	                   user.setBlacklist(true);
	               } else {
	                   user.setBlacklist(false); // 노쇼 횟수가 3회 미만인 경우 블랙리스트 해제
	               }
	               
	               userRepository.save(user);
	           }
	       } 

	       return "redirect:/admin/rvList/{branchName}?page=" + page; // 노쇼 상태 업데이트 후 다시 목록 페이지로 리다이렉트
	   }

	 
	  @PostMapping("/rvList/{branchName}/{rvId}/alert2")
	    public String updatealert2(@PathVariable("rvId") int rvId,
	    		@PathVariable("branchName")String branchName,Model model, PageData pageData,
	    		@RequestParam int page) {
		  	pageData.setMsg("결제취소 하시겠습니까?");
		  	pageData.setGoUrl("/admin/rvList/"+branchName +"/"+rvId+ "/updatepaid?page="+page);
					  
		
		 return "inc/alert";
	    }
	
	//결제처리
	  @GetMapping("/rvList/{branchName}/{rvId}/updatepaid")
	    public String updatePaidStatus(@PathVariable("rvId") int rvId, 
	    		@PathVariable("branchName") String branchName, Model model,HttpSession session,
	    		@RequestParam int page) {
	
		  
		  Reservation a =stat.pay(rvId);
		  if(reservationRepository.findByRvId(rvId).isPaid()==false) {   
		  stat.updatePaidStatus(rvId, true);
		  Pay b =new Pay();
			 b.setReservation(a);
			 b.setTotalprice(a.getPrice());
			 b.setSaleDate(LocalDateTime.now());
			 payRepository.save(b);
		  }else {
			stat.updatePaidStatus(rvId, false);
			
			Pay b =payRepository.findByReservationRvId(rvId);
			payRepository.deleteById(b.getPayId());
		  }
		 
	
		
		 return "redirect:/admin/rvList/{branchName}?page="+page;
	    }
	  @PostMapping("/rvList/{branchName}/{rvId}/alert")
	    public String updatealert(@PathVariable("rvId") int rvId,HttpSession session,
	    		@PathVariable("branchName")String branchName,Model model, PageData pageData,
	    		@RequestParam int page) {
		  	pageData.setMsg("결제완료 되었습니다.");
		  	pageData.setGoUrl("/admin/rvList/"+branchName +"/"+rvId+ "/updatepaid?page="+page);
		 // 세션등록 ==start==
			model.addAttribute("userRole", session.getAttribute("userRole"));
			model.addAttribute("admin", session.getAttribute("admin"));
			model.addAttribute("adminBn", session.getAttribute("adminBn"));
			model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
			// 세션등록 ==end==		  
		
		 return "inc/alert";
	    }

	
	  @GetMapping("blackList")
		String blacklist(@RequestParam(required = false) String userName, @PageableDefault(size = 15,sort = "rvId" , direction = Direction.DESC ) Pageable pageable,
				@RequestParam(name = "sortBy", defaultValue = "rvDate") String sortBy,
				@RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection, Model model, HttpSession session) {
		
		  
		  Sort sort = Sort.by(sortDirection.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),sort      ); 
			
			    
		 List<Branch> branchList = branchRepository.findAll(); // BranchService를 이용하여 모든 지점을 가져오는 예시
		  	model.addAttribute("branchList", branchList);
			model.addAttribute("pageable", pageable);
			model.addAttribute("userName", userName);
			model.addAttribute("sortBy", sortBy);
			// 세션등록 ==start==
			model.addAttribute("userRole", session.getAttribute("userRole"));
			model.addAttribute("admin", session.getAttribute("admin"));
			model.addAttribute("adminBn", session.getAttribute("adminBn"));
			model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
			// 세션등록 ==end==
			return "admin/blacklist";
		}
	
	  @GetMapping("/blackList/{branchName}")
		public String blacklistbranch(@RequestParam(required = false) String userName,@PathVariable String branchName, @PageableDefault(size = 15,sort = "rvId" , direction = Direction.DESC ) Pageable pageable,
				@RequestParam(name = "sortBy", defaultValue = "rvId") String sortBy,@RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
				
				Model model,HttpSession session) {
			 // 정렬을 적용합니다. (Spring Data JPA의 PageRequest 객체를 사용)
		  Sort sort = Sort.by(sortDirection.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),sort      ); 
		   
			 if(branchName.equals("null")) {
				   return "redirect:/admin/login";
			   }
			
			List<Branch> branchList = branchRepository.findAll(); // BranchService를 이용하여 모든 지점을 가져오는 예시
			  model.addAttribute("branchList", branchList);
		   
				Branch branch =branchRepository.findByName(branchName);
				
				
				
				 Page<Reservation> searchList;
				 if (branchName.equals("전체") || branchName == null) {
					 	
				        if (userName == null || userName.isEmpty()|| userName.equals("null") ) {
				        	 searchList = reservationRepository.findByNoShow(true, pageable);
				        	System.out.println("얘는 전체1"+branch+"1");
				           
				        } else {
				        	 searchList = reservationRepository.findByUserNameContainingAndNoShow(userName,true, pageable);
				            System.out.println("얘는 브런치이름1"+branch+"1");
				        }
				    } else {
				        if (userName == null || userName.isEmpty()|| userName.equals("null")) {
				            // 사용자 이름과 지점으로 검색하고 페이징을 적용합니다.
				        	searchList = reservationRepository.findByBranchAndNoShow(branch,true, pageable);
				        	System.out.println("얘는 유"+userName);
				            System.out.println("얘는 브런치이름2"+branch+"2");
				        } else {
				        	searchList = reservationRepository.findByBranchAndNoShowAndUserNameContaining( branch,true, userName, pageable);
				            System.out.println(branch+"3");
				        }
				    }
		    

		
			model.addAttribute("searchList", searchList);
			
			model.addAttribute("pageable", pageable);
			model.addAttribute("userName", userName); 
			model.addAttribute("sortBy", sortBy);
			model.addAttribute("branchName", branchName);
			// 세션등록 ==start==
			model.addAttribute("userRole", session.getAttribute("userRole"));
			model.addAttribute("admin", session.getAttribute("admin"));
			model.addAttribute("adminBn", session.getAttribute("adminBn"));
			model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
			// 세션등록 ==end==
			return "admin/blackList";
		}
}
