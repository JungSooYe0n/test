package com.trs.netInsight.widget.report.entity.repository;

import com.trs.netInsight.widget.report.entity.ReportResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by shao.guangze on 2018年5月18日 上午10:19:35
 */
@Repository
public interface ReportResourceRepository
		extends PagingAndSortingRepository<ReportResource, String>, JpaSpecificationExecutor<ReportResource> {

	/**
	 * 根据用户id查询报告资源表report_resource，排序
	 * 
	 * @author shao.guangze
	 * @param userId
	 * @param sort
	 * */
	List<ReportResource> findByUserId(String userId, Sort sort);

	/**
	 * @author shao.guangze
	 * @param userId
	 * @param pageable
	 * @return
	 */
	List<ReportResource> findByUserId(String userId, Pageable pageable);

	/***
	 * 根据模板Id和资源状态值指定要操作的ReportResource
	 * resourceStatus	==》 ReportResource,resourceStatus
	 * @param templateId
	 * @param resourceStatus
	 */
	@Transactional
	void deleteByTemplateIdAndResourceStatus(String templateId, Integer resourceStatus);

	/***
	 * 根据模板Id和资源状态值指定要操作的ReportResource
	 * resourceStatus	==》 ReportResource,resourceStatus
	 * @param templateId
	 * @param resourceStatus
	 */
	@Transactional
	void deleteByTemplateIdAndResourceStatusAndChapterPosition(String templateId, Integer resourceStatus, Integer chapterPosition);

	/***
	 * 根据模板Id和资源状态值指定要操作的ReportResource
	 * resourceStatus	==》 ReportResource,resourceStatus
	 * @param templateId
	 * @param resourceStatus
	 * @return
	 */
	List<ReportResource> findByTemplateIdAndResourceStatus(String templateId, Integer resourceStatus);

	/***
	 * listPreview时使用
	 * @param reportId
	 * @param resourceStatus
	 * @return
	 */
	List<ReportResource> findByReportIdAndResourceStatus(String reportId, Integer resourceStatus);

	/**
	 * 删除手动报告时使用
	 * @param reportId
	 * @return
	 */
	List<ReportResource> findByReportId(String reportId);

	/**
	 * 删除手动报告时使用
	 * @param reportId
	 */
	@Transactional
	void  deleteByReportId(String reportId);

	@Transactional
	void deleteByReportTypeAndCreatedTimeLessThan(String reportType, Date createdTime);

	List<ReportResource> findByUserIdAndTemplateId(String userId, String templateId, Sort sort);

	List<ReportResource> findByLibraryId(String libraryId);

	ReportResource findByUserIdAndSidAndChapter(String userId, String sid, String chapter);

	ReportResource findBySidAndTemplateIdAndChapter(String sid, String templateId, String chapter);

	ReportResource findBySidAndTemplateIdAndChapterPosition(String sid, String templateId, Integer chapterPosition);

	List<ReportResource> findByTemplateIdAndReportId(String templateId, String reportId);

	List<ReportResource> findByUserIdAndSidIn(String userId, Collection<String> sid);

	List<ReportResource> findByUserIdAndReportType(String userId, String reportType);

	//定位到某条资源具体位置，制作/完成（预览）时的拖拽
	List<ReportResource> findByTemplateIdAndChapterAndResourceStatusAndSid(String templateId, String chapter, int resourceStatus, String sid);

	List<ReportResource> findByTemplateIdAndChapter(String templateId, String chapter);
	List<ReportResource> findByTemplateIdAndChapterAndReportId(String templateId, String chapter, String reportId);

	@Transactional
	void deleteByTemplateId(String templateId);

	@Transactional
	void deleteByTemplateIdAndReportIdIsNull(String templateId);

	List<ReportResource> findByTemplateIdAndChapterAndResourceStatus(String templateId, String chapter,
                                                                     int resourceStatus);

	List<ReportResource> findByReportIdAndChapterAndResourceStatus(String reportId, String chapter,
                                                                   int resourceStatus);
}
