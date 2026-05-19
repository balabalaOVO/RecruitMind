package com.example.recruitmentagent.dto.response;

import java.util.List;

public record AnalyzeResponse(
    int code,
    String message,
    AnalyzeData data
) {
    public record AnalyzeData(
        String jobDescription,
        int totalAnalyzed,
        List<ResultItem> results
    ) {}

    public record ResultItem(
        String inputId,
        AnalysisResult analysis
    ) {}

    public static AnalyzeResponse success(String jobDescription, List<ResultItem> results) {
        return new AnalyzeResponse(200, "success", new AnalyzeData(jobDescription, results.size(), results));
    }

    public static AnalyzeResponse error(int code, String message) {
        return new AnalyzeResponse(code, message, null);
    }
}
