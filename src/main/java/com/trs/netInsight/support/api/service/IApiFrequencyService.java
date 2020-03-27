package com.trs.netInsight.support.api.service;

import java.util.List;

import com.trs.netInsight.support.api.entity.ApiClient;
import com.trs.netInsight.support.api.entity.ApiFrequency;
import com.trs.netInsight.support.api.result.ApiCommonResult;

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
	 * @Return : List<ApiFrequency>
	 */
	public List<ApiFrequency> findAll();
	public ApiFrequency findByCodeWithInit(int code);

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
	public void save(List<ApiFrequency> list);

	/**
	 * 新增
	 * @param frequency
	 * @return
	 */
	public ApiFrequency save(ApiFrequency frequency);

	/**
	 * 根据code检索Api频率实体,优先本客户端自定义频率
	 * 
	 * @since changjiang @ 2018年7月3日
	 * @param client
	 * @param code
	 * @return
	 * @Return : ApiFrequency
	 */
	public ApiFrequency findByCodeWithClient(int code, ApiClient client);

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
