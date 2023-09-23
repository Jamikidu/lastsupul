package supul.repository;


import java.util.List;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import supul.model.Branch;
import supul.model.Thema;






public interface ThemaRepository extends JpaRepository<Thema, Integer> {

	Page<Thema> findAllByTitleContaining(String themaname, Pageable pageable);
	List<Thema> findByRankingThemaName(String themaname);
	List<Thema> findByBranch(Branch branch);
	List<Thema> findByTitle(String title);

}
  