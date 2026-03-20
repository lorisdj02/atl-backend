package com.atl.backend.repository;
import com.atl.backend.model.FinalStageBracket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinalStageBracketRepository extends JpaRepository<FinalStageBracket, Integer>{
}
