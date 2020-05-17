package com.trs.netInsight.support.log.service;

import com.trs.netInsight.support.log.entity.FuzzySearchLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface IFuzzySearchLogService {
    FuzzySearchLog save(FuzzySearchLog fuzzySearchLog);

    List<FuzzySearchLog> findFuzzySearchLogListByCondition(String userName,String startTime,String endTime);

    ByteArrayOutputStream exportFuzzySearchLog(List<FuzzySearchLog> list) throws IOException;
}
