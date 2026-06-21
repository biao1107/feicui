package com.gaocui.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页 AI 找货匹配请求 (游客).
 */
@Getter
@Setter
public class AiMatchRequest {

    @NotBlank(message = "需求不能为空")
    private String message;
}
