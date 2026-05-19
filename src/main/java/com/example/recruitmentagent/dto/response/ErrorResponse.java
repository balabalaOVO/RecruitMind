package com.example.recruitmentagent.dto.response;

public record ErrorResponse(
    int code,
    String message,
    String detail
) {}
