package com.trs.netInsight.support.appApi.entity.repository;

import com.trs.netInsight.support.appApi.entity.ApkInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * ApkInfo持久层服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since duhq 2019/05/10
 *
 */
@Repository
public interface IApkInfoRepository extends JpaRepository<ApkInfo, String>, JpaSpecificationExecutor<ApkInfo> {

	//获取最新版本apk的数据
	public ApkInfo findByVersion(String version);

	//获取最新版本的apk
	public ApkInfo findFirstByOrderByVersioncodeDesc();

}
