package com.gaocui.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 首页 AI 找货匹配请求 (游客).
 */
public class AiMatchRequest {

    @NotBlank(message = "需求不能为空")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
