package com.gaocui.modules.ai.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 发布商品 - AI 文案生成请求 (传已上传的图片 URL).
 */
public class AiGenerateRequest {

    @NotEmpty(message = "至少需要1张图片")
    private List<String> images;

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
