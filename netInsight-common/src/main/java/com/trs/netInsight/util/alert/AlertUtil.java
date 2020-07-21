package com.trs.netInsight.util.alert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.util.HttpUtil;
import com.trs.netInsight.widget.alert.entity.AlertEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/11/29.
 * @desc
 */
public class AlertUtil {

    public static List<AlertEntity> getAlerts(String userId, String sids,String alertUrl) throws OperationException{

        //在存历史预警的时候 不管是heky还是Mid都存成alert实体中的sid了  所以查找时候按照sid去查就可以
        List<AlertEntity> alertList = null;
        String url = alertUrl+"/alert/getAlertByUserIdAndInSids";
        HashMap<String, String> map = new HashMap<>();
        map.put("userId",userId);
        map.put("sids",sids);
        String doPost = HttpUtil.doPost(url, map, "utf-8");
        if (doPost.contains("\"code\":500")){
            Map<String,String> dataMap = (Map<String,String>)JSON.parse(doPost);
            String message = map.get("message");
            throw new OperationException("预警查询出错，message："+message,new Exception());
        }else {
            alertList = JSONArray.parseArray(doPost,AlertEntity.class);
        }
        return alertList;

    }
}
