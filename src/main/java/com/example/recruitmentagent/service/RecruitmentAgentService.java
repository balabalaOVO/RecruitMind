package com.example.recruitmentagent.service;

import com.example.recruitmentagent.dto.request.AnalyzeRequest;
import com.example.recruitmentagent.dto.response.AnalyzeResponse;

public interface RecruitmentAgentService {
    AnalyzeResponse analyze(AnalyzeRequest request);
}
