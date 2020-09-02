package com.trs.netInsight.widget.alert.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

public class AlertAutoConst {


    public final static String nameKey = "name";
    public final static String source = "source";
    public final static String queryTrsl = "query";
    public final static String alertStatus = "stop";

    //开启预警接口规则信息
    public final static String start_alertRule = "/vaildQueryManager/startQuery";


    //关闭预警接口规则信息
    public final static String stop_alertRule = "/vaildQueryManager/stopQuery";

    //添加预警规则信息
    public final static String add_alertRule = "/vaildQueryManager/insertQuery";


    //移除预警规则信息
    public final static String delete_alertRule = "/vaildQueryManager/removeQuery";

    //修改预警规则信息
    public final static String update_alertRule = "/vaildQueryManager/updateQuery";

    //查找一个预警规则信息
    public final static String findOne_alertRule = "/vaildQueryManager/findQuery";

    //根据查询数据库查找预警规则信息
    public final static String findBySource_alertRule = "/vaildQueryManager/findAllQuery";





}
