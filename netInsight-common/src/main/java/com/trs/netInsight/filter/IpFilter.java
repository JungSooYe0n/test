package com.trs.netInsight.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.stereotype.Component;

import com.trs.netInsight.util.NetworkUtil;

/**
 *  登录ip
 * @Type IpFilter.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年12月6日 上午10:11:11
 * @version
 */
@Component
public class IpFilter implements Filter {
	private FilterConfig config;

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	/*
	 * 1. 获取Map 2. 获取请求IP 3. 查看IP在Map中是否存在 4. 如果存在，把访问次数+1，再保存回去 5.
	 * 如果不存在，添加键值，键为IP，值为1 6. 放行！
	 */	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
//		String ip = NetworkUtil.getIpAddress((HttpServletRequest)request);
//		// 获取ServletConfig
//		ServletContext sc = com.trs.netInsight.config.getServletContext();
//		// 获取ServletContext中的map
//		@SuppressWarnings("unchecked")
//		Map<String, Integer> map = (Map<String, Integer>) sc.getAttribute("map");
//		// 如果map不存在，说明这是第一次被访问
//		if (map == null) {
//			// 创建map
//			map = new LinkedHashMap<String, Integer>();
//		}
//		// 获取请求ip
//		// 判断map中是否存在这个ip
//		if (map.containsKey(ip)) {
//			// 如果ip存在，说明这个IP已经访问过本站
//			// 获取访问次数
//			Integer count = map.get(ip);
//			// 把访问次数+1
//			count++;
//			// 把新的访问次数保存回去
//			map.put(ip, count);
//		} else {
//			// 因为这个IP是第一次访问，所以值为1
//			map.put(ip, 1);
//		}
//		// 把map放入ServletContext中
//		sc.setAttribute("map", map);
		// 放行
		chain.doFilter(request, response);
	}

	public void destroy() {

	}
	
	
}
