package com.example.recruitmentagent.controller;

import com.example.recruitmentagent.entity.AnalysisSession;
import com.example.recruitmentagent.service.AnalysisHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
public class HistoryController {

    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    private final AnalysisHistoryService historyService;

    public HistoryController(AnalysisHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> listHistory() {
        List<AnalysisSession> sessions = historyService.listAll();
        log.info("查询历史记录，共 {} 条会话", sessions.size());
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "success",
            "data", sessions
        ));
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<Map<String, Object>> getHistoryDetail(@PathVariable Long id) {
        AnalysisSession session = historyService.getById(id);
        if (session == null) {
            return ResponseEntity.ok(Map.of(
                "code", 404,
                "message", "会话不存在",
                "data", (Object) null
            ));
        }
        log.info("查询会话详情: id={}, 结果数={}", id, session.getResults().size());
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "success",
            "data", session
        ));
    }
}
