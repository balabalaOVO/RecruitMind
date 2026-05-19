package com.example.recruitmentagent.controller;

import com.example.recruitmentagent.dto.response.AnalyzeResponse;
import com.example.recruitmentagent.service.SampleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class SampleDataController {

    private static final Logger log = LoggerFactory.getLogger(SampleDataController.class);

    private final SampleDataService sampleDataService;

    public SampleDataController(SampleDataService sampleDataService) {
        this.sampleDataService = sampleDataService;
    }

    @GetMapping("/sample-run")
    public ResponseEntity<AnalyzeResponse> runSamples() {
        log.info("收到样例数据分析请求（3份材料：candidate1, candidate2, project3）");
        AnalyzeResponse response = sampleDataService.runSamples();
        log.info("样例分析完成，共分析 {} 份材料", response.data() != null ? response.data().totalAnalyzed() : 0);
        return ResponseEntity.ok(response);
    }
}
