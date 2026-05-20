package com.example.recruitmentagent.runner;

import com.example.recruitmentagent.dto.response.AnalyzeResponse;
import com.example.recruitmentagent.dto.response.AnalyzeResponse.ResultItem;
import com.example.recruitmentagent.service.SampleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.sample-runner.enabled", havingValue = "true")
public class SampleDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataRunner.class);

    private final SampleDataService sampleDataService;

    public SampleDataRunner(SampleDataService sampleDataService) {
        this.sampleDataService = sampleDataService;
    }

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("  样例数据批量分析开始（3份材料）");
        log.info("========================================");

        AnalyzeResponse response = sampleDataService.runSamples();

        if (response.data() == null) {
            log.error("样例分析失败: {}", response.message());
            return;
        }

        log.info("岗位描述: {}", response.data().jobDescription());
        log.info("共分析 {} 份材料", response.data().totalAnalyzed());
        log.info("----------------------------------------");

        for (ResultItem item : response.data().results()) {
            log.info("");
            log.info("【{}】", item.inputId());
            log.info("  标签:       {}", item.analysis().tags());
            log.info("  匹配度:     {}%", item.analysis().matchScore());
            log.info("  风险点:     {}", item.analysis().risks());
            log.info("  下一步动作: {}", item.analysis().nextAction());
            log.info("  建议追问:   {}", item.analysis().suggestedQuestions());
        }

        log.info("");
        log.info("========================================");
        log.info("  样例数据批量分析结束");
        log.info("========================================");
    }
}
