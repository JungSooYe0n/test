package com.trs.netInsight.widget.report.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.analysis.entity.ChartAnalyzeEntity;
import com.trs.netInsight.widget.report.entity.*;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.user.entity.User;

import java.util.List;

public interface IReportService {

	/**
	 * 查询某个用户的所有收藏
	 * 
	 * @throws TRSException
	 */
	public Object getAllFavourites(User user, int pageNo, int pageSize,String groupName,String keywords,String invitationCard,String forwarPrimary,Boolean isExport) throws TRSException;

	/**
	 * 查询某个用户收藏  根据条件
	 *
	 * @throws TRSException
	 */
	public Object getFavouritesByCondition(User user, int pageNo, int pageSize,List<String> groupName,String keyword,String fuzzyValueScope,String invitationCard,String forwarPrimary,Boolean isExport) throws TRSException;

	/**
	 * 由于hybase搜索结果不按照查询条件的sid排列 而从mysql取出的市按照时间排列的 为了让结果按照时间排列
	 */
	public List<ChartAnalyzeEntity> resultByTime(List<ChartAnalyzeEntity> result, List<String> sidList);

	/**
	 * 加入收藏
	 * 
	 * @param sids
	 *            需要收藏的文章ID
	 * @param userId
	 *            用户ID
	 * @return
	 */
	public String saveFavourites(String sids, String userId, String subGroupId,String md5,String groupName,String urltime);

	/**
	 * 取消收藏
	 * 
	 * @param sids
	 *            需要删除的id
	 * @param userId
	 *            用户id
	 * @return
	 */
	public String delFavourites(String sids, String userId);
	public String changeToken();
	public String changeHistoryFavourites();
	public String changeHistoryFavouritesGroupName();

	public Object favouriteHybase( String sids,List<String> sidList, String groupName,int pageNo,
								   int pageSize) throws TRSException;


	/**
	 * 新增素材库
	 * 
	 * @param materialLibrary
	 * @return
	 */
	public Object saveMaterialLibrary(MaterialLibrary materialLibrary);

	public void saveBatchList(List<MaterialLibrary> libraryList);

	public void delBatchList(List<MaterialLibrary> libraryList);

	/**
	 * 对专项监测过来的数据，新增或者修改
	 * 
	 * @param specialProject
	 */
	public void saveMaterialLibrary(SpecialProject specialProject);

	/**
	 * 获取当前用户所有的素材库
	 * 
	 * @param userId
	 *            用户id
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页大小
	 * @return
	 * @throws TRSException
	 */
	public Object getUserLibrary(String userId, int pageNo, int pageSize) throws TRSException;
	/**
	 * 获取机构素材库
	 * @date Created at 2017年12月28日  下午3:39:33
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws TRSException
	 */
	public Object getOrganizationIdLibrary(String organizationId, int pageNo, int pageSize) throws TRSException;

	public Object preview(MaterialLibrary materialLibrary,String groupame) throws TRSException;

	/**
	 * 获取所有的素材库
	 * 
	 * @return
	 */
	public List<MaterialLibrary> getAllLibrary();

	/**
	 * 获取单个素材库
	 * 
	 * @param libraryId
	 * @return
	 */
	public MaterialLibrary getOneLibrary(String libraryId);

	/**
	 * 获取该用户的所有报告
	 * 
	 * @param user
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<Report> getAllReport(User user, int pageNo, int pageSize);
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午3:32:33
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<Report> getReportByOrganizationId(String organizationId, int pageNo, int pageSize);

	/**
	 * 获取单个报告
	 * 
	 * @return
	 */
	public Report getOneReport(String reportId);

	/**
	 * 模糊查询报告
	 * 
	 * @param user
	 * @param reportName
	 * @return
	 */
	public List<Report> seachReport(User user, String reportName);
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午3:36:02
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param reportName
	 * @return
	 */
	public List<Report> seachReportByOrganizationId(String organizationId, String reportName);

	public List<MaterialLibrary> seachLib(User user, String libName);
	
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午3:37:54
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param libName
	 * @return
	 */
	public List<MaterialLibrary> seachLibByOrganizationId(String organizationId, String libName);

	public void saveReport(Report report);

	/**
	 * 删除报告
	 * 
	 * @param reportId
	 * @return
	 */
	public Object deleteReport(String reportId);

	public Template saveOrupdateTep(Template templat);

	public Template getOneTemplate(String templateId, String templateList) throws Exception;

	public Object getTemplateList(String userId, String libraryId, int pageNo, int pageSize) throws Exception;
	public Object getTemplateListByOrganizationId(String organizationId, String libraryId, int pageNo, int pageSize) throws Exception;

	public Object getTemplateNoLib(String userId, int pageNo, int pageSize) throws Exception;

	public Object saveFavMaterial(String sid, String libraryId, String operate,String groupName) throws Exception;

	/**
	 * 构建出报告模板
	 * 
	 * @param eleList
	 * @param libraryId
	 * @param template
	 * @return
	 * @throws OperationException
	 */
	public String analyTemplateList(List<TElement> eleList, String libraryId, Template template, String reportName)
			throws OperationException;

	public void saveReportData(ReportData reportData);

	/**
	 * 得到生成报告所使用的sql 拼接来着：素材库real_sql , 报告素材库中的sid ， 如果为收藏则加，删除这去除。
	 * 
	 * @author songbinbin 2017年5月11日 String
	 * @param libraryId
	 * @return
	 */
	public String getReportMaterialSql(String libraryId);

	public boolean mergeReport(String reportId);
	
	public boolean mergeReport(Report report);

	public Object bulidReportDoc(Report report) throws Exception;

	public void bulidReportDoc(Report report,ReportData reportData) throws Exception;
	
	public Object drawImage(String reportId, String imageDate, String title, int position, String key, String imageName)
			throws Exception;
	/**
	 * 构建主键检索表达式  传统
	 * @param sidList 主键list
	 * @return
	 */
	//public String buildSql(List<String> sidList);
	
	/**
	 * 构建主键检索表达式 微博
	 * @param sidList 主键list
	 * @return
	 */
	//public String buildSqlWeiBo(List<String> sidList);
	
	/**
	 * 构建主键检索表达式  微信
	 * @param sidList 主键list
	 * @return
	 */
	//public String buildSqlWeiXin(List<String> sidList);
	


	

}
