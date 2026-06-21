package com.gaocui.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装. 直接接收 MyBatis-Plus 的 IPage 转换.
 */
@Getter
@Setter
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前页 */
    private long current;
    /** 每页条数 */
    private long size;
    /** 总记录数 */
    private long total;
    /** 总页数 */
    private long pages;
    /** 数据列表 */
    private List<T> records;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        r.setTotal(page.getTotal());
        r.setPages(page.getPages());
        r.setRecords(page.getRecords());
        return r;
    }

    public static <S, T> PageResult<T> of(IPage<S> page, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        r.setTotal(page.getTotal());
        r.setPages(page.getPages());
        r.setRecords(records);
        return r;
    }
}
