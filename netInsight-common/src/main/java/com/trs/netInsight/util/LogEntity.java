package com.trs.netInsight.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import lombok.Getter;
import lombok.Setter;

public class LogEntity implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -8115656817856897148L;

	/**
	 * 
	 */

	/**
	 * 开始调用hybaseApi的时间
	 */
	@Getter
	@Setter
	public long comeHybase;
	
	/**
	 * 调用完hybaseApi的时间
	 */
	@Getter
	@Setter
	public long finishHybase;
	
	/**
	 * 链接Hybase用了多久
	 */
	@Getter
	@Setter
	public int connectHybase;
	
	/**
	 * 每次查Hybase用了多久
	 */
	@Getter
	@Setter
	public int everyHybase;
	
	/**
	 * hybase检索表达式
	 */
	@Getter
	@Setter
	public String trsl;
	
	/**
	 * 性质  分类统计  查找  计数
	 */
	@Getter
	@Setter
	public String type;
	
	/**
	 * 数据库
	 */
	@Getter
	@Setter
	public String db;
	
	
	/**
	 * 分类统计的字段
	 */
	@Getter
	@Setter
	public String field;
	
	
	/**
	 * hybase节点
	 */
	@Getter
	@Setter
	public String link = "startLog";
	
	@Override
	public String toString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		String comeHybaseFormat = simpleDateFormat.format(comeHybase);
		String finishHybaseFormat = simpleDateFormat.format(finishHybase);
		String result = "\n 开始调用hybaseApi的时间 "+comeHybaseFormat
				+"\n 调用完hybaseApi的时间 "+finishHybaseFormat
				+"\n 本次查Hybase用了多久 "+everyHybase
				+"\n 链接Hybase本次用了多久 "+connectHybase
				+"\n hybase检索表达式  "+ trsl
				+"\n hybase链接节点 "+ link
				+"\n 查询的数据库为  "+ db
				+"\n 性质 "+ type;
		if("分类统计".equals(type)){
			result += "\n 分类统计的字段为  "+ field;
		}
		return result;
				
	}
	
}
