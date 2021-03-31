package com.trs.netInsight.support.log.filter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.entity.SystemLogException;
import com.trs.netInsight.support.log.entity.enums.DepositPattern;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.repository.SystemLogExceptionRepository;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.ResultCode;
import com.trs.netInsight.support.log.factory.AbstractSystemLog;
import com.trs.netInsight.support.log.factory.AbstractSystemLogOperation;
import com.trs.netInsight.support.log.factory.SystemLogFactory;
import com.trs.netInsight.support.log.factory.SystemLogOperationFactory;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.NetworkUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.user.entity.User;

/**
 * 日志类，主要拦截加了log注解的，写入日志
 *
 * @Type SystemLogFilter.java
 * @Desc
 * @author 谷泽昊
 * @date 2018年7月25日 下午3:35:45
 * @version
 */
@Aspect
@Order(-99)
@Component
@Slf4j
public class SystemLogAspectFilter {

	/**
	 * 截取字符串开始值
	 */
	private final static int START = 0;

	/**
	 * 截取字符串末位值
	 */
	private final static int END = 150;

	@Autowired
	private SystemLogExceptionRepository systemLogExceptionRepository;


	ExecutorService singleThreadExecutor = Executors.newFixedThreadPool(10);


	/**
	 * 拦截被Api标注的方法
	 *
	 * @date Created at 2018年7月25日 下午5:53:11
	 * @Author 谷泽昊
	 * @param pjp
	 * @param log
	 * @return
	 * @throws TRSException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	@Around("@annotation(log)")
	public Object doAround(ProceedingJoinPoint pjp, Log log)
			throws TRSException, NoSuchMethodException, SecurityException {
		RequestAttributes ras = RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ras).getRequest();

		// 所有参数
		Map<String, String[]> parameterMap = request.getParameterMap();
		String parames = getParames(parameterMap);
		parames = paramesSizeLimited(parames);// 对parames额外处理，限制大小。
		// 类型
		SystemLogType systemLogType = log.systemLogType();
		// 具体操作
		SystemLogOperation systemLogOperation = log.systemLogOperation();
		String systemLogOperationPosition = log.systemLogOperationPosition();
		// operation position eg： 日常监测 / 环境 / 污染源新闻数据监控
		String operationPosition = getOperationPosition(systemLogType, systemLogOperation, parameterMap,
				systemLogOperationPosition);
		// 备注
		String methodDescription = StringUtil.getKeyNameFromParam(log.methodDescription(), parameterMap, "$");
		methodDescription = formatMethodDesc(methodDescription,operationPosition);
		// ip
		String requestIp = NetworkUtil.getIpAddress(request);
		// 获取客户端URI 获得请求接口路径
		String requestUri = request.getRequestURI();
		// hybase
		DepositPattern depositPattern = log.depositPattern();

		// browserInfo & osInfo
		String osAndBrowserinfo = NetworkUtil.getOsAndBrowserInfo(request);
		String osInfo = osAndBrowserinfo.split(" --- ")[0];
		String browserInfo = osAndBrowserinfo.split(" --- ")[1];
		String sessionId = request.getSession().getId();
		Integer num = getNum(request);

		Object proceed = null;
		Date startTime = new Date();
		Date endTime = null;
		// hybase查询的语句
		String trsl = null;
		User user = new User();
		try {
			Subject currentUser = SecurityUtils.getSubject();
			if (currentUser.isAuthenticated()) {
				Object principal = currentUser.getPrincipal();
				if (principal instanceof User) {
					user = (User) principal;
				}
			}
		} catch (Exception e) {
			user.setUserName("—");
		}
		try {
			proceed = pjp.proceed();
			endTime = new Date();
			trsl = getTrsl(request);
			User currentUser = UserUtils.getUser();
			long timeConsumed = endTime.getTime() - startTime.getTime();
			String simpleStatus = 500 == ResultCode.SUCCESS ? "失败" : "成功";
			SystemLog systemLog = new SystemLog(parames, methodDescription, systemLogType.getValue(),
					systemLogOperation.getValue(),systemLogOperation.getOperator(), null, requestIp, requestUri, startTime, endTime, timeConsumed,
					ResultCode.SUCCESS, simpleStatus, null, osInfo, browserInfo, sessionId, operationPosition,
					user.getUserName() == null ? UserUtils.getUser().getUserName():user.getUserName(), trsl,num,null,0);
			addUserInfo(systemLog);
			RunnableThreadTest runnableThreadTest = new RunnableThreadTest(systemLog,log.depositPattern(),currentUser);
			singleThreadExecutor.execute(runnableThreadTest);

			return proceed;
		} catch (Throwable throwable) {
			endTime = new Date();
			if(trsl == null) trsl = "searchExcepton::"+getTrsl(request);
			throwNewTRSException(throwable, depositPattern, parames, methodDescription, systemLogType,
					systemLogOperation, requestIp, requestUri, startTime, endTime, osInfo, browserInfo, sessionId,
					operationPosition, user.getUserName(), trsl);
			return throwable;
		}
	}

	private String getTrsl(HttpServletRequest request) {
		Object trslObj = request.getAttribute("system-log-trsl");
		if (trslObj instanceof String) {
			request.removeAttribute("system-log-trsl");
			return trslObj.toString();
		}
		return null;
	}

	/**
	 * 记录导出excel数量
	 * @param request
	 * @return
	 */
	private Integer getNum(HttpServletRequest request){
		String numstr = request.getParameter("num");
		if(numstr==null) return null;
		Integer num = null;
		try {
			num = Integer.parseInt(numstr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}

	/**
	 * 获取操作位置
	 *
	 * @date Created at 2018年11月7日 下午6:42:15
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param systemLogType
	 * @param parameterMap
	 * @param operationPosition
	 * @return
	 */
	private String getOperationPosition(SystemLogType systemLogType, SystemLogOperation systemLogOperation,
										Map<String, String[]> parameterMap, String operationPosition) {
		AbstractSystemLogOperation abstractSystemLogOperation = SystemLogOperationFactory
				.createSystemLogOperation(systemLogType);
		if (abstractSystemLogOperation != null) {
			return abstractSystemLogOperation.getOperationPosition(parameterMap, operationPosition, systemLogOperation);
		}

		return operationPosition;
	}

	/**
	 * 获取所有参数
	 *
	 * @date Created at 2018年7月25日 下午3:45:06
	 * @Author 谷泽昊
	 * @param reqMap
	 * @return
	 */
	private String getParames(Map<String, String[]> reqMap) {
		// 请求参数集合
		StringBuffer requestStr = new StringBuffer();

		// 遍历map的一种方法 得到参数与参数所传值键值对
		for (Map.Entry<String, String[]> entry : reqMap.entrySet()) {
			if (entry.getValue() != null && entry.getValue().length > 0) {
				if (StringUtils.equals("password", entry.getKey())
						|| StringUtils.equals("passwordAgain", entry.getKey())) {
					requestStr.append(entry.getKey()).append("=[XXXXXX],");
					continue;
				}
				requestStr.append(entry.getKey()).append("=").append(Arrays.toString(entry.getValue())).append(",");
			}
		}
		if (requestStr != null && requestStr.length() > 0) {
			requestStr.deleteCharAt(requestStr.length() - 1);
		}
		return requestStr.toString();
	}

	/**
	 * 抛出异常
	 *
	 * @date Created at 2018年7月26日 下午2:40:54
	 * @Author 谷泽昊
	 * @param throwable
	 * @param depositPattern
	 * @param requestParams
	 * @param methodDescription
	 * @param systemLogType
	 * @param systemLogOperation
	 * @param requestIp
	 * @param requestUri
	 * @param startTime
	 * @param endTime
	 * @param osInfo
	 * @param browserInfo
	 * @param sessionId
	 * @param operationPosition
	 * @param trsl
	 * @throws TRSException
	 */
	private void throwNewTRSException(Throwable throwable, DepositPattern depositPattern, String requestParams,
									  String methodDescription, SystemLogType systemLogType, SystemLogOperation systemLogOperation,
									  String requestIp, String requestUri, Date startTime, Date endTime, String osInfo, String browserInfo,
									  String sessionId, String operationPosition, String operationUserName, String trsl) throws TRSException {
		User user = UserUtils.getUser();
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(depositPattern);
		long timeConsumed = endTime.getTime() - startTime.getTime();
		String simpleStatus = 500 == ResultCode.SUCCESS ? "失败" : "成功";
		if (throwable instanceof TRSException) {
			TRSException t = (TRSException) throwable;

			SystemLog systemLog = new SystemLog(requestParams, methodDescription, systemLogType.getValue(),
					systemLogOperation.getValue(), systemLogOperation.getOperator(),null, requestIp, requestUri, startTime, endTime, timeConsumed,
					t.getCode(), simpleStatus, StringUtils.substring(t.getMessage(), START, END), osInfo, browserInfo, sessionId, operationPosition,
					operationUserName == null ? UserUtils.getUser().getUserName():operationUserName, trsl,null,null,0);
			addUserInfo(systemLog);
			RunnableThreadTest runnableThreadTest = new RunnableThreadTest(systemLog,depositPattern,user);
			singleThreadExecutor.execute(runnableThreadTest);
			try {
				SystemLogException systemLogException = new SystemLogException(systemLog.getId(),getTrace(t));
				systemLogExceptionRepository.save(systemLogException);
			} catch (Exception e) {
				log.info("--------->插入错误日志到system_log_exception表报错");
				e.printStackTrace();
			}
			throw t;
		} else {
			SystemLog systemLog = new SystemLog(requestParams, methodDescription, systemLogType.getValue(),
					systemLogOperation.getValue(),systemLogOperation.getOperator(), null, requestIp, requestUri, startTime, endTime, timeConsumed,
					ResultCode.OPERATION_EXCEPTION, simpleStatus, StringUtils.substring(throwable.getMessage(), START, END), osInfo, browserInfo, sessionId, operationPosition,
					operationUserName == null ? UserUtils.getUser().getUserName():operationUserName, trsl,null,null,0);
			addUserInfo(systemLog);
			RunnableThreadTest runnableThreadTest = new RunnableThreadTest(systemLog,depositPattern,user);
			singleThreadExecutor.execute(runnableThreadTest);
			try {
				System.out.println(getTrace(throwable));
				SystemLogException systemLogException = new SystemLogException(systemLog.getId(),getTrace(throwable));
				systemLogExceptionRepository.save(systemLogException);
			} catch (Exception e) {
				log.info("--------->插入错误日志到system_log_exception表报错");
				e.printStackTrace();
			}
			if (throwable.getMessage().contains("检索超时")){
				throw new TRSException("处理controller结果出错,message:" + throwable, CodeUtils.HYBASE_TIMEOUT,
						throwable);
			}else if (throwable.getMessage().contains("表达式过长")){
				throw new TRSException("处理controller结果出错,message:" + throwable, CodeUtils.HYBASE_EXCEPTION,
						throwable);
			}
			throw new TRSException("处理controller结果出错,message:" + throwable, ResultCode.OPERATION_EXCEPTION, throwable);
		}
	}

	private void addUserInfo(SystemLog systemLog){
// 用户
		User user = UserUtils.getUser();
		if(StringUtils.isNotBlank(user.getId())){
			systemLog.setCreatedUserId(user.getId());
		}
		if(StringUtils.isNotBlank(user.getUserName())){
			systemLog.setCreatedUserName(user.getUserName());
		}
		if(StringUtils.isNotBlank(user.getDisplayName())){
			systemLog.setDisplayName(user.getDisplayName());
		}
		if (StringUtils.isNotBlank(user.getOrganizationId())) {
			systemLog.setOrganizationId(user.getOrganizationId());
		}
		if (StringUtils.isNotBlank(user.getSubGroupId())){
			systemLog.setSubGroupId(user.getSubGroupId());
		}
	}

	/**
	 * 获得打印日志
	 * @param t
	 * @return
	 */
	public static String getTrace(Throwable t) {
		StringWriter stringWriter= new StringWriter();
		PrintWriter writer= new PrintWriter(stringWriter);
		t.printStackTrace(writer);
		StringBuffer buffer= stringWriter.getBuffer();
		return buffer.toString();
	}

	/***
	 * params在数据库中的存储字段是TEXT，最大64KB，即65536个字符
	 * UTF-8下，1个汉字可能占2个、3个、4个字符，所以统一按照最大长度65536/4 处理。
	 *
	 * @param parames
	 *            参数
	 * @return
	 */
	private String paramesSizeLimited(String parames) {
		if (StringUtil.isEmpty(parames)) {
			return parames;
		} else if (parames.length() > 16384) {
			parames = parames.substring(0, 16384);
			return parames;
		} else {
			return parames;
		}
	}

	/**
	 * 格式化方法描述,修改的时候记录原数据
	 * @return
	 */
	private String formatMethodDesc(String methodDescription,String operationPosition){
		String preStr = "修改为: ";
		String tmp[] = operationPosition.split("：");
		if(tmp[0].contains("修改") || tmp[0].contains("重命名")){
			operationPosition = operationPosition.substring(operationPosition.indexOf("："));
			int lastInd;
			if(operationPosition.contains("/")){
				lastInd = operationPosition.lastIndexOf("/")+1;
				String operationPositionPre = operationPosition.substring(0,lastInd);
				return preStr + operationPositionPre + methodDescription;
			}else return preStr + methodDescription;
		}
		return methodDescription;
	}

}
class RunnableThreadTest implements Runnable{
	private SystemLog systemLog;
	private DepositPattern depositPattern;
	private User user;

	public RunnableThreadTest(SystemLog systemLog1,DepositPattern depositPattern1,User user1){
		systemLog = systemLog1;
		depositPattern = depositPattern1;
		user = user1;
	}
	@Override
	public void run() {
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(depositPattern);
		abstractSystemLog.add(systemLog,user);
	}
}
