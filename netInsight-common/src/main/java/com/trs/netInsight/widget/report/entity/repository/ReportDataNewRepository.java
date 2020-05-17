package com.trs.netInsight.widget.report.entity.repository;

import com.trs.netInsight.widget.report.entity.ReportDataNew;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by shao.guangze on 2018年6月1日 下午4:54:29
 */
public interface ReportDataNewRepository extends PagingAndSortingRepository<ReportDataNew, String>,JpaSpecificationExecutor<ReportDataNew>{
	
	@Query(value = "update report_data_new set overview_ofdata=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveOverviewOfdata(String overviewOfdata, String id);
	
	@Query(value = "update report_data_new set news_top10=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveNewsTop10(String newsTop10, String id);
	
	@Query(value = "update report_data_new set weibo_top10=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveWeiboTop10(String weiboTop10, String id);
	
	@Query(value = "update report_data_new set wechat_top10=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveWechatTop10(String wechatTop10, String id);
	
	@Query(value = "update report_data_new set data_trend_analysis=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveDataTrendAnalysis(String dataTrendAnalysis, String id);
	
	@Query(value = "update report_data_new set data_source_analysis=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveDataSourceAnalysis(String dataSourceAnalysis, String id);
	
	@Query(value = "update report_data_new set website_source_top10=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveWebsiteSourceTop10(String websiteSourceTop10, String id);
	
	@Query(value = "update report_data_new set weibo_active_top10=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveWeiboActiveTop10(String weiboActiveTop10, String id);
	
	@Query(value = "update report_data_new set wechat_active_top10=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveWechatActiveTop10(String wechatActiveTop10, String id);
	
	@Query(value = "update report_data_new set area=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveArea(String area, String id);
	
	@Query(value = "update report_data_new set emotion_analysis=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveEmotionAnalysis(String emotionAnalysis, String id);
	
	@Query(value = "update report_data_new set news_hot_topics=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveNewsHotTopics(String newsHotTopics, String id);
	
	@Query(value = "update report_data_new set weibo_hot_topics=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveWeiboHotTopics(String weiboHotTopics, String id);

	@Query(value = "update report_data_new set wechat_hot_top10=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveWechatHotTop10(String wechatHotTop10, String id);
	
	@Query(value = "update report_data_new set done_flag=?1 where id=?2 ", nativeQuery = true)
	@Transactional
	@Modifying
	public void saveDoneFlag(Integer doneFlag, String id);
}
