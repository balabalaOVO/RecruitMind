package com.example.recruitmentagent.repository;

import com.example.recruitmentagent.entity.AnalysisSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisSessionRepository extends JpaRepository<AnalysisSession, Long> {

    @EntityGraph(attributePaths = "results")
    List<AnalysisSession> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "results")
    Optional<AnalysisSession> findById(Long id);
}
