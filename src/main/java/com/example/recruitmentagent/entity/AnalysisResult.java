package com.example.recruitmentagent.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_result")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private AnalysisSession session;

    @Column(name = "input_id", length = 100)
    private String inputId;

    @Column(name = "material_text", columnDefinition = "TEXT")
    private String materialText;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "next_action", length = 50)
    private String nextAction;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "risks", columnDefinition = "TEXT")
    private String risks;

    @Column(name = "suggested_questions", columnDefinition = "TEXT")
    private String suggestedQuestions;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AnalysisResult() {}

    public AnalysisResult(String inputId, String materialText, Integer matchScore,
                          String nextAction, String tags, String risks,
                          String suggestedQuestions, String errorMessage) {
        this.inputId = inputId;
        this.materialText = materialText;
        this.matchScore = matchScore;
        this.nextAction = nextAction;
        this.tags = tags;
        this.risks = risks;
        this.suggestedQuestions = suggestedQuestions;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnalysisSession getSession() { return session; }
    public void setSession(AnalysisSession session) { this.session = session; }
    public String getInputId() { return inputId; }
    public void setInputId(String inputId) { this.inputId = inputId; }
    public String getMaterialText() { return materialText; }
    public void setMaterialText(String materialText) { this.materialText = materialText; }
    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getRisks() { return risks; }
    public void setRisks(String risks) { this.risks = risks; }
    public String getSuggestedQuestions() { return suggestedQuestions; }
    public void setSuggestedQuestions(String suggestedQuestions) { this.suggestedQuestions = suggestedQuestions; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
