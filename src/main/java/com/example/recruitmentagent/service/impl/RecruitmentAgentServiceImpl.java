package com.example.recruitmentagent.service.impl;

import com.example.recruitmentagent.constant.NextActionEnum;
import com.example.recruitmentagent.dto.request.AnalyzeRequest;
import com.example.recruitmentagent.dto.request.AnalyzeRequest.Material;
import com.example.recruitmentagent.dto.response.AnalysisResult;
import com.example.recruitmentagent.dto.response.AnalyzeResponse;
import com.example.recruitmentagent.dto.response.AnalyzeResponse.ResultItem;
import com.example.recruitmentagent.entity.AnalysisSession;
import com.example.recruitmentagent.service.AnalysisHistoryService;
import com.example.recruitmentagent.service.RecruitmentAgentService;
import com.example.recruitmentagent.util.PromptLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecruitmentAgentServiceImpl implements RecruitmentAgentService {

    private static final Logger log = LoggerFactory.getLogger(RecruitmentAgentServiceImpl.class);

    private static final String DEFAULT_JOB_DESCRIPTION =
        "我们正在招聘一位高级Python后端工程师，要求5年以上Python开发经验，" +
        "熟悉Django/FastAPI等主流框架，有微服务架构设计经验，" +
        "熟悉Docker和Kubernetes，有团队管理经验者优先。";

    private final ChatModel chatModel;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final AnalysisHistoryService historyService;

    public RecruitmentAgentServiceImpl(ChatModel chatModel,
                                       PromptLoader promptLoader,
                                       ObjectMapper objectMapper,
                                       AnalysisHistoryService historyService) {
        this.chatModel = chatModel;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
        this.historyService = historyService;
    }

    @Override
    public AnalyzeResponse analyze(AnalyzeRequest request) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String jobDesc = request.jobDescription() != null && !request.jobDescription().isBlank()
            ? request.jobDescription()
            : DEFAULT_JOB_DESCRIPTION;

        List<Material> materials = request.materials();
        if (materials == null || materials.isEmpty()) {
            log.warn("[{}] 请求中无待分析材料", traceId);
            return AnalyzeResponse.error(400, "待分析材料列表不能为空");
        }

        log.info("[{}] 开始批量分析，共 {} 份材料，模型: qwen-plus", traceId, materials.size());

        String systemPrompt = promptLoader.load("prompts/system-prompt.txt");
        String analyzeTemplate = promptLoader.load("prompts/analyze-prompt.txt");

        List<ResultItem> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (Material material : materials) {
            try {
                log.info("[{}] 开始分析材料 [{}], 文本长度: {}", traceId, material.id(), material.text().length());
                AnalysisResult analysisResult = analyzeSingle(systemPrompt, analyzeTemplate, jobDesc, material, traceId);
                results.add(new ResultItem(material.id(), analysisResult));
                successCount++;
                log.info("[{}] 材料 [{}] 分析完成, 匹配度: {}, 动作: {}",
                    traceId, material.id(), analysisResult.matchScore(), analysisResult.nextAction());
            } catch (Exception e) {
                failCount++;
                log.error("[{}] 材料 [{}] 分析失败: {}", traceId, material.id(), e.getMessage(), e);
                results.add(new ResultItem(material.id(), fallbackResult(e.getMessage())));
            }
        }

        log.info("[{}] 批量分析完成, 成功: {}, 失败: {}, 总计: {}",
            traceId, successCount, failCount, materials.size());

        // Persist to database
        try {
            AnalysisSession session = new AnalysisSession(traceId, jobDesc, materials.size());
            session.setSuccessCount(successCount);
            session.setFailCount(failCount);
            for (int i = 0; i < results.size(); i++) {
                ResultItem item = results.get(i);
                Material material = materials.get(i);
                com.example.recruitmentagent.entity.AnalysisResult entity =
                    new com.example.recruitmentagent.entity.AnalysisResult(
                        item.inputId(),
                        truncate(material.text(), 5000),
                        item.analysis().matchScore(),
                        item.analysis().nextAction(),
                        toJson(item.analysis().tags()),
                        toJson(item.analysis().risks()),
                        toJson(item.analysis().suggestedQuestions()),
                        item.analysis().risks() != null && !item.analysis().risks().isEmpty()
                            && item.analysis().risks().get(0).startsWith("分析失败")
                            ? item.analysis().risks().get(0) : null
                    );
                session.addResult(entity);
            }
            historyService.saveSession(session);
        } catch (Exception e) {
            log.error("[{}] 持久化分析结果失败: {}", traceId, e.getMessage(), e);
        }

        return AnalyzeResponse.success(jobDesc, results);
    }

    private AnalysisResult analyzeSingle(String systemPrompt, String template,
                                         String jobDesc, Material material, String traceId) {
        String userPrompt = renderTemplate(template, Map.of(
            "jobDescription", jobDesc,
            "materialText", material.text()
        ));

        log.debug("[{}] 材料 [{}] 渲染后的 Prompt 长度: {}", traceId, material.id(), userPrompt.length());

        Prompt prompt = new Prompt(List.of(
            new SystemMessage(systemPrompt),
            new UserMessage(userPrompt)
        ));

        ChatResponse response = chatModel.call(prompt);
        String rawContent = response.getResult().getOutput().getText();
        log.debug("[{}] 材料 [{}] 模型原始响应: {}", traceId, material.id(), rawContent);

        String jsonStr = extractJson(rawContent);
        log.debug("[{}] 材料 [{}] 提取的 JSON: {}", traceId, material.id(), jsonStr);

        try {
            AnalysisResult result = objectMapper.readValue(jsonStr, AnalysisResult.class);
            validateResult(result, material, traceId);
            return result;
        } catch (JsonProcessingException e) {
            log.error("[{}] 材料 [{}] JSON 解析失败, 原始文本: {}", traceId, material.id(), rawContent);
            throw new RuntimeException("模型返回格式错误，无法解析为 JSON", e);
        }
    }

    private String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("模型返回空响应");
        }
        String cleaned = raw
            .replaceAll("```json\\s*", "")
            .replaceAll("```\\s*", "")
            .trim();

        int start = cleaned.indexOf('{');
        if (start == -1) {
            log.warn("未找到有效 JSON 对象，使用清理后的原始文本: {}", cleaned);
            return cleaned;
        }

        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = start; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\' && inString) {
                escape = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return cleaned.substring(start, i + 1);
                }
            }
        }
        log.warn("JSON 对象未闭合，使用清理后的原始文本: {}", cleaned);
        return cleaned;
    }

    private void validateResult(AnalysisResult result, Material material, String traceId) {
        if (result.matchScore() != null && (result.matchScore() < 0 || result.matchScore() > 100)) {
            log.warn("[{}] 材料 [{}] matchScore 超出范围: {}, 已裁剪",
                traceId, material.id(), result.matchScore());
        }
        if (result.nextAction() != null && !NextActionEnum.isValid(result.nextAction())) {
            log.warn("[{}] 材料 [{}] nextAction 值无效: {}",
                traceId, material.id(), result.nextAction());
        }
    }

    private AnalysisResult fallbackResult(String errorMsg) {
        return new AnalysisResult(
            Collections.emptyList(), 0,
            List.of("分析失败: " + errorMsg),
            "NEED_MORE_INFO", Collections.emptyList()
        );
    }

    private String toJson(List<String> list) {
        if (list == null) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return null;
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }

    private String renderTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
