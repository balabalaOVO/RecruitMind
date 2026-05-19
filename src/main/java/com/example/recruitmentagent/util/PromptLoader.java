package com.example.recruitmentagent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptLoader {

    private static final Logger log = LoggerFactory.getLogger(PromptLoader.class);

    public String load(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            log.debug("成功加载提示词模板: {}", path);
            return content;
        } catch (IOException e) {
            log.error("加载提示词模板失败: {}", path, e);
            throw new RuntimeException("无法加载提示词模板: " + path, e);
        }
    }
}
