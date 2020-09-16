package com.trs.netInsight.widget.alert.entity;

import lombok.Getter;
import lombok.Setter;
import com.trs.netInsight.support.fts.entity.FtsDocumentAlertType;
import java.util.List;

/**
 * 为app端查询页面分页 单写一个实体
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/24 15:04.
 * @desc
 */
@Getter
@Setter
public class PageAlertSend {
    private List<FtsDocumentAlertType> content;//存储结果集

    private boolean first;//是不是第一页

    private boolean last;//是不是最后一页

    private String pageId;//每一页的唯一标示

    private int size;//一页几条 也就是pageSize

    private int totalElements;//一共几条

    private int totalPages;//一共几页

    private int number;//从0开始 第几页  也就是pageNo

    private String appDetail;

}
