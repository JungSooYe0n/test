package com.trs.netInsight.support.log.service.impl;

import com.trs.netInsight.support.log.entity.SearchTimeLongLog;
import com.trs.netInsight.support.log.entity.enums.SearchLogType;
import com.trs.netInsight.support.log.repository.SearchTimeLongLogRepository;
import com.trs.netInsight.support.log.service.SearchTimeLongLogService;
import com.trs.netInsight.util.CollectionsUtil;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import jdk.nashorn.internal.ir.IfNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 马加鹏
 * @date 2021/4/23 9:39
 */
@Service
public class SearchTimeLongLogServiceImpl implements SearchTimeLongLogService {

    @Autowired
    @Qualifier("searchTimeLongLogRepository")
    private SearchTimeLongLogRepository searchTimeLongLogRepository;

    @Override
    public List<Map<String, Object>> selectSearchLog(String modelType, String createdTime, String searchTime, int pageNum, int pageSize, String searchType, String searchText) {

        if (UserUtils.isSuperAdmin()) {

            Pageable pageable = new PageRequest(pageNum, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "createdTime")));

            Specification<SearchTimeLongLog> specification = new Specification<SearchTimeLongLog>() {
                @Override
                public Predicate toPredicate(Root<SearchTimeLongLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();
                    if (StringUtil.isNotEmpty(modelType)) {
                        if ("all".equals(modelType)) {
                            predicates.add(cb.isNotNull(root.get("modelType")));
                        } else {
                            String type = "";
                            switch (modelType) {
                                case "column":
                                    type = SearchLogType.COLUMN.getValue();
                                    break;
                                case "special":
                                    type = SearchLogType.SPECIAL.getValue();
                                    break;
                                default:
                                    break;
                            }
                            predicates.add(cb.equal(root.get("modelType"), type));
                        }
                    }
                    if (StringUtil.isNotEmpty(createdTime)) {
                        if ("all".equals(createdTime)) {
                            predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), new Date()));
                        } else {
                            String userTime = null;
                            switch (createdTime) {
                                case "1h":
                                    userTime = DateUtil.formatDateAfterForHour(new Date(), DateUtil.yyyyMMdd, -1);
                                    break;
                                case "1d":
                                    userTime = DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd, -1);
                                    break;
                                case "7d":
                                    userTime = DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd, -7);
                                    break;
                                case "30d":
                                    userTime = DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd, -30);
                                    break;
                                default:
                                    userTime = DateUtil.format2String(new Date(), DateUtil.yyyyMMdd);
                                    break;
                            }
                            Date date = null;
                            try {
                                date = new SimpleDateFormat(DateUtil.yyyyMMdd).parse(userTime);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), date));
                        }
                    }
                    if (StringUtil.isNotEmpty(searchTime)) {
                        if ("all".equals(searchTime)) {
                            predicates.add(cb.isNotNull(root.get("searchTime")));
                        } else {
                            int time = -1;
                            switch (searchTime) {
                                case "3y":
                                    time = 90;
                                    break;
                                case "6y":
                                    time = 180;
                                    break;
                                case "1n":
                                    time = 365;
                                    break;
                                default:
                                    time = 0;
                                    break;
                            }
                            if (time == 0) {
                                predicates.add(cb.greaterThanOrEqualTo(root.get("searchTime"), time));
                            } else {
                                predicates.add(cb.lessThanOrEqualTo(root.get("searchTime"), time));
                            }
                        }
                    }
                    if (StringUtil.isNotEmpty(searchType)) {
                        String text = StringUtil.isNotEmpty(searchText) ? searchText : "";
                        switch (searchType) {
                            case "organizationName":
                                predicates.add(cb.like(root.get("organizationName"), "%" + text + "%"));
                                break;
                            case "userName":
                                predicates.add(cb.like(root.get("createdUserName"), "%" + text + "%"));
                                break;
                            default:
                                break;
                        }
                    }
                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            Page<SearchTimeLongLog> searchTimeLongLogPage = searchTimeLongLogRepository.findAll(specification, pageable);

            List<Map<String, Object>> result = new ArrayList<>();
            if (searchTimeLongLogPage != null) {
                List<SearchTimeLongLog> content = searchTimeLongLogPage.getContent();
                if (CollectionsUtil.isNotEmpty(content)) {
                    int i = 1;
                    for (SearchTimeLongLog searchTimeLongLog : content) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("num", i++);
                        map.put("organizationName", searchTimeLongLog.getOrganizationName());
                        map.put("userName", searchTimeLongLog.getCreatedUserName());
                        map.put("modelType", searchTimeLongLog.getModelType());
                        map.put("modelName", searchTimeLongLog.getModelName());
                        map.put("timeRange", searchTimeLongLog.getTimeRange());
                        map.put("createdTime", DateUtil.format2String(searchTimeLongLog.getCreatedTime(), DateUtil.yyyyMMdd));
                        map.put("searchTime", searchTimeLongLog.getSearchTime());
                        map.put("trsl", searchTimeLongLog.getTrsl());
                        result.add(map);
                    }
                }
            }
            return result;
        }
        return null;
    }

}
