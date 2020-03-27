package com.trs.netInsight.support.storage.impl;

import java.util.List;

import com.trs.netInsight.support.storage.StorageConnector;
import com.trs.netInsight.support.storage.model.Asset;

/**
 * MongoDB
 *
 * Create by yan.changjiang on 2017年11月20日
 */
public class MongoDBConnector implements StorageConnector {

	/**
	 * @see com.trs.support.storage.StorageConnector#checkStorageStatus()
	 * @since slzs @ 2017年4月10日 下午2:38:18
	 * @return
	 */
	@Override
	public boolean checkStorageStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see com.trs.support.storage.StorageConnector#getAsset(java.lang.String)
	 * @since slzs @ 2017年4月10日 下午2:38:18
	 * @param assetId
	 * @return
	 */
	@Override
	public Asset getAsset(String assetId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.trs.support.storage.StorageConnector#getAssets(java.util.List)
	 * @since slzs @ 2017年4月10日 下午2:38:18
	 * @param assetIds
	 * @return
	 */
	@Override
	public List<Asset> getAssets(List<String> assetIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
