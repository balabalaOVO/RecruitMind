package com.example.recruitmentagent.service;

import com.example.recruitmentagent.dto.request.AnalyzeRequest;
import com.example.recruitmentagent.dto.request.AnalyzeRequest.Material;
import com.example.recruitmentagent.dto.response.AnalyzeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SampleDataService {

    private static final Logger log = LoggerFactory.getLogger(SampleDataService.class);

    private static final List<SampleEntry> SAMPLES = List.of(
        new SampleEntry("candidate1", "sample_data/candidate1.txt"),
        new SampleEntry("candidate2", "sample_data/candidate2.txt"),
        new SampleEntry("project3", "sample_data/project3.txt")
    );

    private final RecruitmentAgentService agentService;

    public SampleDataService(RecruitmentAgentService agentService) {
        this.agentService = agentService;
    }

    public AnalyzeResponse runSamples() {
        List<Material> materials = SAMPLES.stream()
            .map(s -> new Material(s.id, loadSampleFile(s.path)))
            .toList();

        log.info("已加载 {} 份样例材料，开始分析", materials.size());
        return agentService.analyze(new AnalyzeRequest(materials, null));
    }

    private String loadSampleFile(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return resource.getContentAsString(StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("加载样例文件失败: {}", path, e);
            throw new RuntimeException("无法加载样例文件: " + path, e);
        }
    }

    private record SampleEntry(String id, String path) {}
}
