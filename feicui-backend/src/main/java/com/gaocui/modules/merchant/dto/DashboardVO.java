package com.gaocui.modules.merchant.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 商家后台数据面板.
 */
@Getter
@Setter
public class DashboardVO {

    /** 已上架商品数 */
    private Long listedCount;
    /** 发布上限(FREE 20 / VIP 100) */
    private Integer productLimit;
    /** 今日客资数 */
    private Long todayLeads;
    /** 累计客资数 */
    private Long totalLeads;


}
