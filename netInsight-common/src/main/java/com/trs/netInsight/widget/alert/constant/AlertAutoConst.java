package com.trs.netInsight.widget.alert.constant;

public class AlertAutoConst {


    public final static String nameKey = "name";
    public final static String source = "source";
    public final static String queryTrsl = "query";
    public final static String alertStatus = "stop";
    public final static String prefix = "name"; //查询语句的唯一标识（请求方的主键）支持前缀*模糊

    //---------------------这是数据中心的 自动预警的接口信息 -----------------------------------
    //开启预警接口规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String start_alertRule = "/vaildQueryManager/startQuery";
    //关闭预警接口规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String stop_alertRule = "/vaildQueryManager/stopQuery";
    //添加预警规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String add_alertRule = "/vaildQueryManager/insertQuery";
    //提交预警规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String submit_alertRule = "/vaildQueryManager/submitQuery";
    //移除预警规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String delete_alertRule = "/vaildQueryManager/removeQuery";
    //模糊匹配，将符合的预警规则信息移除 ，谨慎使用 - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String fuzzy_delete_alertRule = "/vaildQueryManager/fuzzyRemoveQuery";

    //修改预警规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通  - 这个被废弃了
    //public final static String update_alertRule = "/vaildQueryManager/updateQuery";
    //查找一个预警规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String findOne_alertRule = "/vaildQueryManager/findQuery";
    //模糊查询查找预警规则信息，根据前缀匹配  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String fuzzy_find_alertRule = "/vaildQueryManager/fuzzyQuery";
    //根据查询数据库查找预警规则信息  - 接口在数据中心那边，如果要改，必须与数据中心新沟通
    public final static String findBySource_alertRule = "/vaildQueryManager/findAllQuery";

//---------------------上面的信息是数据中心的 接口信息 -----------------------------------



    //---------------------下面的信息是 预警工程中操作自动预警的 接口信息 -----------------------------------
    //保存预警规则信息，有则修改，无则新建 - 接口在预警工程中
    public final static String alertNetInsight_save_auto = "/autoAlert/saveAutoAlert";


    //移除预警规则信息 - 接口在预警工程中
    public final static String alertNetInsight_delete_auto = "/autoAlert/deleteAutoAlertRule";

    //修改预警规则信息的状态 - 接口在预警工程中
    public final static String alertNetInsight_update_status_auto = "/autoAlert/updateAutoAlertRuleStatus";

    //查找一个预警规则信息 - 接口在预警工程中
    public final static String alertNetInsight_findOne_auto = "/autoAlert/findOneAutoAlert";

}
