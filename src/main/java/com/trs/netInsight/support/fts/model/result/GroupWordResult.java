package com.trs.netInsight.support.fts.model.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 检索返回结果对象 主要针对词云图
 * Created by yangyanyan on 2018/5/24.
 */
public class GroupWordResult implements Iterable<GroupWordInfo>  {
    private List<GroupWordInfo> groupList = new ArrayList<>();



    public void addAll(GroupWordResult result){
        groupList.addAll(result.getGroupList());
    }
    public void addAll(List<GroupWordInfo> infos){
        for (GroupWordInfo info : infos) {
            groupList.add(info);
        }
    }

    public int size(){return groupList.size();}

    public void addGroup(String field,long number,String entityType){
        groupList.add(new GroupWordInfo(field,number,entityType));
    }

    public String getFieldValue(int index){
        return groupList.get(index).getFieldValue();
    }
    public long getCount(int index){
        return groupList.get(index).getCount();
    }
    public String getEntityType(int index){
        return groupList.get(index).getEntityType();
    }


    @Override
    public Iterator<GroupWordInfo> iterator() {
        return groupList.iterator();
    }

    public void sort(){
        this.groupList.sort(GroupWordInfo::compareTo);
    }

    public List<GroupWordInfo> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<GroupWordInfo> groupList) {
        this.groupList = groupList;
    }
}
