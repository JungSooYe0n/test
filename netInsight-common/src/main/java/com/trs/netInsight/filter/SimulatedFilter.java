package com.trs.netInsight.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * 过滤OPTIONS请求
 * 
 * @Type SimulatedFilter.java
 * @Desc
 * @author 北京拓尔思信息技术股份有限公司
 * @author 谷泽昊
 * @date 2018年12月11日 下午2:30:06
 * @version
 */
@Setter
@Component
public class SimulatedFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		if (StringUtils.equals(request.getMethod(), "OPTIONS")) {
			// servletRequest.getRequestDispatcher("/error").forward(servletRequest,
			// servletResponse);
		    response.setHeader("Access-Control-Allow-Headers", "Content-Type,Simulated-Login-Token");
			request.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("ok");
			out.close();
		}else{
			// 放行
			chain.doFilter(servletRequest, servletResponse);
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
