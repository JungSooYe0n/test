/*
 * Project: netInsight
 * 
 * File Created at 2018年3月2日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.ckm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trs.ckm.soap.AbsTheme;
import com.trs.ckm.soap.CkmSoapException;
import com.trs.ckm.soap.CluClsInfo;
import com.trs.ckm.soap.SegDictWord;
import com.trs.netInsight.support.ckm.entity.*;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;

/**
 * @Desc CKM 服务接口
 * @attention 服务中使用的API接口参数类皆源自trs.ckm.soap包,具体类内属性及方法本服务不在赘述, 详细说明见《TRS CKM
 *            SOAP Java API V6.0 用户手册》,<font color='red'>成型接口谨慎修改!</font>
 * @author changjiang
 * @date 2018年3月2日 下午4:41:35
 * @version 1.0.1
 */
public interface ICkmService {

	/**
	 * 对内容内实体词进行词频统计,返回前topN个
	 * 
	 * @param words
	 *            内容
	 * @param topN
	 *            返回前N条,Integer.maxValue为返回全部(不建议使用)
	 * @param entityType
	 *            请引用{@link com.trs.netInsight.support.ckm.entity.WordType}
	 *            中TYPE_CODE值
	 * @return
	 */
	public Map<String, Integer> statisticsEntity(String words, int topN, int entityType) throws CkmSoapException;

	/**
	 * 传统媒体文章聚类
	 * 
	 * @param docs
	 *            传统媒体文章列表
	 * @param clusterType
	 *            聚类类别,默认使用按照正文聚类(0),其他:1=按照标题聚类;2=按照摘要聚类;可根据需要进行对应扩展
	 * @return
	 * @throws CkmSoapException
	 */
	public List<CluClsInfo> cluster(List<FtsDocument> docs, int clusterType) throws CkmSoapException;

	/**
	 * 微博文章聚类
	 * 
	 * @param docs
	 *            传统媒体文章列表
	 * @param clusterType
	 *            聚类类别,默认使用按照正文聚类(0)
	 * @return
	 * @throws CkmSoapException
	 */
	public List<CluClsInfo> clusterStatus(List<FtsDocumentStatus> docs, int clusterType) throws CkmSoapException;

	/**
	 * 微信文章聚类
	 * 
	 * @param docs
	 *            传统媒体文章列表
	 * @param clusterType
	 *            聚类类别,默认使用按照正文聚类(0),1=按照标题聚类
	 * @return
	 * @throws CkmSoapException
	 */
	public List<CluClsInfo> clusterWeChat(List<FtsDocumentWeChat> docs, int clusterType) throws CkmSoapException;

	/**
	 * k-means聚类
	 * 
	 * @param corpus
	 *            语料,key-means集
	 * @param featureSize
	 *            特征数
	 * @param k
	 *            聚类数
	 * @return
	 * @throws CkmSoapException
	 */
	public ClusterInfo[] clusterByKMeans(List<IdText> corpus, int featureSize, int k) throws CkmSoapException;

	/**
	 * 抽取文本内容中的实体词
	 * 
	 * @param content
	 * 			文本
	 * @param maxKeyword
	 * 			最大抽取关键词个数
	 * @return
	 * @throws CkmSoapException
	 */
	public Map<String, Set<String>> ploText(String content, int maxKeyword) throws CkmSoapException;

	/**
	 * 按类型（ploType）抽取文本内容中的实体词
	 * 
	 * @param content
	 * 			文本
	 * @param maxKeyword
	 * 			最大抽取关键词个数
	 * @param ploType
	 * 			抽取关键词类型   1——人名/地名/机构名
	 * 						2——时间、MSN、email、QQ、车牌、护照号、身份证号、电话号码等有意义的数字信息
	 * 						4——案件
	 * 						8——房屋
	 * @return
	 * @throws CkmSoapException
	 */
	public Map<String, Set<String>> ploTextDetail(String content, int maxKeyword, String ploType) throws CkmSoapException;
	
	/**
	 * 对指定文本进行分词
	 * 
	 * @param content
	 * @return
	 * @throws CkmSoapException
	 */
	public List<Word> segment(String content) throws CkmSoapException;

	/**
	 * 抽取指定文本的主题词
	 * 
	 * @param content
	 * @param topN
	 *            返回指定条数
	 * @return
	 * @throws CkmSoapException
	 */
	public List<AbsTheme> theme(String content, int topN) throws CkmSoapException;

	/**
	 * 对文本集进行抽取主题词
	 * 
	 * @param contents
	 * @return
	 * @throws CkmSoapException
	 */
	public List<AbsTheme> theme(List<String> contents) throws CkmSoapException;

	/**
	 * 语料相似度计算,基本应用于热点话题与热点事件的抽取
	 * 
	 * @param corpus
	 *            语料集
	 * @param ration
	 *            相似度阈值(只返回阈值以上的数据,建议阈值范围0.35f-0.25f)
	 * @return
	 * @throws CkmSoapException
	 */
	public List<List<AnalysisValue>> getSimDatas(Map<String, SimData> corpus, float ration) throws CkmSoapException;

	/**
	 * 对文本进行多层分类,分类结果包含置信度
	 * 
	 * @param mod
	 *            模板名称(默认使用'mod_zb'分类模板)
	 * @param text
	 *            分类文本
	 * @return
	 * @throws CkmSoapException
	 */
	public Map<String, Integer> classify(String mod, String text) throws CkmSoapException;
	
	/**
	 * 计算MD5值
	 * @date Created at 2018年4月10日  下午4:06:40
	 * @Author 谷泽昊
	 * @param content 正文字段
	 * @return
	 * @throws CkmSoapException
	 */
	public String simMD5GenerateTheme(String content) throws CkmSoapException;
	
	/**
	 * 情感划分
	 * @date Created at 2018年7月13日
	 * @Author liang.xin
	 * @param content 需要进行判别的字段
	 * @return Map<String, Integer>
	 * 			形式为Map<"">
	 * @throws CkmSoapException
	 */
	public Map<String, Integer> emotionDivide(String content) throws CkmSoapException;

	/**
	 * @param sContent 语句
	 * @return
	 * @throws CkmSoapException
	 */
	public List<SegWord> SegMakeWord(String sContent) throws CkmSoapException;
	
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 服务中使用的API接口参数类皆源自trs.ckm.soap包,具体类内属性及方法本服务不在赘述,详细说明见《TRS CKM SOAP Java API
 * V6.0 用户手册》 Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月2日 changjiang create
 */