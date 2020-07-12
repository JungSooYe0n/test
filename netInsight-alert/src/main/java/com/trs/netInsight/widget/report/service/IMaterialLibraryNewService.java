package com.trs.netInsight.widget.report.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.widget.report.entity.MaterialLibraryNew;
import com.trs.netInsight.widget.user.entity.User;

import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/22.
 * @desc 舆情报告 极简模式  素材库分组服务层接口
 */
public interface IMaterialLibraryNewService {
    //添加//修改
    public MaterialLibraryNew save(String id ,String name);


    //删除
    public String delete(String id);
    //查所有(通过用户id)
    public List<MaterialLibraryNew> findByUser(User user);
    //通过用户分组id
    public List<MaterialLibraryNew> findBySubGroupId();
    //查一个
    public MaterialLibraryNew findOne(String id);

    /**
     *
     * 添加素材
     * @param sids
     * @param user
     * @param md5
     * @param groupName
     * @param libraryId
     */
    public String  saveMaterialResource(String sids, User user, String md5,
                                      String urlTime,String groupName,String libraryId,String name)throws OperationException;

    /**
     * 获取 素材库组对应来源下的的资源
     * @param libraryId
     * @param groupName
     * @param fuzzyValue
     * @return
     */
    public Object findMaterialResource(String libraryId, int pageNo, int pageSize,
                                                String groupName, String fuzzyValue, String invitationCard,
                                                String forwarPrimary,String time) throws Exception;

    /**
     * 获取 素材库组对应来源下的的资源
     * @param libraryId
     * @return
     */
    public List<FtsDocumentCommonVO> findMaterialResourceForReport(String libraryId) throws TRSException;

    /**
     * 删除 某素材库下的素材资源
     * @param sids
     * @param libraryId
     * @return
     */
    public String delLibraryResource(String sids,String libraryId);
    /**
     * 删除 去掉某条信息，从所有素材库中
     * @param sids
     * @return
     */
    String delLibraryResourceForIds(String sids);

    /**
     * 获取素材库下数据，按条数
     * @param libraryId
     * @param groupName
     * @param fuzzyValue
     * @return
     */
    public Object findMaterialSourceByCondition(String libraryId, int pageNo, int pageSize,
                                                    List<String> groupName, String fuzzyValue, String fuzzyValueScope,String invitationCard,
                                                    String forwarPrimary, String time,Boolean isExport) throws TRSException;

    public Object changeHistoryMaterial();
}
