package com.gaocui.modules.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 发布商品 - AI 文案生成请求 (传已上传的图片 URL).
 */
@Getter
@Setter
public class AiGenerateRequest {

    @NotEmpty(message = "至少需要1张图片")
    private List<String> images;
}
