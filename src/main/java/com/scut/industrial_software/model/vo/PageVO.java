package com.scut.industrial_software.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class PageVO<T> {
    // 当前页码
    private Integer pageNum;
    // 每页大小
    private Integer pageSize;
    // 总记录数
    private Long total;
    // 总页数
    private Integer pages;
    // 数据列表
    private List<T> records;

    // 静态构建方法
    public static <T> PageVO<T> build(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setRecords(records);
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setPages((int) Math.ceil((double) total / pageSize));
        return pageVO;
    }
}