package com.trs.netInsight.support.storage;

import java.util.List;

import com.trs.netInsight.support.storage.model.Asset;

/**
 * 存储接口
 *
 * Create by yan.changjiang on 2017年11月20日
 */
public interface StorageConnector {

	/**
	 * 检查存储服务状态
	 * 
	 * @return true正常 false服务异常
	 */
	public boolean checkStorageStatus();

	/**
	 * 获取单条
	 * 
	 * @param assetId
	 *            资产id
	 * @return
	 */
	public Asset getAsset(String assetId);

	/**
	 * 获取批数据
	 * 
	 * @since slzs @ 2017年4月10日 下午2:37:30
	 * @param assetIds
	 * @return
	 */
	public List<Asset> getAssets(List<String> assetIds);

}
