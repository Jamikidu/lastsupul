package supul.model;

import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import supul.model.board.NoticeBoard;
import supul.model.board.QnaBoard;
import supul.model.board.ReviewBoard;
import supul.model.board.QnaComment;


@Entity
@Table(name ="branch")
@Data
public class Branch {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="branch_id")
	int branchId; 
	String name;
	String phone;
	String address; 
	String coordinate;  //좌표 추가
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "branch")
	List<Thema> tm;
	
	@OneToMany(mappedBy = "branch")
	List<Reservation> rv;
	
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "branch")
	List<QnaBoard> qna;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "branch")
	List<ReviewBoard> review;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "branch")
	List<QnaComment> comment;


	

	@Override
	public String toString() {
		return name;
	}


	
	
}
 