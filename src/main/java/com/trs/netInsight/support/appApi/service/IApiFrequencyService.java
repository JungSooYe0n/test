package com.trs.netInsight.support.appApi.service;

import com.trs.netInsight.support.appApi.entity.AppApiClient;
import com.trs.netInsight.support.appApi.entity.AppApiFrequency;
import com.trs.netInsight.support.appApi.result.ApiCommonResult;

import java.util.List;

/**
 * api频率相关服务接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
public interface IApiFrequencyService {

	/**
	 * 获取全部Api频率
	 * 
	 * @since changjiang @ 2018年7月3日
	 * @return
	 * @Return : List<AppApiFrequency>
	 */
	public List<AppApiFrequency> findAll();

	/**
	 * 获取api频率表条数
	 * 
	 * @since changjiang @ 2018年7月3日
	 * @return
	 * @Return : int
	 */
	public long countAll();

	/**
	 * 批量新增
	 * 
	 * @since changjiang @ 2018年7月3日
	 * @param list
	 * @Return : void
	 */
	public void save(List<AppApiFrequency> list);

	/**
	 * 新增
	 * @param frequency
	 * @return
	 */
	public AppApiFrequency save(AppApiFrequency frequency);

	/**
	 * 根据code检索Api频率实体,优先本客户端自定义频率
	 * 
	 * @since changjiang @ 2018年7月3日
	 * @param client
	 * @param code
	 * @return
	 * @Return : AppApiFrequency
	 */
	public AppApiFrequency findByCodeWithClient(int code, AppApiClient client);

	/**
	 * 设置自定义频率
	 *
	 * @param clientId
	 * @param code
	 * @param frequencyCustom
	 * @param setAll
	 * @return
	 */
	public ApiCommonResult setFrequencyCustom(String clientId, int code, String frequencyCustom, boolean setAll);
}
