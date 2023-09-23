package supul.model.board;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import supul.model.Admin;
import supul.model.Branch;

@Entity
@Table(name = "notice_board")
@Data
public class NoticeBoard {
	 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id", nullable = false)
    private int noticeId; 
    
    
    
    private String writer;
    
    @Column(name = "branch_name")
    private String branchName;
 

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "content", nullable = false, length = 999)
    private String content;    
    
    @Column(name = "category")
    private String category; 
    
    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "modi_date")
    private LocalDateTime modiDate;

    
    @Transient
    private MultipartFile file;

    @Column(name = "file_name")
    private String fileName;

    @Transient
    private String filePath;
    
}