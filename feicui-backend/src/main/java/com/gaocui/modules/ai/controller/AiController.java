package com.gaocui.modules.ai.controller;

import com.gaocui.common.api.Result;
import com.gaocui.modules.ai.dto.AiGenerateRequest;
import com.gaocui.modules.ai.dto.AiGenerateResponse;
import com.gaocui.modules.ai.dto.AiMatchRequest;
import com.gaocui.modules.ai.dto.AiMatchResponse;
import com.gaocui.modules.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 接口:
 * 1. /home/ai/match 首页找货匹配(游客可访问).
 * 2. /merchant/products/ai-generate 发布商品图片转文案(需登录).
 */
@Tag(name = "AI", description = "找货匹配 / 图片转文案")
@RestController
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "首页AI找货匹配(游客)")
    @PostMapping("/home/ai/match")
    public Result<AiMatchResponse> match(@RequestBody @Valid AiMatchRequest req) {
        return Result.success(aiService.match(req.getMessage()));
    }

    @Operation(summary = "发布商品-AI图片转文案(需登录)")
    @PostMapping("/merchant/products/ai-generate")
    public Result<AiGenerateResponse> generate(@RequestBody @Valid AiGenerateRequest req) {
        return Result.success(aiService.generateProductInfo(req.getImages()));
    }
}
