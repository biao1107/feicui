package com.gaocui.modules.home.controller;

import com.gaocui.common.api.Result;
import com.gaocui.modules.lead.dto.LeadSubmitRequest;
import com.gaocui.modules.lead.service.LeadService;
import com.gaocui.modules.product.dto.ProductDetailVO;
import com.gaocui.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 游客(首页/商品详情)接口, 无需登录.
 * AI 找货匹配接口 (/home/ai/match) 在 Phase 5 接入.
 */
@Tag(name = "游客-首页", description = "商品详情等公开接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {

    private final ProductService productService;
    private final LeadService leadService;

    @Operation(summary = "商品详情(游客, 仅已上架)")
    @GetMapping("/products/{id}")
    public Result<ProductDetailVO> productDetail(@PathVariable Long id) {
        return Result.success(productService.homeDetail(id));
    }

    @Operation(summary = "提交客资(联系卖家, 留邮箱留言)")
    @PostMapping("/products/{id}/lead")
    public Result<Map<String, Long>> submitLead(@PathVariable Long id, @RequestBody @Valid LeadSubmitRequest req) {
        return Result.success(Map.of("id", leadService.submit(id, req)));
    }
}
