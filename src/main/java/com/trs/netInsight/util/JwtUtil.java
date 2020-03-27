/*
 * Project: netInsight
 * 
 * File Created at 2018年9月10日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.trs.netInsight.widget.user.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 
 * @Type JwtUtil.java
 * @author 谷泽昊
 * @date 2018年9月10日 下午4:43:06
 * @version
 */
public class JwtUtil {
	/**
	 * token秘钥，请勿泄露，请勿随便修改
	 */
	private static final String SECRET = "netInsightJwt300229";
	/**
	 * token 过期时间: 10天
	 */
	private static final int calendarField = Calendar.DATE;
	private static final int calendarInterval = 10;

	public static String getJwtToken(Map<String, Object> claims) {
		// expire time
		Calendar nowTime = Calendar.getInstance();
		nowTime.add(calendarField, calendarInterval);
		String token = Jwts.builder()
				// 传参
				.setClaims(Jwts.claims(claims))
				// 设置有效期
				// .setExpiration( nowTime.getTime())
				// 加密方式
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
		return token;
	}

	public static Map<String, Object> parseJwtToken(String token) {
		Jws<Claims> jws = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
//		String signature = jws.getSignature();
//		Map<String, String> header = jws.getHeader();
		Map<String, Object> claims=new HashMap<>();
		claims.putAll(jws.getBody());
		return claims;
	}
	
	public static void main(String[] args) {
		Map<String, Object> claims=new HashMap<>();
		claims.put("id", "userId");
		User user=new User();
		user.setEmail("safdfsdfsdfsdf");
		claims.put("user", user);
		String jwtToken = getJwtToken(claims);
		System.err.println(jwtToken);
		Map<String, Object> map = parseJwtToken(jwtToken);
		System.err.println(map);
		
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月10日 谷泽昊 creat
 */