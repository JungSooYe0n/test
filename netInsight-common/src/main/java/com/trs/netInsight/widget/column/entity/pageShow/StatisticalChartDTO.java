package com.trs.netInsight.widget.column.entity.pageShow;

import com.trs.netInsight.widget.column.entity.StatisticalChart;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.entity.emuns.StatisticalChartInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class StatisticalChartDTO implements Serializable {

    private String id;
    private String name;
    private ChartPageInfo chartPage = ChartPageInfo.StatisticalChart;
    private String chartType;
    private Boolean isTop;
    private Integer sequence;
    private String contrast;
    private String timeRange;
    private Integer topSequence;
    private Integer tabWidth = 50;
    private String pointToId;
    private void setPointToId(String mapperId){
        this.pointToId = mapperId;
    }

    private Integer index = 0;

    public StatisticalChartDTO() {
    }

    public StatisticalChartDTO(StatisticalChart statisticalChart, StatisticalChartInfo statisticalChartInfo, String timeRange) {
        this.id = statisticalChart.getId();
        this.name = statisticalChartInfo.getChartName();
        this.chartType = statisticalChartInfo.getChartType();
        this.isTop = statisticalChart.getIsTop();
        this.sequence = statisticalChartInfo.getSequence();
        this.contrast = statisticalChartInfo.getContrast();
        this.timeRange = timeRange;
        this.topSequence = statisticalChart.getTopSequence();

        if (StatisticalChartInfo.WORD_CLOUD.equals(statisticalChartInfo)
                || StatisticalChartInfo.MAP.equals(statisticalChartInfo)
                || StatisticalChartInfo.CHART_LINE.equals(statisticalChartInfo)) {
            this.tabWidth = 100;
        }

        addTypeSeq();
        this.pointToId = statisticalChart.getParentId();
    }

    /**
     * 因为前端页面需要根据type顺序判断echart插件，来添加时间插件和回显时间，所以添加一个序号，统计分析页面不需要
     */
    private void addTypeSeq() {
        if ("pieChart".equals(this.chartType)) {
            this.index = 1;
        } else if ("mapChart".equals(this.chartType)) {
            this.index = 2;
        } else if ("wordCloudChart".equals(this.chartType)) {
            this.index = 3;
        } else if ("barGraphChart".equals(this.chartType)) {
            this.index = 4;
        } else if ("brokenLineChart".equals(this.chartType)) {
            this.index = 5;
        } else if ("timeListInfo".equals(this.chartType)) {
            this.index = 6;
        } else if ("md5ListInfo".equals(this.chartType)) {
            this.index = 7;
        } else if ("emotionPieChart".equals(this.chartType)) {
            this.index = 8;
        } else if ("crossBarGraphChart".equals(this.chartType)) {
            this.index = 9;
        } else if ("hotTopicSort".equals(this.chartType)) {
            this.index = 10;
        }
    }
}
