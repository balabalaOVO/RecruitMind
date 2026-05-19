package com.example.recruitmentagent.dto.response;

import java.util.List;

public record AnalysisResult(
    List<String> tags,
    Integer matchScore,
    List<String> risks,
    String nextAction,
    List<String> suggestedQuestions
) {}
