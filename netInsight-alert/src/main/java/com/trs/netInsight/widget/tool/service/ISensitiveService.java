package com.trs.netInsight.widget.tool.service;

import java.util.Date;
import java.util.List;
import java.util.Map;


import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.model.result.GroupInfo;

/**
 * 文章敏感模态分析服务接口
 * <p> Be careful!本标准适用于所有单篇文章检索服务<br>
 * 本服务内单篇计算接口需要的trsl表达式,应存在以下特征<br>
 * 1:该表达式应指向明确的一篇文章<br>
 * 2:该表达式应有明确的时间检索范围,格式示例:'IR_URLTIME:[20180815000000 TO 20180816000000]'<br>
 *
 * </p>
 *
 * @author changjiang
 * @date created at 2018年8月15日11:52:20
 * @since 北京拓尔思信息技术股份有限公司
 */
public interface ISensitiveService {

    /**
     * 计算单篇文章敏感分类
     *
     * @param trsl
     * @return
     */
    public Object getSensitiveType(String trsl) throws TRSException;

    /**
     * 计算单篇文章敏感程度
     *
     * @param trsl
     * @return
     */
    public Object getSensitiveDegree(String trsl) throws TRSException;

	/**
	 *  计算单篇文章热度
	 * @date Created at 2018年8月15日  下午4:53:53
	 * @Author 谷泽昊
	 * @param md5tag 文章的MD5值
	 * @param time 文章的时间
	 * @return
	 * @throws TRSException
	 */
	public float getHotDegree(String md5tag,Date date) throws TRSException;

    /**
     * 计算单篇文章危险倾向
     *
     * @param trsl
     * @param sensitiveDegree
     * @param getHotDegree
     * @return
     */
    public Object getTendencyTendency(String trsl, Object sensitiveDegree, Object getHotDegree) throws TRSException;

    /**
     * 计算单篇文章敏感分类,敏感程度,热度,及危险倾向
     *
     * @param trsl
     * @return
     */
    public Object computeSensitive(String trsl) throws TRSException;

    /**
     * 根据文章id获取趋势图
     * @date Created at 2018年8月16日  下午2:59:55
     * @Author 谷泽昊
     * @param md5
     * @param date 时间
     * @throws TRSException getPie
     */
	public Map<String, Object> getTrendMap(String md5, Date date) throws TRSException;

	
	/**
	 * 获取文章情感正负面
	 * @date Created at 2018年8月16日  下午3:53:20
	 * @Author 谷泽昊
	 * @param md5tag
	 * @param date
	 * @return
	 * @throws TRSException 
	 */
	public Map<String, Long> getEmotion(String md5, Date date) throws TRSException;
	
	/**
	 * 获取文章详情页饼图信息
	 * @param sid
	 * @param md5
	 * @param trsl
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public List<GroupInfo> getPie(String sid, String md5, String trsl) throws TRSException, TRSSearchException;

	/**
	 * 获取文章详情页饼图信息（缓存丢失时）
	 * @param date
	 * @param md5
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public List<GroupInfo> getPieNoCache(Date date, String md5) throws TRSException, TRSSearchException;

}
