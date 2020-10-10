package com.trs.netInsight.widget.alert.entity;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AutoAlertRuleResult {

    private Boolean status;
    private Integer code;
    private String message;
    private Object data;
    private Integer total;

    public static AutoAlertRuleResult StringToObject(String string){
        if(StringUtil.isEmpty(string)){
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(string);
        Boolean status = jsonObject.getBoolean("status");
        Integer code = jsonObject.getInteger("code");
        String message = jsonObject.getString("msg");
        Integer total = jsonObject.getInteger("total");
        Object data = jsonObject.get("data");

        AutoAlertRuleResult result = new AutoAlertRuleResult(status,code,message,data,total);
        return result;
    }

}
