package com.gaocui.modules.product.controller;

import com.gaocui.common.api.PageResult;
import com.gaocui.common.api.Result;
import com.gaocui.modules.product.dto.ProductDetailVO;
import com.gaocui.modules.product.dto.ProductListItemVO;
import com.gaocui.modules.product.dto.ProductSaveRequest;
import com.gaocui.modules.product.dto.ProductStatusRequest;
import com.gaocui.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 商家商品管理接口 (需登录).
 * 状态机: DRAFT 草稿 → LISTED 已上架 ↔ DELISTED 已下架.
 */
@Tag(name = "商品管理", description = "商家发布/编辑/上下架/删除商品")
@RestController
@RequestMapping("/merchant/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "上传商品图片, 返回可访问 URL")
    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(Map.of("url", productService.uploadImage(file)));
    }

    @Operation(summary = "创建商品草稿")
    @PostMapping
    public Result<Map<String, Long>> create(@RequestBody @Valid ProductSaveRequest req) {
        return Result.success(Map.of("id", productService.createDraft(req)));
    }

    @Operation(summary = "编辑商品(商品管理-编辑)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ProductSaveRequest req) {
        productService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "发布商品(草稿→上架, 校验额度)")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        productService.publish(id);
        return Result.success();
    }

    @Operation(summary = "切换上下架状态")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody @Valid ProductStatusRequest req) {
        productService.changeStatus(id, req.getTargetStatus());
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    @Operation(summary = "商品管理列表(按状态过滤, 分页)")
    @GetMapping
    public Result<PageResult<ProductListItemVO>> page(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(productService.page(current, size, status));
    }

    @Operation(summary = "商品详情(商家自己)")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }
}
