package com.trs.netInsight.support.log.service.impl;

import com.trs.netInsight.support.log.entity.FuzzySearchLog;
import com.trs.netInsight.support.log.repository.FuzzySearchLogRepository;
import com.trs.netInsight.support.log.service.IFuzzySearchLogService;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.support.report.excel.ExcelFactory;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class FuzzySearchLogServiceImpl implements IFuzzySearchLogService {

    @Autowired
    private FuzzySearchLogRepository fuzzySearchLogRepository;

    public static String[] exportHeadFuzzySearchLog = {"序号", "搜索用户", "搜索时间","搜索类型（精准/模糊）", "输入的关键字(原始)", "输入的关键字(转化空格后)",
            "模糊关键字(分词过后的)", "基础表达式", "模糊的基础表达式(分词后的)", "精准查询表达式", "模糊查询表达式"};

    @Override
    public FuzzySearchLog save(FuzzySearchLog fuzzySearchLog) {
        return fuzzySearchLogRepository.saveAndFlush(fuzzySearchLog);
    }

    @Override
    public List<FuzzySearchLog> findFuzzySearchLogListByCondition(String userName, String startTime, String endTime) {
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");
        PageRequest pageable = new PageRequest(0, 2000, sort);
        Page<FuzzySearchLog> page = null;
        if (StringUtil.isEmpty(userName) && StringUtil.isEmpty(startTime) && StringUtil.isEmpty(endTime)) {
            page = fuzzySearchLogRepository.findAll(pageable);
        } else {
            Specification<FuzzySearchLog> criteria = new Specification<FuzzySearchLog>() {
                @Override
                public Predicate toPredicate(Root<FuzzySearchLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<>();
                    if (StringUtils.isNotBlank(userName)) {
                        predicates.add(cb.equal(root.get("userName"), userName));
                    }
                    if (StringUtils.isNotBlank(startTime)) {
                        Date start = null;
                        Date end = null;
                        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
                        try {
                            if (StringUtils.isNotBlank(startTime)) {
                                start = sdf.parse(startTime);
                                if (StringUtils.isNotBlank(endTime)) {
                                    end = sdf.parse(endTime);
                                } else {
                                    end = sdf.parse(sdf.format(new Date()));
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        predicates.add(cb.between(root.get("createdTime"), start, end));
                    }
                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };
            page = fuzzySearchLogRepository.findAll(criteria, pageable);
        }
        if (page != null && page.getContent() != null) {
            List<FuzzySearchLog> list = page.getContent();
            return list;
        }
        return null;
    }

    @Override
    public ByteArrayOutputStream exportFuzzySearchLog(List<FuzzySearchLog> list) throws IOException {
        ExcelData data = new ExcelData();
        data.setHead(exportHeadFuzzySearchLog);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                //{"序号","搜索用户","搜索时间","输入的关键字(原始)","输入的关键字(转化空格后)",
                //            "模糊关键字(分词过后的)","基础表达式","模糊的基础表达式(分词后的)","精准查询表达式","模糊查询表达式"}
                FuzzySearchLog fuzzySearchLog = list.get(i);
                String userName = fuzzySearchLog.getUserName();
                Date createdTime = fuzzySearchLog.getCreatedTime();
                Boolean isFuzzySearch = fuzzySearchLog.getIsFuzzySearch();
                String searchType = "精准";
                if(isFuzzySearch){
                    searchType = "模糊";
                }
                String originKeyword = fuzzySearchLog.getOriginKeyword();
                String keywords = fuzzySearchLog.getKeywords();
                String fuzzyKeywords = fuzzySearchLog.getFuzzyKeywords();
                String replaceKeywords = fuzzySearchLog.getReplaceKeywords();
                String replaceFuzzyKeywords = fuzzySearchLog.getReplaceFuzzyKeywords();
                String trsl = fuzzySearchLog.getTrsl();
                String fuzzyTrsl = fuzzySearchLog.getFuzzyTrsl();
                data.addRow(i+1, userName, createdTime,searchType, originKeyword, keywords, fuzzyKeywords, replaceKeywords, replaceFuzzyKeywords, trsl, fuzzyTrsl);
            }
        }
        return ExcelFactory.getInstance().export(data);
    }
}
