package com.trs.netInsight.support.log.factory;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.data.domain.Page;

import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.repository.MysqlSystemLogRepository;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IUserService;

/**
 * 日志检索抽象类
 * 
 * @Type AbstractSystemLog.java
 * @Desc
 * @author 北京拓尔思信息技术股份有限公司
 * @author 谷泽昊
 * @date 2018年11月7日 上午10:37:45
 * @version
 */
public abstract class AbstractSystemLog {

	protected MysqlSystemLogRepository mysqlSystemLogRepository;

	protected SessionDAO sessionDAO;

	protected IUserService userService;

	protected IOrganizationService organizationService;

	/**
	 * 初始化
	 * @date Created at 2018年11月7日  上午11:09:08
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param mysqlSystemLogRepository
	 * @param sessionDAO
	 * @param userService
	 * @param organizationService
	 */
	public void init(MysqlSystemLogRepository mysqlSystemLogRepository, SessionDAO sessionDAO, IUserService userService,
			IOrganizationService organizationService) {
		this.mysqlSystemLogRepository = mysqlSystemLogRepository;
		this.sessionDAO = sessionDAO;
		this.userService = userService;
		this.organizationService = organizationService;
	}

	/**
	 * 添加日志
	 * 
	 * @date Created at 2018年7月26日 下午2:42:19
	 * @Author 谷泽昊
	 * @param systemLog requestParams
	 *            参数
	 * @param systemLog methodDescription
	 *            备注
	 * @param systemLog systemLogType
	 *            日志类型
	 * @param systemLog systemLogOperation
	 *            具体操作
	 * @param systemLog systemLogTabId
	 *            日常监测为：栏目分组id
	 * @param systemLog requestIp
	 *            ip
	 * @param systemLog requestUri
	 *            具体类型
	 * @param systemLog startTime
	 *            开始时间
	 * @param systemLog endTime
	 *            结束时间
	 * @param systemLog systemLogstatus
	 *            状态
	 * @param systemLog exceptionDetail
	 *            错误信息
	 * @param systemLog osInfo
	 * @param systemLog browserInfo
	 * @param systemLog sessionId
	 * @param systemLog operationPosition
	 * @param systemLog trsl
	 * @return
	 */
	public abstract SystemLog add(SystemLog systemLog);

	/**
	 * 查看当前运维人员下的所有机构；超管看所有
	 * 
	 * @date Created at 2018年11月6日 下午4:58:05
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param id
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public abstract Map<String, Object> findorgList(String retrievalCondition, String retrievalInformation, String id,
			Integer pageNum, Integer pageSize,Integer onLine,Integer sortBy,String ascDesc,String organizationType) throws Exception;

	/**
	 * 当前机构下所有用户的操作日志
	 * 
	 * @date Created at 2018年11月6日 下午4:58:17
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param organizationId
	 * @param timeLimited
	 * @param userId
	 * @param operation
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public abstract Page<SystemLog> findCurOrgLogs(String organizationId, String timeLimited, String userId,
			String operation,String operationDetail,String createTimeOrder,String simpleStatus, Integer pageNum, Integer pageSize);

	/**
	 * 查看机构下所有用户
	 * 
	 * @date Created at 2018年11月6日 下午4:58:25
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public abstract List<Map<String, String>> curOrgUsers(String organizationId);

	/**
	 * 日志统计分析
	 * 
	 * @date Created at 2018年11月6日 下午4:58:35
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param id
	 * @param timeLimited
	 * @param operation
	 * @param timeConsumed
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public abstract Map<String, Object> logTimeStatistics(String id, String timeLimited, String operation,
			String timeConsumed, String retrievalCondition, String retrievalInformation,String createTimeOrder, Integer pageNum,
			Integer pageSize);

	/**
	 * 查看单个日志
	 * 
	 * @date Created at 2018年11月6日 下午4:57:29
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param id
	 * @return
	 */
	public abstract SystemLog findById(String id);

	/**
	 * 浏览器图
	 * 
	 * @date Created at 2018年11月6日 下午4:57:20
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @return
	 */
	public abstract List<Map<String, Object>> browserUsagePer();

	/**
	 * ip占比
	 * @return
	 */
	public abstract List<Map<String, Object>> ipPer();

}
