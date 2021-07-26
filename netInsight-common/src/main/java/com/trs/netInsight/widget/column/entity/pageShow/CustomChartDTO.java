package com.trs.netInsight.widget.column.entity.pageShow;

import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.column.entity.CustomChart;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CustomChartDTO implements Serializable {

    private String id;
    private String name;
    private SpecialType columnType;
    private String chartType;
    private ChartPageInfo chartPage = ChartPageInfo.CustomChart;
    private Boolean isTop;
    private Integer sequence;
    private Integer topSequence;
    private Boolean hide;
    private String contrast;
    private String groupName;
    private String keyWord;
    private String keyWordIndex;
    private Boolean weight;
    private String sort;
    private String excludeWords;
    private String excludeWordsIndex;
    private String excludeWeb;
    private String monitorSite;
    private String simflag = "urlRemove";
    private String tabWidth;
    private String timeRange;
    private String trsl;
    private String xyTrsl;
    private String mediaLevel;
    private String mediaIndustry;
    private String contentIndustry;
    private String filterInfo;
    private String contentArea;
    private String mediaArea;
    private String preciseFilter;
    private String preciseXWFilter;
    private String preciseLTFilter;
    private String preciseWBFilter;
    private String pointToId;
    private void setPointToId(String mapperId){
        this.pointToId = mapperId;
    }
    private Integer index = 0;


    public CustomChartDTO() {
    }

    public CustomChartDTO(CustomChart customChart) {
        if(customChart.isWeight()&&customChart.getSort()==null){
            customChart.setSort("hittitle");
        }
        if(!customChart.isWeight()&&customChart.getSort()==null){
            customChart.setSort("desc");
        }
        this.id = customChart.getId();
        this.name = customChart.getName();
        this.columnType = customChart.getSpecialType();
        this.chartType = customChart.getType();
        this.isTop = customChart.getIsTop();
        this.sequence = customChart.getSequence();
        this.topSequence = customChart.getTopSequence();
        this.hide = customChart.isHide();
        this.contrast = customChart.getContrast();
        this.groupName = CommonListChartUtil.formatPageShowGroupName(customChart.getGroupName());
        this.keyWord = customChart.getKeyWord();
        this.keyWordIndex = customChart.getKeyWordIndex();
        this.weight = customChart.isWeight();
        this.sort = customChart.getSort();
        this.excludeWords = customChart.getExcludeWords();
        this.excludeWordsIndex = customChart.getExcludeWordIndex();
        this.excludeWeb = customChart.getExcludeWeb();
        this.monitorSite = customChart.getMonitorSite();
        //排重方式 不排 no，单一媒体排重 netRemove,站内排重 urlRemove,全网排重 sourceRemove
        if (customChart.isSimilar()) {
            this.simflag = "netRemove";
        } else if (customChart.isIrSimflag()) {
            this.simflag = "urlRemove";
        } else if (customChart.isIrSimflagAll()) {
            this.simflag = "sourceRemove";
        }else{
            this.simflag = "no";
        }
        this.tabWidth = customChart.getTabWidth();
        this.timeRange = customChart.getTimeRange();
        this.trsl = customChart.getTrsl();
        this.xyTrsl = customChart.getXyTrsl();
        this.mediaLevel = customChart.getMediaLevel();
        this.mediaIndustry = customChart.getMediaIndustry();
        this.contentIndustry = customChart.getContentIndustry();
        this.filterInfo = customChart.getFilterInfo();
        this.contentArea = customChart.getContentArea();
        this.mediaArea = customChart.getMediaArea();
        this.pointToId = customChart.getParentId();
        this.preciseFilter = customChart.getPreciseFilter();
        String[] filter = null;
        List<String> xwList = new ArrayList<>();
        List<String> ltList = new ArrayList<>();
        List<String> wbList = new ArrayList<>();
        if(StringUtil.isNotEmpty(preciseFilter)) {
            filter = customChart.getPreciseFilter().split(";");
            for (int i = 0; i < filter.length; i++) {
                if(filter[i].contains("News")){
                    xwList.add(filter[i]);
                }
                if(filter[i].contains("Luntan")){
                    ltList.add(filter[i]);
                }
                if(filter[i].contains("Weibo")){
                    wbList.add(filter[i]);
                }
            }

        }
        this.preciseXWFilter=String.join(";", xwList);
        this.preciseLTFilter=String.join(";", ltList);
        this.preciseWBFilter=String.join(";", wbList);
        addTypeSeq();
    }
    /**
     * 因为前端页面需要根据type顺序判断echart插件，来添加时间插件和回显时间，所以添加一个序号，统计分析页面不需要
     */
    private void addTypeSeq(){
        if("pieChart".equals(this.chartType)){
            this.index = 1;
        }else if("mapChart".equals(this.chartType)){
            this.index = 2;
        }else if("wordCloudChart".equals(this.chartType)){
            this.index = 3;
        }else if("barGraphChart".equals(this.chartType)){
            this.index = 4;
        }else if("brokenLineChart".equals(this.chartType)){
            this.index = 5;
        }else if("timeListInfo".equals(this.chartType)){
            this.index = 6;
        }else if("md5ListInfo".equals(this.chartType)){
            this.index = 7;
        }else if("emotionPieChart".equals(this.chartType)){
            this.index = 8;
        }else if("crossBarGraphChart".equals(this.chartType)){
            this.index = 9;
        }else if("hotTopicSort".equals(this.chartType)){
            this.index = 10;
        }
    }

}
