package com.trs.netInsight.widget.user.service;


import org.springframework.web.multipart.MultipartFile;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.user.entity.SubGroup;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 用户分组业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/7/24 14:54.
 * @desc
 */
public interface ISubGroupService {
    /**
     * 查询机构下分组
     * @param orgId
     * @param pageNo
     * @param pageSize
     * @param surplusDateSort
     * @param status
     * @param retrievalCondition
     * @param retrievalInformation
     * @return
     * @throws TRSException
     */
    public Page<SubGroup> findByOrgId( String orgId,int pageNo,int pageSize,String surplusDateSort,
                                      String status,String retrievalCondition, String retrievalInformation) throws TRSException;

    /**
     * 没有任何条件  只查询当前机构下的分组
     * @return
     */
    public List<SubGroup> findByOrgId(String orgId);


    /**
     * 添加用户分组
     * @param name      分组名称
     * @param picture   logo图片
     * @param roleIds   权限
     * @param columnNum  日常监测数
     * @param specialNum  专题分析数
     * @param alertNum    预警主题数
     * @param alertAccount  预警账号数
     * @param expireAt     有效期
     * @param columnSync   日常监测同步数据
     * @param specialSync  专题分析同步数据
     * @param userLimit    可登录账号数
     * @param userJson      添加用户
     * @return
     */
    public boolean save(int isAutoadd,String orgId,String name, MultipartFile picture,String[] roleIds,int columnNum,int specialNum,int alertNum,int alertAccount,String expireAt,String columnSync,
                         String specialSyncLevel,String[] specialSync,int userLimit,String userJson) throws TRSException;

    public String  save(SubGroup subGroup,String columnSync, String[] specialSync,String specialSyncLevel) throws TRSException;

    /**
     * 迁移历史数据使用
     * @param subGroup
     * @return
     * @throws TRSException
     */
    public SubGroup  save(SubGroup subGroup);

    /**
     * 修改用户分组
     * @param id
     * @param name
     * @param picture
     * @param pictureName
     * @param roleIds
     * @param columnNum
     * @param specialNum
     * @param alertNum
     * @param alertAccount
     * @param expireAt
     * @param userLimit
     * @param userJson
     */
    public  void  update(String id,String name, MultipartFile picture,String pictureName,String[] roleIds,int columnNum,int specialNum,int alertNum,int alertAccount,String expireAt,int userLimit,String userJson) throws TRSException;

    public String  update(SubGroup subGroup);
    public void updateStatus(SubGroup subGroup,String status);
    public void delete(String id);

    /**
     * 根据 id 查询一个用户分组
     * @param id
     * @return
     */
    public SubGroup findOne(String id);

    /**
     * 编辑回显
     * @param id
     * @return
     */
    public SubGroup detail(String id);

    /**
     * 查询某分组下所有的栏目个数
     * @param subGroupId
     * @return
     */
   // public int getSubGroupColumnCount(String subGroupId);

    /**
     * 查询当前用户分组下所拥有的专题个数
     * @param subGroupId
     * @return
     */
//    public int getSubGroupSpecialCount(String subGroupId);

    /**
     * 查询该用户分组下所拥有的预警数量
     * @param subGroupId
     * @return
     */
//    public int getSubGroupAlertCount(String subGroupId);

    /**
     * 查询该用户分组下所拥有的预警账号个数
     * @param subGroupId
     * @return
     */
    public int getSubGroupAlertAccountCount(String subGroupId);

    /**
     * 设置分组
     * @param id
     * @param columnNum
     * @param specialNum
     * @param alertNum
     * @param alertAccountNum
     * @param userLimit
     * @throws TRSException
     */
    public void setUpGroup( String id,int columnNum,int specialNum, int alertNum, int alertAccountNum,int userLimit) throws TRSException;

    /**
     * 判断该用户是否为该用户分组下的用户
     * @param subGroupId
     * @param userId
     * @return
     */
    public boolean isSubGroupExistUser(String subGroupId, String userId);


    public List<SubGroup> findAll();
}