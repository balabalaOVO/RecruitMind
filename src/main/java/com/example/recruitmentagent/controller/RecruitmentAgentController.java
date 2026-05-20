package com.example.recruitmentagent.controller;

import com.example.recruitmentagent.dto.request.AnalyzeRequest;
import com.example.recruitmentagent.dto.request.AnalyzeRequest.Material;
import com.example.recruitmentagent.dto.response.AnalyzeResponse;
import com.example.recruitmentagent.dto.response.ErrorResponse;
import com.example.recruitmentagent.service.RecruitmentAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent")
public class RecruitmentAgentController {

    private static final Logger log = LoggerFactory.getLogger(RecruitmentAgentController.class);

    private final RecruitmentAgentService service;

    public RecruitmentAgentController(RecruitmentAgentService service) {
        this.service = service;
    }

    /**
     * JSON 格式输入：接受结构化 JSON 请求体
     */
    @PostMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest request) {
        log.info("收到 JSON 分析请求，材料数量: {}",
            request.materials() != null ? request.materials().size() : 0);

        AnalyzeResponse response = service.analyze(request);

        log.info("分析请求处理完成，状态码: {}", response.code());
        return ResponseEntity.ok(response);
    }

    /**
     * 纯文本输入模式：接受 text/plain 原始文本，作为单份材料分析
     */
    @PostMapping(value = "/analyze/text", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<AnalyzeResponse> analyzeText(@RequestBody String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return ResponseEntity.badRequest()
                .body(AnalyzeResponse.error(400, "文本内容不能为空"));
        }

        log.info("收到纯文本分析请求，文本长度: {}", rawText.length());
        String id = "text-" + UUID.randomUUID().toString().substring(0, 6);
        AnalyzeRequest request = new AnalyzeRequest(
            List.of(new Material(id, rawText.trim())), null);
        AnalyzeResponse response = service.analyze(request);
        log.info("纯文本分析请求处理完成，状态码: {}", response.code());
        return ResponseEntity.ok(response);
    }

    /**
     * 文件上传模式：接受 multipart/form-data 上传的文本文件，每份文件作为一份材料分析
     */
    @PostMapping(value = "/analyze/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeResponse> analyzeFiles(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(AnalyzeResponse.error(400, "上传文件列表不能为空"));
        }

        log.info("收到文件上传分析请求，文件数量: {}", files.size());
        List<Material> materials = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("跳过空文件: {}", file.getOriginalFilename());
                continue;
            }
            String filename = file.getOriginalFilename();
            String id = filename != null
                ? filename.replaceFirst("\\.[^.]+$", "")
                : "file-" + UUID.randomUUID().toString().substring(0, 6);
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
                materials.add(new Material(id, content));
                log.info("已加载文件 [{}], 原始文件名: {}, 文本长度: {}", id, filename, content.length());
            } catch (IOException e) {
                log.error("读取上传文件失败: {}", filename, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AnalyzeResponse.error(500, "读取文件失败: " + filename));
            }
        }

        if (materials.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(AnalyzeResponse.error(400, "所有上传文件均为空"));
        }

        AnalyzeRequest request = new AnalyzeRequest(materials, jobDescription);
        AnalyzeResponse response = service.analyze(request);
        log.info("文件上传分析请求处理完成，分析 {} 份材料", materials.size());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("处理请求时发生异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
    }
}
