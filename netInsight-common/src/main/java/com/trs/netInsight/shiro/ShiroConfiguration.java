package com.trs.netInsight.shiro;

import com.trs.netInsight.shiro.filter.KickoutSessionControlFilter;
import com.trs.netInsight.shiro.filter.SimulatedLoginFilter;
import com.trs.netInsight.shiro.filter.UserExistenceFilter;
import com.trs.netInsight.shiro.manager.RedisCacheManager;
import com.trs.netInsight.shiro.manager.RedisManager;
import com.trs.netInsight.shiro.manager.RedisSessionDAO;
import com.trs.netInsight.widget.user.entity.Permission;
import com.trs.netInsight.widget.user.repository.PermissionRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Description : Apache Shiro 核心通过 Filter 来实现，就好像SpringMvc 通过DispachServlet
 * 来主控制一样。 既然是使用 Filter 一般也就能猜到，是通过URL规则来进行过滤和权限校验，所以我们需要定义一系列关于URL的规则和访问权限。
 *
 * @Type ShiroConfiguration.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年11月17日 下午7:00:47
 * @version
 */

@Configuration
@Order(value = 1)
@Slf4j
public class ShiroConfiguration {
	private static ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();


	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Value("${spring.redis.password}")
	private String password;

	@Value("${spring.redis.timeout}")
	private int timeout;
	@Value("${spring.session.timeout}")
	private int sessionTimeout;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	public static final String DEFAULT_SESSION_ID_NAME = "TRSJSESSIONID";

	/**
	 * ShiroFilterFactoryBean 处理拦截资源文件问题。
	 * 注意：单独一个ShiroFilterFactoryBean配置是或报错的，以为在
	 * 初始化ShiroFilterFactoryBean的时候需要注入：SecurityManager
	 *
	 * Filter Chain定义说明 1、一个URL可以配置多个Filter，使用逗号分隔 2、当设置多个过滤器时，全部验证通过，才视为通过
	 * 3、部分过滤器可指定参数，如perms，roles
	 *
	 */
	@Bean
	public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
		log.debug("------------------->shiroFilter");
		// 必须设置 SecurityManager
		shiroFilterFactoryBean.setSecurityManager(securityManager);

		// 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
		shiroFilterFactoryBean.setLoginUrl("/user/login");
		// 登录成功后要跳转的链接
		shiroFilterFactoryBean.setSuccessUrl("/index");
		// 未授权界面;
		shiroFilterFactoryBean.setUnauthorizedUrl("/user/forbidden");

		// 自定义拦截器
		Map<String, Filter> filtersMap = new LinkedHashMap<String, Filter>();
		// 限制同一帐号同时在线的个数。
		filtersMap.put("kickout", kickoutSessionControlFilter());
		// 判断user持续存在
		filtersMap.put("userExistence", userExistenceFilter());
		filtersMap.put("simulatedLogin", simulatedLoginFilter());
		shiroFilterFactoryBean.setFilters(filtersMap);

		// 权限控制map.
		Map<String, String> filterChainDefinitionMap = loadFilterChainDefinitions();
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}

	@Bean
	public SecurityManager securityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		// 设置realm.
		securityManager.setRealm(myShiroRealm());
		// 自定义缓存实现 使用redis
		// securityManager.setCacheManager(cacheManager());
		// 自定义session管理 使用redis
		securityManager.setSessionManager(sessionManager());
		// 注入记住我管理器;
		securityManager.setRememberMeManager(rememberMeManager());
		return securityManager;
	}

	/**
	 * 身份认证realm; (这个需要自己写，账号密码校验；权限等)
	 *
	 * @return
	 */
	@Bean
	public MyShiroRealm myShiroRealm() {
		MyShiroRealm myShiroRealm = new MyShiroRealm();
		myShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
		return myShiroRealm;
	}

	/**
	 * 配置shiro redisManager 使用的是shiro-redis开源插件
	 *
	 * @return
	 */
	public RedisManager redisManager() {
		RedisManager redisManager = new RedisManager();
		redisManager.setHost(host);
		redisManager.setPort(port);
		redisManager.setExpire(sessionTimeout);// 配置缓存过期时间
		redisManager.setPassword(password);
		redisManager.setTimeout(timeout);
		// redisManager.setPassword(password);
		return redisManager;
	}

	/**
	 * cacheManager 缓存 redis实现 使用的是shiro-redis开源插件
	 *
	 * @return
	 */
	public RedisCacheManager cacheManager() {
		RedisCacheManager redisCacheManager = new RedisCacheManager();
		redisCacheManager.setRedisManager(redisManager());
		return redisCacheManager;
	}

	/**
	 * RedisSessionDAO shiro sessionDao层的实现 通过redis 使用的是shiro-redis开源插件
	 */
	@Bean
	public RedisSessionDAO redisSessionDAO() {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisManager(redisManager());
		return redisSessionDAO;
	}

	/**
	 * Session Manager 使用的是shiro-redis开源插件
	 */
	@Bean
	public DefaultWebSessionManager sessionManager() {
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		//解决网察项目无故死掉的问题（暂时研究方向是shiro的cookie导致的）
		Cookie cookie = new SimpleCookie(DEFAULT_SESSION_ID_NAME);
		cookie.setPath("/netInsight");
		cookie.setHttpOnly(true); //more secure, protects against XSS attacks
		sessionManager.setSessionIdCookie(cookie);

		sessionManager.setSessionDAO(redisSessionDAO());
		return sessionManager;
	}

	/**
	 * cookie对象;
	 *
	 * @return
	 */
	public SimpleCookie rememberMeCookie() {
		// 这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
		SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
		// <!-- 记住我cookie生效时间30天 ,单位秒;-->
		simpleCookie.setMaxAge(2592000);
		return simpleCookie;
	}

	/**
	 * cookie管理对象;记住我功能
	 *
	 * @return
	 */
	public CookieRememberMeManager rememberMeManager() {
		CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
		cookieRememberMeManager.setCookie(rememberMeCookie());
		// rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
		cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhmFLUs0KTA3Kprsdag=="));
		return cookieRememberMeManager;
	}

	/**
	 * 凭证匹配器 （由于我们的密码校验交给Shiro的SimpleAuthenticationInfo进行处理了
	 * 所以我们需要修改下doGetAuthenticationInfo中的代码; @return
	 */
	@Bean
	public HashedCredentialsMatcher hashedCredentialsMatcher() {
		MyHashedCredentialsMatcher hashedCredentialsMatcher = new MyHashedCredentialsMatcher();
		hashedCredentialsMatcher.setHashAlgorithmName("md5");// 散列算法:这里使用MD5算法;
		hashedCredentialsMatcher.setHashIterations(2);// 散列的次数，比如散列两次，相当于md5(md5(""));
		hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);// 表示是否存储散列后的密码为16进制，需要和生成密码时的一样，默认是base64；
		return hashedCredentialsMatcher;
	}

	/**
	 * 限制同一账号登录同时登录人数控制
	 *
	 * @return
	 */
	public KickoutSessionControlFilter kickoutSessionControlFilter() {
		KickoutSessionControlFilter kickoutSessionControlFilter = new KickoutSessionControlFilter();
		// 使用cacheManager获取相应的cache来缓存用户登录的会话；用于保存用户—会话之间的关系的；
		// 这里我们还是用之前shiro使用的redisManager()实现的cacheManager()缓存管理
		// 也可以重新另写一个，重新配置缓存时间之类的自定义缓存属性
		kickoutSessionControlFilter.setCacheManager(cacheManager());
		// 用于根据会话ID，获取会话进行踢出操作的；
		kickoutSessionControlFilter.setSessionManager(sessionManager());
		// 是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；踢出顺序。
		kickoutSessionControlFilter.setKickoutAfter(false);
		// 同一个用户最大的会话数，默认1；比如2的意思是同一个用户允许最多同时两个人登录；
		kickoutSessionControlFilter.setMaxSession(1);
		// 被踢出后重定向到的地址；
		kickoutSessionControlFilter.setKickoutUrl("/kickout");
		return kickoutSessionControlFilter;
	}

	/**
	 * 存入用户
	 *
	 * @return
	 */
	public UserExistenceFilter userExistenceFilter() {
		UserExistenceFilter userExistenceFilter = new UserExistenceFilter();
		userExistenceFilter.setSessionTimeout(sessionTimeout);
		userExistenceFilter.setKickoutUrl("/isLogin");
		return userExistenceFilter;
	}

	/**
	 *  模拟登录拦截
	 * @date Created at 2018年12月11日  下午2:02:44
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @return
	 */
	public SimulatedLoginFilter simulatedLoginFilter() {
		SimulatedLoginFilter simulatedLoginFilter = new SimulatedLoginFilter();
		simulatedLoginFilter.setUserRepository(userRepository);
//		simulatedLoginFilter.setOrganizationService(organizationService);
//		simulatedLoginFilter.setUserService(userService);
		simulatedLoginFilter.setUrl("/user/forbidden");
		return simulatedLoginFilter;
	}

	/**
	 * 重新加载权限
	 */
	public void updatePermission() {

		synchronized (shiroFilterFactoryBean) {

			AbstractShiroFilter shiroFilter = null;
			try {
				shiroFilter = (AbstractShiroFilter) shiroFilterFactoryBean.getObject();
			} catch (Exception e) {
				throw new RuntimeException("get ShiroFilter from shiroFilterFactoryBean error!");
			}

			PathMatchingFilterChainResolver filterChainResolver = (PathMatchingFilterChainResolver) shiroFilter
					.getFilterChainResolver();
			DefaultFilterChainManager manager = (DefaultFilterChainManager) filterChainResolver.getFilterChainManager();

			// 清空老的权限控制
			manager.getFilterChains().clear();

			shiroFilterFactoryBean.getFilterChainDefinitionMap().clear();
			shiroFilterFactoryBean.setFilterChainDefinitionMap(loadFilterChainDefinitions());
			// 重新构建生成
			Map<String, String> chains = shiroFilterFactoryBean.getFilterChainDefinitionMap();
			for (Map.Entry<String, String> entry : chains.entrySet()) {
				String url = entry.getKey();
				String chainDefinition = entry.getValue().trim().replaceAll("\\s+", "");
				manager.createChain(url, chainDefinition);
			}
		}
	}

	/**
	 * 加载权限
	 */
	public Map<String, String> loadFilterChainDefinitions() {
		// 配置不会被拦截的链接 顺序判断
		// 配置退出过滤器,其中的具体的退出代码Shiro已经替我们实现了
		// 从数据库获取动态的权限
		// filterChainDefinitionMap.put("/add", "perms[权限添加]");
		// <!-- 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
		// <!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
		// logout这个拦截器是shiro已经实现好了的。contract
		// 从数据库获取
		// 权限控制map.从数据库获取
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
		// 获取登录页的配置
		filterChainDefinitionMap.put("/loginSuffix", "anon");
		// 获取登录页配置的logo图标或者底栏二维码
		filterChainDefinitionMap.put("/getLoginPagePic", "anon");
		// 登出
		filterChainDefinitionMap.put("/loginout", "logout");
		// 登录
		filterChainDefinitionMap.put("/login", "anon");
		//一键登录
		filterChainDefinitionMap.put("/autoLogin", "anon");
		filterChainDefinitionMap.put("/autoLoginGen", "anon");
		//大屏点击次数记录
		filterChainDefinitionMap.put("/bigScreenLog/addBigScreenLog", "anon");
		//疫情数据
		filterChainDefinitionMap.put("/yq/getTxt", "anon");
		filterChainDefinitionMap.put("/yq/getData", "anon");
		// 未授权
		filterChainDefinitionMap.put("/kickout", "anon");
		// 没有权限
		filterChainDefinitionMap.put("/user/forbidden", "anon");
		// form表单登录
		filterChainDefinitionMap.put("/doLogin", "anon");
		// 获取模拟登录的token
		filterChainDefinitionMap.put("/getSimulatedLoginToken", "anon");
		// 测试原生检索
		filterChainDefinitionMap.put("/analysis/text/search", "anon");
		// websocket
		// filterChainDefinitionMap.put("/websocket/info", "anon");
		filterChainDefinitionMap.put("/websocket/**", "anon");
		filterChainDefinitionMap.put("/chat", "anon");
		// 测试 微信
		filterChainDefinitionMap.put("/system/weixin/**", "anon");
		// 模板
		filterChainDefinitionMap.put("/thymeleaf/**", "anon");
		filterChainDefinitionMap.put("/**.html", "anon");
		// 获取验证码
		filterChainDefinitionMap.put("/getKaptchaImage", "anon");
		//自动注册登录账号/机构
//		filterChainDefinitionMap.put("/autoLogin", "anon");
		// 接口swagger
		filterChainDefinitionMap.put("/swagger-ui.html/**", "anon");
		filterChainDefinitionMap.put("/webjars/**", "anon");
		filterChainDefinitionMap.put("/swagger-resources/**", "anon");
		filterChainDefinitionMap.put("/v2/**", "anon");
		// openApi
		filterChainDefinitionMap.put("/api/method/**", "anon");
		// appOpenApi
		filterChainDefinitionMap.put("/ewm/**", "anon");
		filterChainDefinitionMap.put("/app/**", "anon");
		filterChainDefinitionMap.put("/apk/**", "anon");
		// 微信登录二维码
		filterChainDefinitionMap.put("/system/qrcode/createQrcodeLogin", "anon");
		filterChainDefinitionMap.put("/system/qrcode/loginByQrcode", "anon");
		filterChainDefinitionMap.put("/system/qrcode/checkStatus", "anon");
		// 是否登录
		filterChainDefinitionMap.put("/isLogin", "kickout,anon,simulatedLogin");
		filterChainDefinitionMap.put("/chat", "anon");
		// filterChainDefinitionMap.put("/organization/**",
		// "kickout,authc,userExistence");
		// 登录页 全网搜索
		filterChainDefinitionMap.put("/home/netSearch", "anon");
		// 登录页 政企申请试用
		filterChainDefinitionMap.put("/home/applyForTry", "anon");
		// 广告页申请试用
		filterChainDefinitionMap.put("/home/applyForAdvertising", "anon");
		// 申请信息入库
		filterChainDefinitionMap.put("/apply/addApplyUser", "anon");
		//返乡日记
		filterChainDefinitionMap.put("/homecoming/**", "anon");

		// bigCreeen
		filterChainDefinitionMap.put("/bigScreen/**", "anon");

		//权限改造后迁移历史数据
		filterChainDefinitionMap.put("/group/changHistoryDataForUserAndOrg", "anon");
		filterChainDefinitionMap.put("/group/changHistoryDataForColumn", "anon");
		filterChainDefinitionMap.put("/group/changHistoryDataForSpecial", "anon");
		filterChainDefinitionMap.put("/group/changHistoryDataForAlert", "anon");
		filterChainDefinitionMap.put("/group/changHistoryDataForReport", "anon");
		filterChainDefinitionMap.put("/group/changHistoryDataForOther", "anon");

		//疫情app
		filterChainDefinitionMap.put("/app/api/selectAppPageTitle", "anon");
		filterChainDefinitionMap.put("/app/api/selectYiQing", "anon");
		filterChainDefinitionMap.put("/pdfpath/png/*", "anon");
		filterChainDefinitionMap.put("/pdfpath/picture/*", "anon");
		//舆情智库列表
		filterChainDefinitionMap.put("/thinkTank/pageList", "anon");
		filterChainDefinitionMap.put("/thinkTank/picture/*", "anon");
		//舆情智库详情
		filterChainDefinitionMap.put("/thinkTank/pdf/*", "anon");
		filterChainDefinitionMap.put("/thinkTank/png/*", "anon");
		//filterChainDefinitionMap.put("/index.html/**", "anon");
		//知识库录入
		filterChainDefinitionMap.put("/knowledgeBase/saveKnowledge", "anon");
		// 从数据库获得权限 --->过滤器中的权限,登录/同意用户在线...
		List<Permission> permissions = permissionRepository.findAll();
		if (permissions != null && permissions.size() > 0) {
			for (Permission permission : permissions) {
				filterChainDefinitionMap.put(permission.getUrl(),
						"perms[" + permission.getPerms() + "],kickout,authc,userExistence,simulatedLogin");
			}
		}
		// 最后的拦截，一定要放在最后
		filterChainDefinitionMap.put("/**", "kickout,authc,userExistence,simulatedLogin");
		log.debug("------------>加载访问权限: anon,kickout,authc,userExistence,simulatedLogin");
		return filterChainDefinitionMap;
	}

}
