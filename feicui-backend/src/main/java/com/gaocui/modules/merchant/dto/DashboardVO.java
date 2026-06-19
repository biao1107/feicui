package com.gaocui.modules.merchant.dto;

/**
 * 商家后台数据面板.
 */
public class DashboardVO {

    /** 已上架商品数 */
    private Long listedCount;
    /** 发布上限(FREE 20 / VIP 100) */
    private Integer productLimit;
    /** 今日客资数 */
    private Long todayLeads;
    /** 累计客资数 */
    private Long totalLeads;

    public Long getListedCount() {
        return listedCount;
    }

    public void setListedCount(Long listedCount) {
        this.listedCount = listedCount;
    }

    public Integer getProductLimit() {
        return productLimit;
    }

    public void setProductLimit(Integer productLimit) {
        this.productLimit = productLimit;
    }

    public Long getTodayLeads() {
        return todayLeads;
    }

    public void setTodayLeads(Long todayLeads) {
        this.todayLeads = todayLeads;
    }

    public Long getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(Long totalLeads) {
        this.totalLeads = totalLeads;
    }
}
