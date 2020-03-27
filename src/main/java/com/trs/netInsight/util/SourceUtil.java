package com.trs.netInsight.util;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.widget.user.entity.User;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据源工具类
 *
 * @Type SourceUtil.java
 * @author 张娅
 * @date 2020年2月18日
 * @version
 */
public class SourceUtil {


    /**
     * 根据参数的数据源，判断可查询的数据源
     * @return 如果权限和所选全是ALL 返回ALL ，其他情况返回是标准的数据源类型
     */
    public static String isSource(String groupName){
        groupName = groupName.replaceAll("[,|，|；]",";");
        String source = "";
        User user = UserUtils.getUser();
        if(user != null && user.getId() != null){
            if ( !UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())){
                source = UserUtils.checkOrganization(user).getDataSources().replaceAll(",",";");
            }
        }
        if(StringUtil.isEmpty(source) || "ALL".equals(source)){
            //全部数据源权限
            if("ALL".equals(groupName)){
                return groupName;
            }else{
                List<String> groupList = new ArrayList<>();
                for(String g :groupName.split(";")){
                    groupList.add(Const.DATA_SOURCES.get(g));
                }
                return StringUtils.join(groupList,";");
            }
        }
        //判断传进来的数据源是否在用户拥有的数据源权限中
        Set<String> sourceList = new HashSet<>();
        for(String s :source.split(";")){
            sourceList.add(Const.DATA_SOURCES.get(s));
        }
        if("ALL".equals(groupName)){
            return StringUtils.join(sourceList,";");
        }

        String[] groupArr = groupName.split(";");
        Set<String> result = new HashSet<>();
        for(String group:groupArr){
            String g = Const.DATA_SOURCES.get(group);
            if(sourceList.contains(g)){
                result.add(g);
            }
        }
        if(result.size() > 0){
            return StringUtils.join(result,";");
        }else{
            return null;
        }
    }

    public static List<String> getGroupNameList(String groupName){
        groupName = isSource(groupName);
        if(StringUtil.isEmpty(groupName)){
            return null;
        }
        List<String> groupNameList = new ArrayList<>();
        for(String g:groupName.split(";")){
            groupNameList.add(g);
        }
        if(groupNameList.size() >0 ){
            return groupNameList;
        }else{
            return null;
        }
    }
}
