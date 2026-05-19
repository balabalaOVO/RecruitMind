package com.example.recruitmentagent.controller;

import com.example.recruitmentagent.dto.request.AnalyzeRequest;
import com.example.recruitmentagent.dto.response.AnalyzeResponse;
import com.example.recruitmentagent.dto.response.ErrorResponse;
import com.example.recruitmentagent.service.RecruitmentAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class RecruitmentAgentController {

    private static final Logger log = LoggerFactory.getLogger(RecruitmentAgentController.class);

    private final RecruitmentAgentService service;

    public RecruitmentAgentController(RecruitmentAgentService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest request) {
        log.info("收到分析请求，材料数量: {}",
            request.materials() != null ? request.materials().size() : 0);

        AnalyzeResponse response = service.analyze(request);

        log.info("分析请求处理完成，状态码: {}", response.code());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("处理请求时发生异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
    }
}
