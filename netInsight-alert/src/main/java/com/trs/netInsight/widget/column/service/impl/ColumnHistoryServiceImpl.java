package com.trs.netInsight.widget.column.service.impl;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.column.entity.CustomChart;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.repository.CustomChartRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.service.IColumnHistoryService;
import com.trs.netInsight.widget.special.entity.SpecialCustomChart;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.repository.SpecialCustomChartRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import edu.stanford.nlp.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ColumnHistoryServiceImpl implements IColumnHistoryService {

    @Autowired
    private IndexTabRepository indexTabRepository;
    @Autowired
    private CustomChartRepository customChartRepository;
    @Autowired
    private SpecialCustomChartRepository specialCustomChartRepository;
    @Autowired
    private SpecialProjectRepository specialProjectRepository;

    @Transactional
    public Object updateAllFilterInfo(){
        //需要修改的部分为： 如果有其他其它 替换为空串，如果是 股票推介、游戏广告 改为 股票信息、游戏信息，全部在+一个假证假票
        StringBuilder sb = new StringBuilder("修改库中的信息过滤字段。\n");
        List<IndexTab> tabList = indexTabRepository.findAll();
        if(tabList != null && tabList.size() >0 ){
            sb.append("日常监测栏目共有："+tabList.size()+"条。\n");
            int count = 0;
            for(IndexTab tab : tabList){
                try{

                    String filterInfo = tab.getFilterInfo();
                    if(StringUtil.isNotEmpty(filterInfo) && !Const.NOT_FILTER_INFO.equals(filterInfo)){ // 专家模式下这个字段是空或者是全部  空不需要改
                        //如果是不过滤不需要改
                        String[] valueArr = filterInfo.split(";");
                        Set<String> valueArrList = new HashSet<>();
                        for(String v : valueArr){
                            if ("股票推荐".equals(v)) {
                                v = "股票信息";
                            }
                            if ("游戏广告".equals(v)) {
                                v = "游戏信息";
                            }
                            if(Const.FILTER_INFO.contains(v)){
                                valueArrList.add(v);
                            }
                        }
                        if(valueArrList.size() == Const.FILTER_INFO.size() -1){
                            valueArrList.add("假证假票");
                        }
                        filterInfo = StringUtils.join(valueArrList,";");
                        indexTabRepository.saveFilterInfo(filterInfo,tab.getId());
                    }

                    count++;
                }catch (Exception e){

                }
            }
            sb.append("日常监测修改成功："+count+"条。\n");

        }

        List<CustomChart> customChartList = customChartRepository.findAll();
        if(customChartList != null && customChartList.size() >0 ){
            sb.append("日常监测自定义图表共有："+customChartList.size()+"条。。\n");
            int count = 0;
            for(CustomChart customChart : customChartList){
                try{

                    String filterInfo = customChart.getFilterInfo();
                    if(StringUtil.isNotEmpty(filterInfo) && !Const.NOT_FILTER_INFO.equals(filterInfo)){ // 专家模式下这个字段是空或者是全部  空不需要改
                        //如果是不过滤不需要改
                        String[] valueArr = filterInfo.split(";");
                        Set<String> valueArrList = new HashSet<>();
                        for(String v : valueArr){
                            if ("股票推荐".equals(v)) {
                                v = "股票信息";
                            }
                            if ("游戏广告".equals(v)) {
                                v = "游戏信息";
                            }
                            if(Const.FILTER_INFO.contains(v)){
                                valueArrList.add(v);
                            }
                        }
                        if(valueArrList.size() == Const.FILTER_INFO.size() -1){
                            valueArrList.add("假证假票");
                        }
                        filterInfo = StringUtils.join(valueArrList,";");
                        customChartRepository.saveFilterInfo(filterInfo,customChart.getId());
                    }

                    count++;
                }catch (Exception e){

                }
            }
            sb.append("日常监测自定义图表修改成功："+count+"条。\n");
        }

        List<SpecialProject> specialProjectList = specialProjectRepository.findAll();
        if(specialProjectList != null && specialProjectList.size() >0 ){
            sb.append("专题分析栏目共有："+specialProjectList.size()+"条。。\n");
            int count = 0;
            for(SpecialProject specialProject : specialProjectList){
                try{

                    String filterInfo = specialProject.getFilterInfo();
                    if(StringUtil.isNotEmpty(filterInfo) && !Const.NOT_FILTER_INFO.equals(filterInfo)){ // 专家模式下这个字段是空或者是全部  空不需要改
                        //如果是不过滤不需要改
                        String[] valueArr = filterInfo.split(";");
                        Set<String> valueArrList = new HashSet<>();
                        for(String v : valueArr){
                            if ("股票推荐".equals(v)) {
                                v = "股票信息";
                            }
                            if ("游戏广告".equals(v)) {
                                v = "游戏信息";
                            }
                            if(Const.FILTER_INFO.contains(v)){
                                valueArrList.add(v);
                            }
                        }
                        if(valueArrList.size() == Const.FILTER_INFO.size() -1){
                            valueArrList.add("假证假票");
                        }
                        filterInfo = StringUtils.join(valueArrList,";");
                        specialProjectRepository.saveFilterInfo(filterInfo,specialProject.getId());
                    }

                    count++;
                }catch (Exception e){

                }
            }
            sb.append("专题分析栏目修改成功："+count+"条。\n");
        }

        List<SpecialCustomChart> specialCustomChartList = specialCustomChartRepository.findAll();
        if(specialCustomChartList != null && specialCustomChartList.size() >0 ){
            sb.append("专题分析自定义图表共有："+specialCustomChartList.size()+"条。。\n");
            int count = 0;
            for(SpecialCustomChart customChart : specialCustomChartList){
                try{

                    String filterInfo = customChart.getFilterInfo();
                    if(StringUtil.isNotEmpty(filterInfo) && !Const.NOT_FILTER_INFO.equals(filterInfo)){ // 专家模式下这个字段是空或者是全部  空不需要改
                        //如果是不过滤不需要改
                        String[] valueArr = filterInfo.split(";");
                        Set<String> valueArrList = new HashSet<>();
                        for(String v : valueArr){
                            if ("股票推荐".equals(v)) {
                                v = "股票信息";
                            }
                            if ("游戏广告".equals(v)) {
                                v = "游戏信息";
                            }
                            if(Const.FILTER_INFO.contains(v)){
                                valueArrList.add(v);
                            }
                        }
                        if(valueArrList.size() == Const.FILTER_INFO.size() -1){
                            valueArrList.add("假证假票");
                        }
                        filterInfo = StringUtils.join(valueArrList,";");
                        specialCustomChartRepository.saveFilterInfo(filterInfo,customChart.getId());
                    }

                    count++;
                }catch (Exception e){

                }
            }
            sb.append("专题分析自定义图表修改成功："+count+"条。\n");
        }

        return sb.toString();
    }






}
