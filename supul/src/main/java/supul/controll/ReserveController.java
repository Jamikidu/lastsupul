package supul.controll;


import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

import supul.mapper.ThemaMapper;
import supul.model.Branch;
import supul.model.Coupon;
import supul.model.PageData;

import supul.model.Reservation;

import supul.model.RvPay;
import supul.model.Thema;
import supul.model.User;
import supul.repository.BranchRepository;
import supul.repository.CouponRepository;
import supul.repository.PayRepository;
import supul.repository.RankingRepository;
import supul.repository.ReservationBackUpRepository;
import supul.repository.ReservationRepository;
import supul.repository.RvPayRepository;
import supul.repository.ThemaRepository;
import supul.repository.UserRepository;

import supul.service.SaleService;

@Controller
@RequestMapping("reserve")
public class ReserveController {

	@Autowired
	private SaleService stat;
	@Autowired
	ThemaMapper themaMapper;
	@Autowired
	UserRepository userRepository;
	@Resource
	ReservationRepository reservationRepository;
	@Resource
	ReservationBackUpRepository reservationBackUpRepository;
	@Resource
	BranchRepository branchRepository;
	@Resource
	ThemaRepository themaRepository;
	@Resource
	PayRepository payRepository;
	@Resource
	RankingRepository rankingRepository;
	@Resource
	RvPayRepository rvpayRepository;

	@Resource
	SmsService sms;
	@Autowired
	CouponRepository couponRepository;

	@GetMapping("/rvForm")
	String reservationg(Model model, Thema thema, HttpSession session, Reservation tv,
			@RequestParam(value = "themaId", required = false) String themaId,
			@RequestParam(value = "picktime", required = false) LocalTime picktime,
			@RequestParam(value = "date", required = false) LocalDate date, PageData pageData) {
		// ==============영준 수정0919 ================//
		if (session.getAttribute("user") == null) {
			pageData.setMsg("회원만 예약할수있습니다.");
			pageData.setGoUrl("/thema/intro");
			return "inc/alert";
		};
		User user = (User) session.getAttribute("user");
		if(user.isBlacklist()) {
			pageData.setMsg("잦은 노쇼로 인해 예약할 수 없습니다.");
			pageData.setGoUrl("/thema/intro");
			return "inc/alert";
		}
		Optional<Thema> tm = themaRepository.findById(Integer.parseInt(themaId));
		
		if(!tm.isPresent()) {
			pageData.setMsg("잘못된 접근입니다.");
			pageData.setGoUrl("/thema/intro");
			return "inc/alert";
			
		}
		
		if (date != null && date.isBefore(LocalDate.now())) {

			if (date.atTime(picktime).isBefore(LocalDateTime.now().minusMinutes(30))) {

				pageData.setMsg("예약시간이 지났습니다.");
				pageData.setGoUrl("/thema/intro");
				return "inc/alert";
			}
		}
		if (date != null && date.equals(LocalDate.now())) {
			if (date.atTime(picktime).isBefore(LocalDateTime.now().minusMinutes(30))) {

				pageData.setMsg("예약시간이 지났습니다.");
				pageData.setGoUrl("/thema/intro");
				return "inc/alert";
			}
			
				
			
		}

		List<Thema> tlist = themaRepository.findAll();

		for (Thema a : tlist) {
			if (a.getThemaId() == Integer.parseInt(themaId)) {

				if (!a.getTimetable().contains(picktime)) {
					System.out.println(
							a.getTimetable().contains(picktime) + "pic" + picktime + a.getThemaId() + "테마아디" + themaId);
					pageData.setMsg("잘못된 시간입니다.");
					pageData.setGoUrl("/thema/intro");
					return "inc/alert";
				}
			}

		}

		
	      if (date != null && date.isAfter(LocalDate.now().plusWeeks(2))) {
	          

	            pageData.setMsg("아직 열리지 않은 예약입니다.");
	            pageData.setGoUrl("/thema/intro");
	            return "inc/alert";
	         
	      } 
		List<Reservation> rv = reservationRepository.findTodaydate(LocalDate.now());

		String res = date + "_" + picktime + "_" + themaId;
		System.out.println(res);
		for (Reservation a : rv) {
			if (a.getReserChkStr().equalsIgnoreCase(res)) {

				pageData.setMsg("이미 예약되었습니다.");
				pageData.setGoUrl("/thema/intro");
				return "inc/alert";
			}
		}

		// ==============영준 수정0919 ================//
		System.out.println(themaId);
		Optional<Thema> opthema = themaRepository.findById(Integer.parseInt(themaId));
		if (opthema.isPresent()) {
			thema = opthema.get();
		} else {

		}

		System.out.println(thema);
		model.addAttribute("thema", thema);
		model.addAttribute("date", date);
		model.addAttribute("picktime", picktime);

		System.out.println(session.getAttribute("username"));

		// reservation에 들어갈거

		List<LocalTime> timeLists = themaMapper.timetableList(Integer.parseInt(themaId));
		// System.out.println("시간들 =>"+timeLists);

		if (picktime.equals(timeLists.get(0)) || picktime.equals(timeLists.get(timeLists.size() - 1))) {
			model.addAttribute("price", (int) (thema.getPrice() * 0.8 / 1000) * 1000);
			System.out.println("첫타임, 막타임! => " + (int) (thema.getPrice() * 0.8 / 1000) * 1000);
		} else {
			model.addAttribute("price", thema.getPrice());
		}
		model.addAttribute("rvPrice", (int) (thema.getPrice() * 0.2 / 1000) * 1000);
		;

		
		System.out.println(user);
		model.addAttribute("user", user);

		// ====================== 정현수정 ======================
		int id = (int) session.getAttribute("Id");
		Optional<User> Ouser = userRepository.findById(id);
		System.out.println("유저 너 맞니??" + Ouser.get());
		List<Coupon> coupons = couponRepository.findByUserAndUsedIsFalseOrderByRegDate(Ouser.get());
		System.out.println("쿠폰즈~~" + coupons);
		model.addAttribute("coupons", coupons);
		session.removeAttribute("Ocoupon");

		// ====================== 정현수정 ======================
		

		return "reserve/reserveForm";
	}

	@PostMapping("/rvReg")
	String reservation_OK(Model model, int themaId, Thema thema, Reservation rv, @RequestParam("rvPeople") int rvPeople,
			PageData pageData, @RequestParam(name = "couponNum", required = false) String couponNum,
			HttpSession session) {

	   
		model.addAttribute("today",  LocalDate.now());
		int id = (int) session.getAttribute("Id");
		Optional<User> Ouser = userRepository.findById(id);
		User user = new User();
		if (Ouser.isPresent()) {
			user = Ouser.get();
		}

		// 예약번호 생성을 위한 포맷작업
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
		String datenum = (String) LocalDate.now().format(formatter);
		Random ran = new Random();
		// 중복 없으면 예약번호 생성 아니면 재생성
		while (true) {
			int i = ran.nextInt(1, 10000);

			String rvnum = datenum + String.format("%04d", i); // int i 5여도 0005 로 나오게 변환
			// System.out.println("중복처리 전 rvnum: "+rvnum);
			int cnt = themaMapper.chkrvnum(rvnum);
			if (cnt == 0) {
				// System.out.println("중복처리 후 rvnum: "+rvnum);
				rv.setRvNum(rvnum);
				model.addAttribute("rvnum", rvnum);
				break;
			}
		}

		// ====================== 정현수정 ======================
		// + 매개변수에 @RequestParam(name = "coupon") int coupon 추가함
		if (couponNum != null && couponRepository.findByCouponNum(couponNum) != null) {
			Coupon Ocoupon = couponRepository.findByCouponNum(couponNum);
			System.out.println("이프안쿠폰 => " + Ocoupon);
			session.setAttribute("Ocoupon", Ocoupon);
			rv.setRvPrice((rv.getRvPrice() - Ocoupon.getDiscount()));

		}
		// ====================== 정현수정 ======================

		Optional<Thema> opthema = themaRepository.findById(themaId);
		if (opthema.isPresent()) {
			thema = opthema.get();
		}
		Optional<Branch> a = branchRepository.findById(thema.getBranch().getBranchId());
		Branch branch = a.get();
		rv.setRvDate(LocalDateTime.now());
		System.out.println(LocalDateTime.now());
		rv.setThema(thema);
		rv.setUser(user);
		rv.setBranch(branch);
		rv.setUserName(user.getUserName());
		session.setAttribute("rv", rv);

		pageData.setMsg("결제 창으로 넘어갑니다!");
		pageData.setGoUrl("/reserve/pay/" + rv.getRvNum());
		return "inc/alert";

	}

	@RequestMapping("/pay/{rvNum}")
	public String pay(Model model, @PathVariable String rvNum, HttpSession session) {

		Reservation rv = (Reservation) session.getAttribute("rv");

		model.addAttribute("rv", rv);
		model.addAttribute("user", session.getAttribute("user"));
		model.addAttribute("username", session.getAttribute("username"));

		System.out.println(rv);

		if (session.getAttribute("Ocoupon") != null) {
			model.addAttribute("coupon", session.getAttribute("Ocoupon"));
		}
		System.out.println("쿠폰이다!!=>" + session.getAttribute("Ocoupon"));
		return "reserve/reserveReg";
	}

	@RequestMapping("/pay/completed")
	public String paycompleted(Model model, HttpSession session) {
		System.out.println("여기 왔나?1");
		Reservation rv = (Reservation) session.getAttribute("rv");
		System.out.println("야야야" + rv);
		String text = "예약완료\n" + rv.getThemaName() + " " + rv.getDate() + " " + rv.getTime() + "으로 예약되었습니다.\n"
				+ "예약번호는 " + rv.getRvNum() + "\n예약금" + rv.getRvPrice() + "원이 입금되었습니다. ";

		//sms.sendOne("01023112390", "01023112390", text);
		RvPay rvPay = (RvPay) session.getAttribute("rvpay");
		rv.setRvpay(rvPay);
		reservationRepository.save(rv);
		rvPay.setReservation(rv);

		rvpayRepository.save(rvPay);
		System.out.println("여기 왔나?5" + rv);
		session.removeAttribute("rvpay");
		model.addAttribute("rv", rv);
		model.addAttribute("user", session.getAttribute("user"));
		model.addAttribute("username", session.getAttribute("username"));

		if (session.getAttribute("Ocoupon") != null) {
			Coupon Ocoupon = (Coupon) session.getAttribute("Ocoupon");
			System.out.println("내가원하는쿠폰=>" + Ocoupon);
			Ocoupon.setUsed(true);
			Ocoupon.setUsedDate(LocalDateTime.now());
			couponRepository.save(Ocoupon);
			session.removeAttribute("Ocoupon");
		}

		return "reserve/complete";
	}

//	@PostMapping("preparePay")
//	@ResponseBody
//	Object preparePay() {
//		restPay.preparePay(RvPay, total);
//		return "사전 검증 성공";
//	}
//	
//	@PostMapping("orderConfirm")
//	@ResponseBody
//	Object orderConfirm(
//			HttpSession session,
//			@RequestBody OrderDTO ordDTO) {
//		// String nonMem = (String)session.getAttribute("nonMemberId");
//		Map<Object, LinkedHashMap<Object, Object>> response = 
//				(Map<Object, LinkedHashMap<Object, Object>>) restPay.afterPay(ordDTO);
//		if(this.ordDTO.getOrdersTotalPrice() == (int)response.get("response").get("amount")) {
//			this.ordDTO.setImp_uid(ordDTO.getImp_uid());
//			for(BagsDTO bag : updStock) {
//				bagMapper.updSells(bag);
//				// bagMapper.updStock(bag);
//			}
//			cartMapper.allDelete(ordDTO.getMemberId());
//			this.ordDTO.setOrderStatus("결제 완료");
//			ordMapper.orderInsert(this.ordDTO);
//			/* if(nonMem != null) {
//				session.removeAttribute("nonMemberId");
//				session.removeAttribute("nonMemberName");
//				session.removeAttribute("nonMemberPhone");
//			} */
//			
//			return ResponseEntity.ok("결제 완료");
//		}else {
//			ordDTO.setCancelReason("잘못된 결제 요청");
//			ordDTO.setImpUid(ordDTO.getImp_uid());
//			ordDTO.setMerchantUid(ordDTO.getMerchant_uid());
//			ordDTO.setOrdersTotalPrice(total);
//			Map<String, Object> cancelRes = (Map<String, Object>) restPay.cancelPay(ordDTO);
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 실패");
//		}
//	}
//	

	// 예약확인
	@RequestMapping("checkRv")
	String checkreservation(Model model, HttpSession session, PageData pageData) {
		model.addAttribute("username", session.getAttribute("username"));
		model.addAttribute("user", session.getAttribute("user"));
		if (session.getAttribute("Id") == null) {
			pageData.setMsg("로그인이 필요합니다!");
			pageData.setGoUrl("/user/login");
			return "inc/alert";
		}
		
		int userId = (int) session.getAttribute("Id");
		List<Reservation> rvList = reservationRepository.findAllUpcomingReservationsForUser(userId, LocalDate.now());
		// System.out.println("나오냐 "+rvList);
		
		model.addAttribute("nowdate", LocalDate.now());
		model.addAttribute("nowtime", LocalTime.now());
		model.addAttribute("rvList", rvList);
		model.addAttribute("session", session);
		model.addAttribute("userName", (String) session.getAttribute("username"));
		return "reserve/checkRv";
	}

	// 예약취소
	@RequestMapping("/cancleRv")
	String cancleRv(Model mm, int rvId, HttpSession session, PageData pageData) {// 파라미터값이 매개변수랑 이름이 똑같으면 알아서 들어감
		mm.addAttribute("userName", (String) session.getAttribute("username"));

		Reservation rvDTO = reservationRepository.findByRvId(rvId);
	
		rvDTO.setCancle(true);	
		rvDTO.setPayCancle(false);
		reservationRepository.save(rvDTO);
		
		
		pageData.setMsg("예약취소 성공(예약금 환불불가)");
		pageData.setGoUrl("/reserve/checkRv");
	
		return "inc/alert";
	}

	@RequestMapping("tonggye/{branchName}")
	public String chart(Model model, HttpSession session, @PathVariable String branchName,
			@RequestParam(name = "startDate", required = false) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) LocalDate endDate,
			@PageableDefault(size = 20, sort = "rvDate", direction = Direction.DESC) Pageable pageable,
			@RequestParam(name = "sortBy", defaultValue = "rvId") String sortBy,
//				@RequestParam(name ="month" ,required = false ) int month,
			@RequestParam(name = "sortDirection", defaultValue = "DESC") String sortDirection,
			@RequestParam(name = "type", defaultValue = "") String type, // 검색 유형 (content, title, user)
			@RequestParam(name = "keyword", defaultValue = "") String keyword) {

		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by(Sort.Direction.ASC, sortBy));

		Branch branch = branchRepository.findByName(branchName);

//				ModelAndView modelAndView = new ModelAndView("statistics");
//		        modelAndView.addObject("month", month);
		if(branchName.equals("null")) {
			return "redirect:/admin/login";
		}
		
		
		Page<Reservation> searchList;
		if (branchName.equals("전체") || branchName == null) {
			searchList = reservationRepository.findAll(pageable);
		} else {
			searchList = stat.searchbr(branch, pageable);
		}

		// 총 토탈! 과거부터 현재까지!
		BigDecimal totalSalesPrice = stat.totalSales(startDate, endDate);
		model.addAttribute("salesData", totalSalesPrice);

		List<Object[]> thema_price = reservationRepository.thema_price();
		model.addAttribute("thema_price", thema_price);

		List<Object[]> date_price = reservationRepository.date_price();
		model.addAttribute("date_price", date_price);

		Object[] branch_Total = stat.branch_Total(branchName);
		model.addAttribute("branch_Total", branch_Total);

		List<Object[]> branchListTotal = reservationRepository.branchListTotal();
		model.addAttribute("branchListTotal", branchListTotal);

		List<Object[]> branchThemaTotal = stat.branchThemaTotal(branchName);
		model.addAttribute("branchThemaTotal", branchThemaTotal);

		model.addAttribute("branch", branch);
		List<Object[]> thematotal = stat.themaTotal(startDate, endDate);
		model.addAttribute("thematotal", thematotal);
		List<Object[]> total = stat.total(startDate, endDate);
		model.addAttribute("total", total);
		List<Object[]> branchtotal = stat.branchTotal(startDate, endDate);
		model.addAttribute("branchtotal", branchtotal);

		Map<YearMonth, Map<String, Integer>> monthlyData = new HashMap<>();

		List<Reservation> rvList = reservationRepository.findAll();
		Map<String, Integer> monthlyStats = Reservation.MonthlyStats(rvList);
		model.addAttribute("monthlyStats", monthlyStats);
		Map<String, Integer> ReservationCount = Reservation.ReservationCount(rvList);
		model.addAttribute("monthlyStats", monthlyStats);

		// totalData를 반복하고 월별로 그룹화하여 저장
		for (Object[] row : total) {
			String themaName = (String) row[0];
			Integer totalPrice = ((Number) row[1]).intValue(); // Object를 Integer로 변환

			LocalDateTime dateTime = (LocalDateTime) row[2];
			YearMonth yearMonth = YearMonth.from(dateTime);

			// 해당 월에 대한 Map이 없으면 생성
			monthlyData.putIfAbsent(yearMonth, new HashMap<>());

			// 테마별 가격을 해당 월에 추가 또는 누적
			monthlyData.get(yearMonth).put(themaName,
					monthlyData.get(yearMonth).getOrDefault(themaName, 0) + totalPrice);
		}

		// 월별로 테마별 가격 출력 또는 사용
		for (Map.Entry<YearMonth, Map<String, Integer>> entry : monthlyData.entrySet()) {
			YearMonth yearMonth = entry.getKey();
			Map<String, Integer> monthlyTotal = entry.getValue();

			// System.out.println("월: " + yearMonth.toString());
			for (Map.Entry<String, Integer> totalEntry : monthlyTotal.entrySet()) {
				String themaName = totalEntry.getKey();
				Integer totalPrice = totalEntry.getValue();
				// System.out.println("테마: " + themaName + ", 가격: " + totalPrice);
			}
		}
		ObjectMapper objectMapper = new ObjectMapper();
		String monthlyDataJson = null;
		try {
			monthlyDataJson = objectMapper.writeValueAsString(monthlyData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("monthlyDataJson", monthlyDataJson);

		List<Branch> branchList = branchRepository.findAll();
		List<Object[]> thema_priceDate = stat.thema_price(startDate, endDate);

		model.addAttribute("thema_priceDate", thema_priceDate);
		model.addAttribute("branchList", branchList);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("searchList", searchList);
		model.addAttribute("branchName", branchName);
		model.addAttribute("monthlyData", monthlyData);
		// 세션등록 ==start==
		model.addAttribute("userRole", session.getAttribute("userRole"));
		model.addAttribute("admin", session.getAttribute("admin"));
		model.addAttribute("adminBn", session.getAttribute("adminBn"));
		model.addAttribute("adminBranchId", session.getAttribute("adminBranchId"));
		// 세션등록 ==end==
		return "pay/tonggye";

	}

	@RequestMapping("paytonggye/{branchName}")
	public String paychart(Model model, @PathVariable String branchName, HttpSession session,
			@RequestParam(name = "startDate", required = false) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) LocalDate endDate,
			@PageableDefault(size = 20, sort = "rvDate", direction = Direction.DESC) Pageable pageable,
			@RequestParam(name = "sortBy", defaultValue = "rvId") String sortBy) {
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by(Sort.Direction.ASC, sortBy));

		model.addAttribute("userRole", session.getAttribute("userRole"));
		Branch branch = branchRepository.findByName(branchName);
		System.out.println(branch);
		System.out.println(branchName);
		Page<Reservation> searchList;
		if (branchName.equals("전체") || branchName == null) {
			searchList = reservationRepository.findAll(pageable);
		} else {
			searchList = reservationRepository.findByBranch(branch, pageable);
		}

		BigDecimal totalSalesPrice = payRepository.TotalSales(startDate, endDate);
		model.addAttribute("salesData", totalSalesPrice);

		List<Object[]> thema_price = payRepository.thema_price();
		model.addAttribute("thema_price", thema_price);

		model.addAttribute("branch", branch);
		List<Object[]> thematotal = payRepository.themaTotal(startDate, endDate);
		model.addAttribute("thematotal", thematotal);
		List<Object[]> total = payRepository.Total(startDate, endDate);
		model.addAttribute("total", total);
		List<Object[]> branchtotal = payRepository.branchTotal(startDate, endDate);
		model.addAttribute("branchtotal", branchtotal);

		Map<YearMonth, Map<String, Integer>> monthlyData = new HashMap<>();

		// totalData를 반복하고 월별로 그룹화하여 저장
		for (Object[] row : total) {
			String themaName = (String) row[0];
			Integer totalPrice = ((Number) row[1]).intValue(); // Object를 Integer로 변환

			LocalDateTime dateTime = (LocalDateTime) row[2];
			YearMonth yearMonth = YearMonth.from(dateTime);

			// 해당 월에 대한 Map이 없으면 생성
			monthlyData.putIfAbsent(yearMonth, new HashMap<>());

			// 테마별 가격을 해당 월에 추가 또는 누적
			monthlyData.get(yearMonth).put(themaName,
					monthlyData.get(yearMonth).getOrDefault(themaName, 0) + totalPrice);
		}

		// 월별로 테마별 가격 출력 또는 사용
		for (Map.Entry<YearMonth, Map<String, Integer>> entry : monthlyData.entrySet()) {
			YearMonth yearMonth = entry.getKey();
			Map<String, Integer> monthlyTotal = entry.getValue();

			// System.out.println("월: " + yearMonth.toString());
			for (Map.Entry<String, Integer> totalEntry : monthlyTotal.entrySet()) {
				String themaName = totalEntry.getKey();
				Integer totalPrice = totalEntry.getValue();
				// System.out.println("테마: " + themaName + ", 가격: " + totalPrice);
			}
		}
		ObjectMapper objectMapper = new ObjectMapper();
		String monthlyDataJson = null;
		try {
			monthlyDataJson = objectMapper.writeValueAsString(monthlyData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("monthlyDataJson", monthlyDataJson);

		List<Reservation> rvList = reservationRepository.findAll();
		List<Branch> branchList = branchRepository.findAll();

		String admin = (String) session.getAttribute("branchName");
		model.addAttribute("admin", admin);

		model.addAttribute("branchList", branchList);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("searchList", searchList);
		model.addAttribute("branchName", branchName);
		model.addAttribute("monthlyData", monthlyData);
		return "pay/paytonggye";

	}

}
