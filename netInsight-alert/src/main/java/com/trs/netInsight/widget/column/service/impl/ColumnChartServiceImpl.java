package com.trs.netInsight.widget.column.service.impl;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.column.entity.*;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.emuns.StatisticalChartInfo;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.entity.pageShow.CustomChartDTO;
import com.trs.netInsight.widget.column.entity.pageShow.IndexPageDTO;
import com.trs.netInsight.widget.column.entity.pageShow.IndexTabDTO;
import com.trs.netInsight.widget.column.entity.pageShow.StatisticalChartDTO;
import com.trs.netInsight.widget.column.repository.*;
import com.trs.netInsight.widget.column.service.IColumnChartService;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.docx4j.wml.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private IndexTabMapperRepository indexTabMapperRepository;
    @Autowired
    private IndexSequenceRepository indexSequenceRepository;

    /**
     * 获取当前栏目对应的所有图表，包括统计分析图表和自定义图表
     *
     * @param id 栏目id
     * @return
     */
    @Transactional
    @Override
    public Object getCustomChart(String id,int pageNo,int pageSize) {
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        Pageable pageable = new PageRequest(pageNo,pageSize,sort);
        Page<CustomChart> customChartPage = customChartRepository.findByParentId(id, pageable);
        List<CustomChartDTO> ccList = null;
        if (customChartPage != null && customChartPage.getContent() != null && customChartPage.getContent().size() > 0) {
            ccList = new ArrayList<>();
            for (CustomChart oneCc : customChartPage.getContent()) {
                CustomChartDTO customChartDTO = new CustomChartDTO(oneCc);
                ccList.add(customChartDTO);
            }
            return new ColumnPageBean<CustomChartDTO>(pageNo,pageSize,(int)customChartPage.getTotalElements(),ccList);
        }
        return null;
    }
    /**
     * 获取当前栏目对应的所有图表，包括统计分析图表和自定义图表
     *
     * @param id 栏目id
     * @return
     */
    @Transactional
    @Override
    public Object getStatisticalChart(String id) {
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        IndexTabMapper mapper = indexTabMapperRepository.findOne(id);
        String timeRange = mapper.getIndexTab().getTimeRange();
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentId(id, sort);
        statisticalChartList = this.initStatisticalChart(statisticalChartList,id);
        List<StatisticalChartDTO> scList = null;
        if (statisticalChartList != null && statisticalChartList.size() > 0) {
            scList = new ArrayList<>();
            for (StatisticalChart oneSc : statisticalChartList) {
                StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(oneSc.getChartType());
                StatisticalChartDTO statisticalChartDTO = new StatisticalChartDTO(oneSc,statisticalChartInfo,timeRange);
                scList.add(statisticalChartDTO);
            }
        }
        scList.stream().sorted(Comparator.comparing(StatisticalChartDTO::getSequence)).collect(Collectors.toList());
        return scList;
    }

    /**
     * 获取当前栏目对应的所有图表，包括统计分析图表和自定义图表
     *
     * @param pageId 栏目id
     * @return
     */
    @Override
    public Object getTopColumnChartForPage(String pageId,int pageNo,int pageSize) {
        if (StringUtil.isEmpty(pageId)) {
            return null;
        }
        List<Object> result = null;
        IndexPage indexPage = indexPageRepository.findOne(pageId);
//        if(ObjectUtil.isNotEmpty(indexPage)){
//            List<IndexPage> childrenPage = indexPage.getChildrenPage();
//            List<IndexTabMapper> childrenMapper = indexPage.getIndexTabMappers();
//
//            //因为这个是分组下的栏目，top的东西不会在分组下，
//            List<Object> columnList = columnService.sortColumn(new ArrayList<>(),childrenMapper,childrenPage,true,false);
//            if(columnList == null || columnList.size() ==0){
//                //当前分组下无数据，直接返回NULL
//                return result;
//            }
//            result = new ArrayList<>();
//            // 将当前分组下的栏目 和栏目对应的置顶的统计分析和自定义图表放进来
//            formatTopColumnChart(result,columnList);
//
//            return new ColumnPageBean<Object>(pageNo,pageSize,result.size(),result).subList();
//        }
        if(ObjectUtil.isNotEmpty(indexPage)){
            List<IndexPage> childrenPage = indexPage.getChildrenPage();
            List<IndexTabMapper> childrenMapper = indexPage.getIndexTabMappers();
            //因为这个是分组下的栏目，top的东西不会在分组下，
            List<Object> columnList = columnService.sortColumnAll(new ArrayList<>(),childrenMapper,childrenPage,true,false);
            if(columnList == null || columnList.size() ==0){
                //当前分组下无数据，直接返回NULL
                return columnList;
            }
            result = new ArrayList<>();
            // 将当前分组下的栏目 和栏目对应的置顶的统计分析和自定义图表放进来
            formatTopColumnChart(result,columnList,false);
//            List<IndexSequence> indexSequenceList = indexSequenceRepository.findByParentId(pageId);

            return new ColumnPageBean<Object>(pageNo,pageSize,result.size(),result).subList();
        }
        return null;
    }

    @Override
    public Object getOneTopColumnChartForPage(String pageId, int pageNo, int pageSize) {
        if (StringUtil.isEmpty(pageId)) {
            return null;
        }
        List<Object> result = null;
        IndexPage indexPage = indexPageRepository.findOne(pageId);
        if(ObjectUtil.isNotEmpty(indexPage)){
            List<IndexPage> childrenPage = indexPage.getChildrenPage();
            List<IndexTabMapper> childrenMapper = indexPage.getIndexTabMappers();
            //因为这个是分组下的栏目，top的东西不会在分组下，
            List<Object> columnList = columnService.sortColumnAll(new ArrayList<>(),childrenMapper,childrenPage,false,false);
            if(columnList == null || columnList.size() ==0){
                //当前分组下无数据，直接返回NULL
                return columnList;
            }
            result = new ArrayList<>();
            formatTopColumnChart(result,columnList,true);
//            return new ColumnPageBean<Object>(pageNo,pageSize,result.size(),result).subList();
            return result;
        }
        return null;
    }

    /**
     * 格式化 分组下豆腐块数据
     *
     * @param result     要返回的结果 - 放所有豆腐块的数据
     * @param columnList 要统计的分组下的子分组和子栏目（包含子层级的）
     * @return
     */
    private List<Object> formatTopColumnChart(List<Object> result, List<Object> columnList,boolean isOne) {
        if (columnList != null && columnList.size() > 0) {
            for (Object obj : columnList) {
                if (obj instanceof IndexTabMapper) {
                    IndexTabMapper mapper = (IndexTabMapper) obj;
                    IndexTabDTO indexTabDTO = new IndexTabDTO(mapper);
                    result.add(indexTabDTO);
//                    this.getTopColumnChartForTab(result, mapper);
                } else if (obj instanceof IndexPage) {
                    IndexPage page = (IndexPage) obj;
                    List<Object> childColumn = page.getColumnList();
                    if (childColumn != null && childColumn.size() > 0) {
                        this.formatTopColumnChart(result, childColumn,isOne);
                    }
                    if (isOne){
                        IndexPageDTO indexPageDTO = new IndexPageDTO(page);
                        result.add(indexPageDTO);
                    }
                }else {
                    result.add(obj);
                }
            }
        }
        return result;
    }

    /**
     * 因为前端页面需要根据type顺序判断echart插件，来添加时间插件和回显时间，所以添加一个序号，统计分析页面不需要
     * @param map
     * @param type
     */
    private void addTypeSeq(Map<String, Object> map,String type){
        if("pieChart".equals(type)){
            map.put("index",1);
        }else if("mapChart".equals(type)){
            map.put("index",2);
        }else if("wordCloudChart".equals(type)){
            map.put("index",3);
        }else if("barGraphChart".equals(type)){
            map.put("index",4);
        }else if("brokenLineChart".equals(type)){
            map.put("index",5);
        }else if("timeListInfo".equals(type)){
            map.put("index",6);
        }else if("md5ListInfo".equals(type)){
            map.put("index",7);
        }else if("emotionPieChart".equals(type)){
            map.put("index",8);
        }else if("crossBarGraphChart".equals(type)){
            map.put("index",9);
        }else if("hotTopicSort".equals(type)){
            map.put("index",10);
        }
    }

    private List<Object> getTopColumnChartForTab(List<Object> result,IndexTabMapper mapper){
        if (ObjectUtil.isEmpty(mapper)) {
            return result;
        }
        List<Object> columnChartList = new ArrayList<>();
        String timeRange = mapper.getIndexTab().getTimeRange();
        Sort sort = new Sort(Sort.Direction.ASC, "topSequence");
        //获取统计分析中被置顶的数据
        List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
        if (statisticalChartList != null && statisticalChartList.size() > 0) {
            for (StatisticalChart oneSc : statisticalChartList) {
                StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(oneSc.getChartType());
                StatisticalChartDTO statisticalChartDTO = new StatisticalChartDTO(oneSc,statisticalChartInfo,timeRange);
                //拿到统计分析图的基本数据
                columnChartList.add(statisticalChartDTO);
            }
        }
        //获取当前栏目被置顶的自定义图表
        List<CustomChart> customChartList = customChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
        if (customChartList != null && customChartList.size() > 0) {
            for (CustomChart oneCc : customChartList) {
                CustomChartDTO customChartDTO = new CustomChartDTO(oneCc);
                columnChartList.add(customChartDTO);
            }
        }
        if(columnChartList.size() ==0 ){
            return result;
        }
        result.addAll(columnChartList);
        return result;
    }



    /**
     * 获取当前栏目下最大置顶排序值
     * @param id
     * @return
     */
    @Override
    public Integer getMaxTopSequence(String id) {
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "topSequence");

        Integer seq = 0;
        List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentIdAndIsTop(id, true, sort);
        if (statisticalChartList != null && statisticalChartList.size() > 0) {
            seq = statisticalChartList.get(statisticalChartList.size() - 1).getTopSequence();
        }
        if(seq == null ){
            seq = 0;
        }
        List<CustomChart> customChartList = customChartRepository.findByParentIdAndIsTop(id, true, sort);
        if (customChartList != null && customChartList.size() > 0) {
            Integer newSeq = customChartList.get(customChartList.size() - 1).getTopSequence();
            if (newSeq != null && newSeq > seq) {
                seq = newSeq;
            }
        }
        return seq == null ? 0:seq;

    }


    /**
     * 初始化统计图表信息
     *
     * @return
     */
    @Transactional
    @Override
    public List<StatisticalChart> initStatisticalChart(List<StatisticalChart> statisticalChartList, String indexTabId) {
        if(statisticalChartList == null){
            statisticalChartList = new ArrayList<>();
        }
        if(statisticalChartList.size() < StatisticalChartInfo.values().length){
            Set<String> chartTypeSet = new HashSet<>();
            if(statisticalChartList.size() >0 ){
                for(StatisticalChart statisticalChart :statisticalChartList){
                    chartTypeSet.add(statisticalChart.getChartType());
                }
            }

            for (StatisticalChartInfo statisticalChartInfo : StatisticalChartInfo.values()) {
                if( !chartTypeSet.contains(statisticalChartInfo.getChartType())){
                    StatisticalChart statisticalChart = new StatisticalChart(statisticalChartInfo.getChartType(),
                            statisticalChartInfo.getSequence(), false, statisticalChartInfo.getChartName(), indexTabId);
                    statisticalChart = statisticalChartRepository.save(statisticalChart);
                    statisticalChartList.add(statisticalChart);
                }
            }
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
        indexSequenceRepository.delete(indexSequenceRepository.findByIndexId(id));
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
