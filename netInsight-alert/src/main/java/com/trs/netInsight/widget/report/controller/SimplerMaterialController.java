package com.trs.netInsight.widget.report.controller;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.report.service.IMaterialLibraryNewService;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/22.
 * @desc 极简报告 素材库控制层
 */
@Slf4j
@RestController
@RequestMapping("/simplerMaterial")
@Api(description = "极简报告 素材库接口")
public class SimplerMaterialController {
    @Autowired
    private IMaterialLibraryNewService materialLibraryNewService;

    /**
     * 素材库列表
     * @return
     */
    @RequestMapping(value = "/materialLibrarys",method = RequestMethod.GET)
    @ApiOperation("极简报告 ： 素材库列表")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_MATERIAL_LIST, systemLogType = SystemLogType.SIMPLER_MATERIAL, systemLogOperationPosition = "")
    @FormatResult
    public Object materialLibrarys(){
        User loginUser = UserUtils.getUser();
        return materialLibraryNewService.findByUser(loginUser);
    }

    /**
     * 删除某素材库分组
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteMaterial", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_MATERIAL_DELETE, systemLogType = SystemLogType.SIMPLER_MATERIAL, systemLogOperationPosition = "")
    @ApiOperation("删除素材库某分组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "素材库分组id", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object deleteMaterial(String id) {
        return materialLibraryNewService.delete(id);
    }

    /**
     * 添加素材库分组
     * @param id
     * @param name
     * @return
     */
    @RequestMapping(value = "/saveMaterial", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_MATERIAL_ADD_UPDATE, systemLogType = SystemLogType.SIMPLER_MATERIAL, systemLogOperationPosition = "")
    @ApiOperation("添加/修改素材库某分组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "素材库分组id", dataType = "String", paramType = "query",required = false),
            @ApiImplicitParam(name = "name", value = "素材库分组名字", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object saveMaterial(String id,String name) {
        return materialLibraryNewService.save(id,name);
    }

    /**
     * 添加素材资源
     * @param libraryId
     * @param sids
     * @param md5s
     * @param groupNames
     * @return
     */
    @RequestMapping(value = "/saveMaterialResource", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_MATERIAL_ADDRESOURCE, systemLogType = SystemLogType.SIMPLER_MATERIAL, systemLogOperationPosition = "")
    @ApiOperation("往素材库分组下添加资源")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "libraryId", value = "素材库分组id", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "name", value = "素材库分组名称", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "sids", value = "素材资源id", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "md5s", value = "素材资源md5", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "urlTimes", value = "素材资源urlTimes", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "groupNames", value = "素材资源来源", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object saveMaterialResource( String sids, String md5s, String urlTimes,String groupNames,String libraryId,String name) throws OperationException {
        String[] groupNameArray = groupNames.split(";");
        String[] sidArray = sids.split(";");
        if(groupNameArray.length != sidArray.length){
            return new OperationException("所传sid和groupName的个数不相同");
        }

        for(int i = 0 ;i < groupNameArray.length ;i++ ){
            if(Const.SOURCE_GROUPNAME_CONTRAST.containsKey(groupNameArray[i])){
                groupNameArray[i] = Const.SOURCE_GROUPNAME_CONTRAST.get(groupNameArray[i]);
            }else{
                return new OperationException("所传参数：groupName值有误，获取我的收藏列表出错");
            }
        }
        groupNames = StringUtils.join(groupNameArray, ";");

        return materialLibraryNewService.saveMaterialResource(sids,UserUtils.getUser(),md5s,urlTimes,groupNames,libraryId,name);
    }

    @RequestMapping(value = "/findMaterialResource", method = RequestMethod.GET)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_MATERIAL_LISTRESOURCE, systemLogType = SystemLogType.SIMPLER_MATERIAL, systemLogOperationPosition = "")
    @ApiOperation("查询素材库分组下的资源")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "libraryId", value = "素材库分组id", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "groupName", value = "素材资源来源", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "fuzzyValue", value = "结果中搜索", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name ="fuzzyValueScope",value = "在结果中搜索的范围",dataType = "String",defaultValue = "fullText",paramType = "query",required = false),
            @ApiImplicitParam(name = "forwarPrimary", value = "微博原发 primary/转发 forward", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "invitationCard", value = "论坛主贴 0 /回帖 1", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "time", value = "时间", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "pageNo", value = "页码", dataType = "int", paramType = "query",required = true),
            @ApiImplicitParam(name = "pageSize", value = "页长", dataType = "int", paramType = "query",required = true)})
    @FormatResult
    public Object findMaterialResource( String libraryId,String groupName, String fuzzyValue,String fuzzyValueScope,String forwarPrimary,String invitationCard,String time,int pageNo,int pageSize) throws Exception {

        //防止前端乱输入
        pageSize = pageSize>=1?pageSize:10;
        List<String> source = new ArrayList<>();
        if("ALL".equals(groupName)){
            groupName = Const.ALL_GROUP_COLLECT;
        }
        String[] groupNameArray = groupName.split(";");

        for(String str : groupNameArray){
            if(Const.SOURCE_GROUPNAME_CONTRAST.containsKey(str)){
                source.add(Const.SOURCE_GROUPNAME_CONTRAST.get(str));
            }else{
                throw new OperationException("所传参数：groupName值有误，为:"+str+"，获取我的收藏列表出错");
            }
        }
        return materialLibraryNewService.findMaterialSourceByCondition(libraryId,pageNo,pageSize,source,fuzzyValue,fuzzyValueScope,invitationCard,forwarPrimary,time);
        //历史方法  -  >  存在筛选问题，而且是从hybase拿取数据，现在从mysql拿取
        // return materialLibraryNewService.findMaterialResource(libraryId,pageNo,pageSize,groupName,fuzzyValue,invitationCard,forwarPrimary,time);
    }


    /**
     * 删除素材库某分组下的资源
     * @param sids
     * @return
     */
    @RequestMapping(value = "/deleteMaterialResource", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_MATERIAL_DELRESOURCE, systemLogType = SystemLogType.SIMPLER_MATERIAL, systemLogOperationPosition = "")
    @ApiOperation("删除素材库某分组下的资源")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sids", value = "要删除的素材资源的sid", dataType = "String", paramType = "query",required = true),
            @ApiImplicitParam(name = "libraryId", value = "要删除的素材资源对应的素材库id", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object deleteMaterialResource(String sids,String libraryId) {
        return materialLibraryNewService.delLibraryResource(sids,libraryId);
    }


    @ApiOperation("更改历史收藏")
    @RequestMapping(value = "/changeHistoryMaterial",method = RequestMethod.GET)
    public Object changeHistoryMaterial(){
        materialLibraryNewService.changeHistoryMaterial();
        System.err.println("结束了~~~~~~~~~~~~~~~~~~~~~~");
        return "";

    }
}
