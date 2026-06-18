package com.gaocui.modules.lead.controller;

import com.gaocui.common.api.PageResult;
import com.gaocui.common.api.Result;
import com.gaocui.modules.lead.dto.LeadDetailVO;
import com.gaocui.modules.lead.dto.LeadListItemVO;
import com.gaocui.modules.lead.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商家客资管理接口 (需登录).
 * VIP 可看完整买家邮箱, 免费商家自动脱敏.
 */
@Tag(name = "客资管理", description = "客资列表/详情/联系状态")
@RestController
@RequestMapping("/merchant/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @Operation(summary = "客资列表(按状态过滤, 分页; 免费邮箱脱敏)")
    @GetMapping
    public Result<PageResult<LeadListItemVO>> page(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(leadService.page(current, size, status));
    }

    @Operation(summary = "客资详情(免费商家邮箱脱敏)")
    @GetMapping("/{id}")
    public Result<LeadDetailVO> detail(@PathVariable Long id) {
        return Result.success(leadService.detail(id));
    }

    @Operation(summary = "标记客资为已联系")
    @PutMapping("/{id}/contacted")
    public Result<Void> markContacted(@PathVariable Long id) {
        leadService.markContacted(id);
        return Result.success();
    }
}
