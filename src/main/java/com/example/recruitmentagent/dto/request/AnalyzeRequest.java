package com.example.recruitmentagent.dto.request;

import java.util.List;

public record AnalyzeRequest(
    List<Material> materials,
    String jobDescription
) {
    public record Material(String id, String text) {}
}
