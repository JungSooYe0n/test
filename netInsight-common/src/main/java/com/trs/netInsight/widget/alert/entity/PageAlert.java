package com.trs.netInsight.widget.alert.entity;

import com.trs.netInsight.support.fts.entity.FtsDocumentAlert;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * 只是为了 保留之前的返回格式，免得前端再做二次修改
 *
 */
@Getter
@Setter
public class PageAlert {

    private List<FtsDocumentAlert> content;//存储结果集

    private boolean first;//是不是第一页

    private boolean last;//是不是最后一页

    private String pageId;//每一页的唯一标示

    private int size;//一页几条 也就是pageSize

    private int totalElements;//一共几条

    private int totalPages;//一共几页

    private int number;//从0开始 第几页  也就是pageNo

}
