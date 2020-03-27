package com.trs.netInsight.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.trs.netInsight.support.fts.model.factory.HybaseFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
/**
 * 记录hybase日志的工具类
 * @author xiaoying
 *
 */
@Slf4j
@Component
public class LogPrintUtil implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	/**
	 * 
	 */
	
	public final static String INFO_LIST = "列表";
	public final static String TREND_MESSAGE = "信息走势图";
	public final static String ACTIVE_LEVEL = "媒体活跃等级图";
	public final static String TREND_TIME = "走势第一条";
	public final static String TREND_MD5 = "走势后边九条";
	public final static String NET_TENDENCY = "网民参与趋势图";
	public final static String META_TENDENCY = "媒体参与趋势图";
	public final static String VOLUME = "情感分析曲线趋势图";
	public final static String TIPPING_POINT = "引爆点";
	public final static String USER_VIEWS = "网友观点";
	public final static String TOPIC_EVO_EXPLOR = "话题演变探索";
	public final static String PATH_BY_NEWS = "传统媒体传播路径分析图";
	public final static String NEWS_SITE_ANALYSIS = "传统媒体传播站点分析图";
	public final static String Column_LIST = "日常监测列表";
	
	/**
	 * 当前线程id
	 */
	@Getter
	@Setter
	public long threadId;
	
	/**
	 * 进入后台的时间
	 */
	@Getter
	@Setter
	public long comeBak;
	
	/**
	 * 走完后台的时间
	 */
	@Getter
	@Setter
	public long finishBak;
	
	/**
	 * 整个后台用了多少时间
	 */
	@Getter
	@Setter
	public int fullBak;
	
	@Getter
	@Setter
	public List<LogEntity> logList = new ArrayList<>();;
	
	
	/**
	 * 调用hybaseApi一共用多长时间
	 */
	@Getter
	@Setter
	public int fullHybase;
	
	
	/**
	 * 调用HybaseApi次数
	 */
	@Getter
	@Setter
	public int count;
	
	public void printTime(String chartName){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		String comeBakFormat = simpleDateFormat.format(comeBak);
		String finishBakFormat = simpleDateFormat.format(finishBak);
		log.debug("\n 模块："+chartName
				+"\n 进入后台的时间 "+comeBakFormat
				+"\n 走完后台的时间 "+finishBakFormat
				+"\n 整个后台用了多少时间 "+fullBak
				+"\n 调用完hybaseApi  "+count+" 次"
				+"\n 调用hybaseApi一共用多长时间 "+fullHybase
				+" \n 每次具体情况"+logList
				);
	}
	
	/**
	 * 获取海贝变量
	 * @date Created at 2018年7月25日  下午4:08:49
	 * @Author 谷泽昊
	 * @param chartName
	 * @param comeBak
	 * @param finishBak
	 * @return
	 */
	public String printTime(String chartName,long comeBak,long finishBak){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		String comeBakFormat = simpleDateFormat.format(comeBak);
		String finishBakFormat = simpleDateFormat.format(finishBak);
		return "模块："+chartName
				+"---进入后台的时间 "+comeBakFormat
				+"---走完后台的时间 "+finishBakFormat
				+"---整个后台用了多少时间 "+fullBak
				+"---调用完hybaseApi  "+count+" 次"
				+"---调用hybaseApi一共用多长时间 "+fullHybase
				+"---每次具体情况"+logList;
	}
}
