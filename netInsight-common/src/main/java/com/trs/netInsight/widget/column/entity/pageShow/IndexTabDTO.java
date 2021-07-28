package com.trs.netInsight.widget.column.entity.pageShow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class IndexTabDTO implements Serializable {
    private String id;
    private String name;
    private ChartPageInfo chartPage = ChartPageInfo.TabChart;
    private String type;
    private String chartType;
    private Integer flag = ColumnFlag.IndexTabFlag.ordinal();
    private Integer flagSort;
    private Boolean show = false;
    private String groupName;
    private String contrast;
    private String timeRange;
    private String tabWidth = "50";
    private SpecialType columnType;
    private Boolean hide;
    private Boolean isMe;
    private Boolean share;
    private String keyWord;
    private String keyWordIndex;
    private Boolean weight;
    private String sort;
    private String excludeWords;
    private String excludeWordsIndex;
    private String excludeWeb;
    private String monitorSite;
    private String simflag = "urlRemove";
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
    private Boolean updateWordForm;
    private Integer wordFromNum;
    private Boolean wordFromSort;
    private Boolean topFlag;
    private String imgOcr;

    private Boolean active = false;
    public void setActive(Boolean active){
        this.active = active;
    }

    private String pointToId;
    private void setPointToId(String mapperId){
        this.pointToId = mapperId;
    }

    private Integer index = 0;

    public IndexTabDTO(){
    }

    public IndexTabDTO(IndexTabMapper mapper){
        IndexTab tab = mapper.getIndexTab();
        if(tab.isWeight()&&tab.getSort()==null){
            tab.setSort("hittitle");
        }
        if(!tab.isWeight()&&tab.getSort()==null){
            tab.setSort("desc");
        }
        this.id = mapper.getId();
        this.name = tab.getName();
        this.chartType = tab.getType();
        this.type = tab.getType();
        this.columnType = tab.getSpecialType();
        if(this.columnType == null){
            if(StringUtil.isNotEmpty(tab.getXyTrsl()) || StringUtil.isNotEmpty(tab.getTrsl())){
                this.columnType=SpecialType.SPECIAL;
            }else{
                this.columnType = SpecialType.COMMON;
            }
        }
        this.hide = mapper.isHide();
        this.isMe = mapper.isMe();
        this.share = mapper.getShare();

        this.groupName = CommonListChartUtil.formatPageShowGroupName(tab.getGroupName());
        this.contrast = tab.getContrast();
        this.timeRange = tab.getTimeRange();

        //获取词距信息
        String keywordJson = tab.getKeyWord();
        this.updateWordForm=false;
        this.wordFromNum= 0;
        this.wordFromSort=false;
        if(StringUtil.isNotEmpty(keywordJson)){
            JSONArray jsonArray = JSONArray.parseArray(keywordJson);
            //现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
            if(jsonArray!= null && jsonArray.size() ==1 ){
                Object o = jsonArray.get(0);
                JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
                String key = jsonObject.getString("keyWords");
                if(StringUtil.isNotEmpty(key) && key.contains(",")){
                    Integer wordFromNum = jsonObject.getInteger("wordSpace");
                    Boolean wordFromSort = jsonObject.getBoolean("wordOrder");
                    this.updateWordForm=true;
                    this.wordFromNum= wordFromNum;
                    this.wordFromSort=wordFromSort;
                }
            }
        }
        this.keyWord= keywordJson;
        this.keyWordIndex= tab.getKeyWordIndex();
        this.weight= tab.isWeight();
        this.sort=tab.getSort();
        this.excludeWords= tab.getExcludeWords();
        this.excludeWordsIndex=tab.getExcludeWordIndex();
        this.excludeWeb= tab.getExcludeWeb();
        this.monitorSite=tab.getMonitorSite();
        //排重方式 不排 no，单一媒体排重 netRemove,站内排重 urlRemove,全网排重 sourceRemove
        if (tab.isSimilar()) {
            this.simflag="netRemove";
        } else if (tab.isIrSimflag()) {
            this.simflag="urlRemove";
        } else if (tab.isIrSimflagAll()) {
            this.simflag= "sourceRemove";
        }else{
            this.simflag= "no";
        }
        this.tabWidth= mapper.getTabWidth();
        this.trsl= tab.getTrsl();
        this.xyTrsl= tab.getXyTrsl();

        this.mediaLevel= tab.getMediaLevel();
        this.mediaIndustry= tab.getMediaIndustry();
        this.contentIndustry= tab.getContentIndustry();
        this.filterInfo=tab.getFilterInfo();
        this.preciseFilter = tab.getPreciseFilter();
        String[] filter = null;
        List<String> xwList = new ArrayList<>();
        List<String> ltList = new ArrayList<>();
        List<String> wbList = new ArrayList<>();
        if(StringUtil.isNotEmpty(preciseFilter)) {
           filter = tab.getPreciseFilter().split(";");
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
        this.contentArea=tab.getContentArea();
        this.mediaArea=tab.getMediaArea();
        this.imgOcr=tab.getImgOcr();
        if("top".equals(mapper.getTopFlag())){
            this.topFlag = true;
        }else{
            this.topFlag =  false;
        }

        this.pointToId = mapper.getId();
        addTypeSeq();
    }
    public IndexTabDTO(IndexTabMapper mapper,Integer level){
        this(mapper);
        this.flagSort = level;

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
