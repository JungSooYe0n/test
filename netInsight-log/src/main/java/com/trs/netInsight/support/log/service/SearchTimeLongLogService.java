package com.trs.netInsight.support.log.service;

import java.util.List;
import java.util.Map;

/**
 * @author 马加鹏
 * @date 2021/4/23 9:38
 */
public interface SearchTimeLongLogService {

    Map<String, Object> selectSearchLog(String modelType, String createdTime, String searchTime, int pageNum, int pageSize, String searchType, String searchText);

}
