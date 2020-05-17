package com.trs.netInsight.util;

import com.trs.netInsight.widget.user.entity.User;

/**
 * 缓存 key值获得(主要 是 用户id 还是 用户分组id 的 区分)
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/8/2 11:38.
 * @desc
 */
public class CachekeyUtil {

    public static String getToolKey(User user,String trsl,String suffix){
        String key = "";
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            key = user.getId() + trsl + suffix;
        }else {
            key = user.getSubGroupId() + trsl + suffix;
        }
        return key;
    }
}
