package com.trs.netInsight.widget.base.enums;

import lombok.Getter;

/**
 * ES GroupName 枚举 "国内新闻", "国内论坛", "国内新闻_手机客户端", "国内新闻_电子报", "国内博客", "微博", "微信";
 *
 * Create by yan.changjiang on 2017年11月22日
 */
public enum ESGroupName {

	/**
	 * 国内新闻
	 */
	NEWS_IN("国内新闻"),

	/**
	 * 国内论坛
	 */
	FORUM_IN("国内论坛"),

	/**
	 * 国内新闻_手机客户端
	 */
	NEWS_IN_APP("国内新闻_手机客户端"),

	/**
	 * 国内新闻_电子报
	 */
	NEWS_IN_DAINZIBO("国内新闻_电子报"),

	/**
	 * 国内博客
	 */
	BLOG_IN("国内博客"),

	/**
	 * 微博
	 */
	WEIBO_SINA("微博"),

	/**
	 * 微信
	 */
	WEIXIN("国内微信"),

	/**
	 * 媒体，论坛，微博等
	 */

	MediaType("国内新闻", "国内论坛", "国内新闻_手机客户端", "国内新闻_电子报", "国内博客", "微博", "国内微信"),
	
	// MediaType("国内新闻", "微博", "微信"),
	MediaTypeENoWeiXin("国内新闻", "国内论坛", "国内新闻_手机客户端", "国内新闻_电子报", "国内博客", "微博"), MOTION("新闻", "微博", "微信", "论坛", "博客",
			"客户端", "电子报"), ALLNEWS("国内新闻", "国内新闻_手机客户端", "国内新闻_电子报"), NEWS("国内新闻", "国内新闻_手机客户端", "国内新闻_电子报", "港澳台新闻");

	@Getter
	private String[] allMedias;
	@Getter
	private String name;

	ESGroupName(String... allMedias) {
		this.allMedias = allMedias;
	}

	ESGroupName(String name) {
		this.name = name;
	}
}
