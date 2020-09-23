package com.trs.netInsight.widget.special.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.widget.column.entity.emuns.SpecialFlag;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;

/**
 * 专项监测服务层
 *
 * Created by ChangXiaoyang on 2017/4/7.
 */
public interface ISpecialService {
	Integer getMaxSequenceForSpecial(String parentPageId,User user);

	/**
	 * 发送邮件
	 * 
	 * @date Created at 2017年11月24日 下午5:20:39
	 * @Author 谷泽昊
	 * @param specialProject
	 * @return
	 * @throws OperationException
	 */
	public Object sendEmail(SpecialProject specialProject) throws OperationException;

	/**
	 * 存储imgurl
	 * 
	 * @date Created at 2017年11月24日 下午5:20:31
	 * @Author 谷泽昊
	 * @param specialProject
	 * @return
	 * @throws TRSException
	 */
	public String imgUrl(SpecialProject specialProject) throws TRSSearchException, TRSException;

	/**
	 * 获取检测方案列表
	 * 
	 * @date Created at 2017年11月24日 下午5:20:25
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param size
	 * @return
	 * @throws TRSException
	 */
	public List<?> getMonitorList(String organizationId, int size) throws TRSException;

	/**
	 * 创建新专项
	 * 
	 * @date Created at 2017年11月24日 下午5:20:16
	 * @Author 谷泽昊
	 * @param specialProject
	 * @throws TRSException
	 */
	public void createSpecial(SpecialProject specialProject) throws TRSException;

	/**
	 * 更新专项
	 * 
	 * @date Created at 2017年11月24日 下午5:20:08
	 * @Author 谷泽昊
	 * 
	 * 
	 * @param specialId
	 *            专项Id
	 * @param specialName
	 *            专项名称
	 * @param allKeywords
	 *            所有关键词[北京;雾霾]
	 * @param anyKeywords
	 *            任意关键词[中国,河北;美国,洛杉矶]
	 * @param excludeWords
	 *            排除词[雾霾;沙尘暴]
	 * @param trsl
	 *            专家模式表达式
	 * @param scope
	 *            搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]
	 * @param type
	 *            专项模式[COMMON, SPECIAL]
	 * @param source
	 *            来源
	 * @param timerange
	 *            时间范围
	 * @param similar
	 *            排重
	 * @param excludeWeb
	 *            排除网站
	 * @param irSimflag
	 *            排重
	 * 
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param weight
	 * 			  权重
	 * @return
	 * @throws TRSException
	 */
	public SpecialProject updateSpecial(String specialId, SpecialType type, String specialName,
			String anyKeywords, String excludeWords,String excludeWordsIndex, String trsl,SearchScope scope, Date startTime, Date endTime,
			String source,String timerange,boolean similar,boolean weight,boolean irSimflag,boolean server,boolean irSimflagAll,String excludeWeb,String monitorSite,String mediaLevel,
										 String mediaIndustry,String contentIndustry,String filterInfo,String contentArea,String mediaArea)
			throws Exception;

	/**
	 * 预览表达式数据
	 * 
	 * @date Created at 2017年11月24日 下午5:20:02
	 * @Author 谷泽昊
	 * @param specialProject
	 * @return
	 * @throws TRSException
	 */
	public Object preview(SpecialProject specialProject,String source) throws TRSException;
	
	/**
	 * 左侧专题专项列表
	 * @param user
	 * @return
	 */
	public Object selectSpecial(User user)throws OperationException;

	/**
	 * 重构左侧专题列表
	 * @param user
	 * @return
	 * @throws OperationException
	 */
	public Object selectSpecialNew(User user)throws OperationException;
	/**
	 * 重构左侧专题列表
	 * @param user
	 * @return
	 * @throws OperationException
	 */
	public Object selectSpecialReNew(User user)throws OperationException;
	Object selectNextShowSpecial(String id,SpecialFlag specialFlag);

    /**
     * 添加用户时 同步数据出现的弹框里的内容
     * @param orgAdminId
     * @return
     * @throws TRSException
     */
	public List selectSomeSpecials(String orgAdminId) throws TRSException;
	
	/**
	 * 拖拽接口
	 * @param ids 按顺序以;分割
	 * @return
	 */
	public Object move(String ids);
	
	/**
	 * 列表拖拽接口
	 * @param ids id按顺序排好;分割字符串
	 * @param twoOrThree 与id一一对应 ;分割 二级传two 专项传three
	 * @return
	 */
	public Object moveList(String ids,String twoOrThree);
	/**
	 *
	 * @param id
	 * @param pid
	 * @param typeFlag
	 * @return
	 */
	/**
	 * 专题左侧列表拖动
	 * @param id   拖动id
	 * @param pid  新父级id
	 * @param typeFlag  原类型标（只用来判断是文件夹还是专题）
	 * @param ids   新顺序 （要求拖到哪在哪，所以需要一个新顺序。若往合着的文件夹下拖，默认放最后。只需要当前拖动id所在级别内的ids）
	 * @param typeFlags  对应类型表（只用来判断是文件夹还是专题,与ids一一对应）
	 * @return
	 */
	public Object moveListNew(String id,String pid,String typeFlag,String[] ids,int[] typeFlags) throws TRSException;
	Object moveProjectSequence(String data,String moveData,String parentId,User user)throws OperationException;

	void insertPropectToLast(String parentId,User user);
	/**
	 * 重新排序column，在删除一个分组或者栏目时，去掉原栏目的排序
	 * @param moveId  被删除的对象的id
	 * @param flag 标识是栏目还是分组  分组为 0  栏目为1
	 * @param user
	 * @return
	 */
	Object moveSequenceForSpecial(String moveId, SpecialFlag flag, User user) throws OperationException;
	/**
	 * 复制当前机构管理员专题到普通用户下
	 * 
	 * @param orgUser
	 * 			普通用户id
	 * @return
	 * @throws TRSException
	 */
	public void copyOrgSpecial2Common(User orgUser,User user) ;

	public void copySomeSpecialToUserGroup(String[] specialSync,String specialSyncLevel,SubGroup subGroup);

	/**
	 * 跨级拖动专题
	 * @date Created at 2018年12月19日  下午3:18:14
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param parentId
	 * @param specialId
	 * @param ids
	 * @param twoOrThree
	 * @return
	 */
	public Object crossLevelDragging(String parentId, String specialId, String ids, String twoOrThree);


	
}
