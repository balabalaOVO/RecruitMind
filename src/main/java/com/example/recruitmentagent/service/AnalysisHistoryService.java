package com.example.recruitmentagent.service;

import com.example.recruitmentagent.entity.AnalysisResult;
import com.example.recruitmentagent.entity.AnalysisSession;
import com.example.recruitmentagent.repository.AnalysisSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalysisHistoryService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisHistoryService.class);

    private final AnalysisSessionRepository sessionRepo;

    public AnalysisHistoryService(AnalysisSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    @Transactional
    public AnalysisSession saveSession(AnalysisSession session) {
        AnalysisSession saved = sessionRepo.save(session);
        log.info("分析会话已保存: id={}, traceId={}, 结果数={}", saved.getId(), saved.getTraceId(), saved.getResults().size());
        return saved;
    }

    public List<AnalysisSession> listAll() {
        return sessionRepo.findAllByOrderByCreatedAtDesc();
    }

    public AnalysisSession getById(Long id) {
        return sessionRepo.findById(id).orElse(null);
    }
}
