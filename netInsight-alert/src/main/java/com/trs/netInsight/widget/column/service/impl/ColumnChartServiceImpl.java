package com.trs.netInsight.widget.column.service.impl;

import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.column.entity.CustomChart;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.StatisticalChart;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.emuns.StatisticalChartInfo;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.CustomChartRepository;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.repository.StatisticalChartRepository;
import com.trs.netInsight.widget.column.service.IColumnChartService;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.wml.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 日常监测栏目对应的图表实现类
 */
@Service
@Slf4j
@Transactional
public class ColumnChartServiceImpl implements IColumnChartService {

    @Autowired
    private StatisticalChartRepository statisticalChartRepository;
    @Autowired
    private CustomChartRepository customChartRepository;

    @Autowired
    private IndexPageRepository indexPageRepository;
    @Autowired
    private IColumnService columnService;

    @Autowired
    private IndexTabRepository indexTabRepository;

    /**
     * 获取当前栏目对应的所有图表，包括统计分析图表和自定义图表
     *
     * @param id 栏目id
     * @return
     */
    @Transactional
    @Override
    public Object getColumnChart(String id) {
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        Map<String, Object> map = new HashMap<>();
        map.put("statisticalChart", null);
        map.put("customChart", null);
        List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentId(id, sort);
        if (statisticalChartList == null || statisticalChartList.size() == 0) {
            statisticalChartList = this.initStatisticalChart(id);
        }
        List<Object> scList = null;
        if (statisticalChartList != null && statisticalChartList.size() > 0) {
            scList = new ArrayList<>();
            for (StatisticalChart oneSc : statisticalChartList) {
                Map<String, Object> oneScInfo = new HashMap<>();
                StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(oneSc.getChartType());
                oneScInfo.put("id", oneSc.getId());
                oneScInfo.put("name", statisticalChartInfo.getChartName());
                oneScInfo.put("chartPage", ChartPageInfo.StatisticalChart);
                oneScInfo.put("chartType", statisticalChartInfo.getChartType());
                oneScInfo.put("isTop", oneSc.getIsTop());
                oneScInfo.put("sequence", statisticalChartInfo.getSequence());
                scList.add(oneScInfo);
            }
        }
        map.put("statisticalChart", scList);
        List<CustomChart> customChartList = customChartRepository.findByParentId(id, sort);
        List<Object> ccList = null;
        if (customChartList != null && customChartList.size() > 0) {
            ccList = new ArrayList<>();
            for (CustomChart oneCc : customChartList) {
                Map<String, Object> oneCcInfo = new HashMap<>();
                oneCcInfo.put("id", oneCc.getId());
                oneCcInfo.put("name", oneCc.getName());
                oneCcInfo.put("columnType", oneCc.getSpecialType());
                oneCcInfo.put("chartType", oneCc.getType());
                oneCcInfo.put("chartPage", ChartPageInfo.CustomChart);
                oneCcInfo.put("isTop", oneCc.getIsTop());
                oneCcInfo.put("sequence", oneCc.getSequence());
                oneCcInfo.put("hide", oneCc.isHide());
                oneCcInfo.put("contrast", oneCc.getContrast());
                oneCcInfo.put("groupName", CommonListChartUtil.formatPageShowGroupName(oneCc.getGroupName()));
                oneCcInfo.put("keyWord", oneCc.getKeyWord());
                oneCcInfo.put("keyWordIndex", oneCc.getKeyWordIndex());
                oneCcInfo.put("weight", oneCc.isWeight());
                oneCcInfo.put("excludeWords", oneCc.getExcludeWords());
                oneCcInfo.put("excludeWeb", oneCc.getExcludeWeb());
                //排重方式 不排 no，单一媒体排重 netRemove,站内排重 urlRemove,全网排重 sourceRemove
                if (oneCc.isSimilar()) {
                    oneCcInfo.put("simflag", "netRemove");
                } else if (oneCc.isIrSimflag()) {
                    oneCcInfo.put("simflag", "urlRemove");
                } else if (oneCc.isIrSimflagAll()) {
                    oneCcInfo.put("simflag", "sourceRemove");
                } else {
                    oneCcInfo.put("simflag", "no");
                }
                oneCcInfo.put("tabWidth", oneCc.getTabWidth());
                oneCcInfo.put("timeRange", oneCc.getTimeRange());
                oneCcInfo.put("trsl", oneCc.getTrsl());
                oneCcInfo.put("xyTrsl", oneCc.getXyTrsl());
                ccList.add(oneCcInfo);
            }
        }
        map.put("customChart", ccList);
        return map;
    }

    /**
     * 获取当前栏目对应的所有图表，包括统计分析图表和自定义图表
     *
     * @param pageId 栏目id
     * @return
     */
    @Override
    public Object getTopColumnChartForPage(String pageId) {
        if (StringUtil.isEmpty(pageId)) {
            return null;
        }
        List<Object> result = null;
        IndexPage indexPage = indexPageRepository.findOne(pageId);
        if(ObjectUtil.isNotEmpty(indexPage)){
            List<IndexPage> childrenPage = indexPage.getChildrenPage();
            List<IndexTabMapper> childrenMapper = indexPage.getIndexTabMappers();
            List<Object> columnList = columnService.sortColumn(childrenMapper,childrenPage,true,false);
            if(columnList == null || columnList.size() ==0){
                //当前分组下无数据，直接返回NULL
                return result;
            }
            result = new ArrayList<>();
            // 将当前分组下的栏目 和栏目对应的置顶的统计分析和自定义图表放进来
            formatTopColumnChart(result,columnList);
        }
        return result;
    }

    /**
     * 格式化 分组下豆腐块数据
     *
     * @param result     要返回的结果 - 放所有豆腐块的数据
     * @param columnList 要统计的分组下的子分组和子栏目（包含子层级的）
     * @return
     */
    private List<Object> formatTopColumnChart(List<Object> result, List<Object> columnList) {
        if (columnList != null && columnList.size() > 0) {
            Map<String, Object> oneColumnChart = null;
            for (Object obj : columnList) {
                if (obj instanceof IndexTabMapper) {
                    IndexTabMapper mapper = (IndexTabMapper) obj;
                    if (!mapper.isHide()) {
                        oneColumnChart = new HashMap<>();
                        IndexTab tab = mapper.getIndexTab();
                        oneColumnChart.put("id", mapper.getId());
                        oneColumnChart.put("name", tab.getName());
                        oneColumnChart.put("chartType", tab.getType());
                        oneColumnChart.put("tabWidth", mapper.getTabWidth());
                        oneColumnChart.put("timeRange", tab.getTimeRange());
                        oneColumnChart.put("chartPage", ChartPageInfo.TabChart);
                        oneColumnChart.put("groupName", CommonListChartUtil.formatPageShowGroupName(tab.getGroupName()));
                        result.add(oneColumnChart);
                        this.getTopColumnChartForTab(result,mapper);
                    }
                } else if (obj instanceof IndexPage) {
                    IndexPage page = (IndexPage) obj;
                    if (!page.isHide()) {
                        List<Object> childColumn = page.getColumnList();
                        if (childColumn != null && childColumn.size() > 0) {
                            this.formatTopColumnChart(result, childColumn);
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<Object> getTopColumnChartForTab(List<Object> result,IndexTabMapper mapper){
        if (ObjectUtil.isEmpty(mapper)) {
            return result;
        }
        List<Map<String,Object>> columnChartList = new ArrayList<>();
        String timeRange = mapper.getIndexTab().getTimeRange();
        Sort sort = new Sort(Sort.Direction.ASC, "topSequence");
        //获取统计分析中被置顶的数据
        List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
        if (statisticalChartList != null && statisticalChartList.size() > 0) {
            for (StatisticalChart oneSc : statisticalChartList) {
                Map<String, Object> oneScInfo = new HashMap<>();
                StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(oneSc.getChartType());
                oneScInfo.put("id", oneSc.getId());
                oneScInfo.put("name", statisticalChartInfo.getChartName());
                oneScInfo.put("columnType", null);
                oneScInfo.put("chartPage", ChartPageInfo.StatisticalChart);
                oneScInfo.put("chartType", statisticalChartInfo.getChartType());
                oneScInfo.put("isTop", oneSc.getIsTop());
                oneScInfo.put("timeRange", timeRange);
                oneScInfo.put("topSequence", oneSc.getTopSequence());
                //拿到统计分析图的基本数据
                columnChartList.add(oneScInfo);
            }
        }
        //获取当前栏目被置顶的自定义图表
        List<CustomChart> customChartList = customChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
        if (customChartList != null && customChartList.size() > 0) {
            for (CustomChart oneCc : customChartList) {
                if(!oneCc.isHide()){
                    Map<String, Object> oneCcInfo = new HashMap<>();
                    oneCcInfo.put("id", oneCc.getId());
                    oneCcInfo.put("name", oneCc.getName());
                    oneCcInfo.put("columnType", oneCc.getSpecialType());
                    oneCcInfo.put("chartType", oneCc.getType());
                    oneCcInfo.put("chartPage", ChartPageInfo.CustomChart);
                    oneCcInfo.put("isTop", oneCc.getIsTop());
                    oneCcInfo.put("topSequence", oneCc.getTopSequence());
                    oneCcInfo.put("tabWidth", oneCc.getTabWidth());
                    oneCcInfo.put("timeRange", oneCc.getTimeRange());
                    oneCcInfo.put("groupName", CommonListChartUtil.formatPageShowGroupName(oneCc.getGroupName()));
                    //拿到基本数据
                    columnChartList.add(oneCcInfo);
                }
            }
        }
        if(columnChartList.size() ==0 ){
            return result;
        }
        //根据置顶顺序进行排序
        Collections.sort(columnChartList, (o1, o2) -> {
            Integer seq1 = (Integer) o1.get("topSequence");
            Integer seq2 = (Integer) o2.get("topSequence");
            return seq1.compareTo(seq2);
        });
        for(Map<String,Object> oneChart: columnChartList){
            result.add(oneChart);
        }
        return result;
    }



    /**
     * 获取当前栏目下最大置顶排序值
     * @param id
     * @return
     */
    @Override
    public Integer getMaxTopSequence(String id) {
        /*
        Collections.sort(sortList, (o1, o2) -> {
				Integer seq1 = (Integer) o1.get("sequence");
				Integer seq2 = (Integer) o2.get("sequence");
				return seq1.compareTo(seq2);
			});
         */
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "topSequence");

        Integer seq = 0;
        List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentIdAndIsTop(id, true, sort);
        if (statisticalChartList != null && statisticalChartList.size() > 0) {
            seq = statisticalChartList.get(statisticalChartList.size() - 1).getTopSequence();
        }
        List<CustomChart> customChartList = customChartRepository.findByParentIdAndIsTop(id, true, sort);
        if (customChartList != null && customChartList.size() > 0) {
            Integer newSeq = customChartList.get(customChartList.size() - 1).getTopSequence();
            if (newSeq > seq) {
                seq = newSeq;
            }
        }
        return seq;

    }


    /**
     * 初始化统计图表信息
     *
     * @param id  栏目id
     * @return
     */
    @Transactional
    @Override
    public List<StatisticalChart> initStatisticalChart(String id) {
        List<StatisticalChart> statisticalChartList = new ArrayList<>();
        for (StatisticalChartInfo statisticalChartInfo : StatisticalChartInfo.values()) {
            StatisticalChart statisticalChart = new StatisticalChart(statisticalChartInfo.getChartType(),
                    statisticalChartInfo.getSequence(), false, statisticalChartInfo.getChartName(), id);
            statisticalChart = statisticalChartRepository.save(statisticalChart);
            statisticalChartList.add(statisticalChart);
        }
        return statisticalChartList;
    }

    /**
     * 获取一个统计分析图表
     * @param id
     * @return
     */
    @Override
    public StatisticalChart findOneStatisticalChart(String id) {
        return statisticalChartRepository.findOne(id);
    }

    /**
     * 获取一个自定义图表
     * @param id
     * @return
     */
    @Override
    public CustomChart findOneCustomChart(String id) {
        return customChartRepository.findOne(id);
    }

    /**
     * 获取当前栏目下自定义图表个数
     * @param id 栏目Id
     * @return
     */
    @Override
    public Long countForTabid(String id) {
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        Long count = customChartRepository.countAllByParentId(id);
        return count;
    }
    /**
     * 获取自定义图表个数
     * @param tabId
     * @return
     */
    @Override
    public Integer getMaxCustomChartSeq(String tabId) {
        Integer seq = 0;
        if (StringUtil.isEmpty(tabId)) {
            return seq;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        List<CustomChart> list = customChartRepository.findByParentId(tabId,sort);
        if (list != null && list.size() > 0) {
            for(CustomChart customChart:list){
                if(customChart.getSequence() >seq){
                    seq = customChart.getSequence();
                }
            }
        }
        return seq;
    }

    /**
     * 存储一个自定义图表
     * @param customChart
     * @return
     */
    @Override
    @Transactional
    public CustomChart saveCustomChart(CustomChart customChart) {
        customChart = customChartRepository.save(customChart);
        return customChart;
    }

    /**
     * 存储一个统计分析分析图表
     * @param statisticalChart
     * @return
     */
    @Override
    @Transactional
    public StatisticalChart saveStatisticalChart(StatisticalChart statisticalChart) {
        statisticalChart = statisticalChartRepository.save(statisticalChart);
        return statisticalChart;
    }

    /**
     * 删除一个自定义图表
     * @param id
     */
    @Transactional
    @Override
    public void deleteCustomChart(String id) {
        CustomChart customChart = customChartRepository.findOne(id);
        //删除之前需要对原来的栏目进行排序
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        List<CustomChart> list = customChartRepository.findByParentId(customChart.getParentId(), sort);
        int i = 1;
        for (CustomChart seq : list) {
            if (!seq.getId().equals(customChart.getId())) {
                seq.setSequence(i);
                customChartRepository.save(seq);
                i++;
            }
        }
        customChartRepository.delete(id);
    }

    /**
     * 删除栏目对应的 统计分析图和自定义图表
     * @param id
     * @return
     */
    @Transactional
    @Override
    public Integer deleteCustomChartForTabMapper(String id) {
        Integer deleteCount = 0;
        List<CustomChart> customChartList = customChartRepository.findByParentId(id);
        if(customChartList != null && customChartList.size() >0){
            customChartRepository.delete(customChartList);
            deleteCount += customChartList.size();
        }

        List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentId(id);
        if(statisticalChartList != null && statisticalChartList.size() >0){
            statisticalChartRepository.delete(statisticalChartList);
            deleteCount += statisticalChartList.size();
        }
        return deleteCount;
    }

    @Transactional
    @Override
    public Object moveCustomChart(List<CustomChart> customChartList){
        int seq = 1;
        for(CustomChart one :customChartList){
            one.setSequence(seq);
            seq++;
        }
        customChartRepository.save(customChartList);
        return customChartList;
    }

    @Transactional
    @Override
    public Object addColumnType(){
        try{
            List<IndexTab> tabList = indexTabRepository.findAll();
            if(tabList != null && tabList.size() >0) {
                for(IndexTab indexTab :tabList){
                    if(StringUtil.isNotEmpty(indexTab.getTrsl())){
                        indexTab.setSpecialType(SpecialType.SPECIAL);
                    }else{
                        indexTab.setSpecialType(SpecialType.COMMON);
                    }
                    indexTabRepository.save(indexTab);
                }
                indexTabRepository.flush();;
            }
            List<CustomChart> customChartList = customChartRepository.findAll();
            if(customChartList != null && customChartList.size() >0) {
                for(CustomChart chart :customChartList){
                    if(StringUtil.isNotEmpty(chart.getTrsl())){
                        chart.setSpecialType(SpecialType.SPECIAL);
                    }else{
                        chart.setSpecialType(SpecialType.COMMON);
                    }
                    customChartRepository.save(chart);
                }
                customChartRepository.flush();
            }

            return "没毛病，你就放心吧";
        } catch (Exception e) {
            return "修改失败了哦" + e.getMessage();
        }
    }

}
