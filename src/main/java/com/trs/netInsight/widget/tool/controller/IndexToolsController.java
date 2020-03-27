package com.trs.netInsight.widget.tool.controller;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.widget.tool.service.ISensitiveService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.Date;

/**
 * 工具箱控制器
 *
 * @author changjiang
 * @date created at 2018年8月15日11:43:52
 * @since 北京拓尔思信息技术股份有限公司
 */
@RestController
@RequestMapping("/indexTools")
public class IndexToolsController {

	@Autowired
	private ISensitiveService sensitiveService;


    /**
     * 单篇文章敏感指数,包含敏感分类,敏感程度,热度,敏感倾向
     *
     * @param trslk 表达式缓存
     * @param sid   文章id
     * @param md5   文章MD5
     * @return
     */
    @FormatResult
    @ApiOperation("单篇文章敏感指数")
    @GetMapping("/getSensitiveDegree")
    public Object getSensitiveDegree(@ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
                                   @ApiParam("sid") @RequestParam("sid") String sid,
                                   @ApiParam("urlTime") @RequestParam(value = "urlTime", required = false) long urlTime ) throws TRSException {

        if (StringUtils.isBlank(trslk) && urlTime <= 0l){
            throw new TRSException("getSensitiveType error : 生成trsl失败,参数缺失!",500);
        }
        QueryBuilder builder = new QueryBuilder();
        builder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
        if (StringUtils.isNotBlank(trslk)){
            String trsl = RedisUtil.getString(trslk);
            if (StringUtils.isNotBlank(trsl)) {
                builder.filterByTRSL(trsl);
            }
        }else if(urlTime>0l){
            String[] timeInterval = DateUtil.getTimeInterval(new Date((urlTime)), DateUtil.yyyyMMddHHmmss, 1);
            if (timeInterval != null && timeInterval.length == 2){
                builder.setStartTime(DateUtil.stringToDate(timeInterval[0],DateUtil.yyyyMMddHHmmss));
                builder.setEndTime(DateUtil.stringToDate(timeInterval[1],DateUtil.yyyyMMddHHmmss));
            }
        }
        if (StringUtils.isNotBlank(builder.asTRSL())) {
            return this.sensitiveService.computeSensitive(builder.asTRSL());
        }else{
            throw new TRSException("getSensitiveType error : 生成trsl失败",500);
        }
    }

	/**
	 * 获取文章趋势图
	 *
	 * @date Created at 2018年8月16日 下午2:58:15
	 * @Author 谷泽昊
	 * @param sid
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@ApiOperation("获取文章趋势图")
	@GetMapping("/getTrendMap")
	public Object getTrendMap(@ApiParam("md5") @RequestParam("md5") String md5,
			@ApiParam("时间，格式为：时间戳") @RequestParam("urlTime") long urlTime) throws TRSException {
		return sensitiveService.getTrendMap(md5, new Date(urlTime));
	}
    
    /**
     * 获取饼图（按站点统计）
     * @date Created at 2018年8月16日  下午6:37:30
     * @Author liangxin
     * @param sid
     * @param trslk
     * @return
     * @throws TRSException 
     */
    @FormatResult
    @ApiOperation("获取饼图")
    @GetMapping("/getPie")
    public Object getPie(
    		@ApiParam("此篇文章的sid") @RequestParam(value = "sid", required = true) String sid,
    		@ApiParam("此篇文章的md5") @RequestParam(value = "md5", required = true) String md5,
    		@ApiParam("此篇文章的urltime") @RequestParam(value = "urlTime", required = false) long urlTime,
    		@ApiParam("trslk——缓存检索表达式的K") @RequestParam(value = "trslk", required = false) String trslk) throws TRSException, TRSSearchException{
	     String trsl = RedisUtil.getString(trslk);
	     if (StringUtils.isNotBlank(trsl)) {
	    	 return sensitiveService.getPie(sid, md5, trsl);
	     }else if(urlTime > 0l){  
	    	 return sensitiveService.getPieNoCache(new Date(urlTime), md5);
	     }else{
            throw new TRSException("getPie error : 获取trsl失败",500);
        }
    }
    
	/**
	 * 获取文章情感正负面
	 *
	 * @date Created at 2018年8月16日 下午2:58:15
	 * @Author 谷泽昊
	 * @param sid
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@ApiOperation("获取文章情感正负面")
	@GetMapping("/getEmotion")
	public Object getEmotion(@ApiParam("md5") @RequestParam("md5") String md5tag,
			@ApiParam("时间，格式为：时间戳") @RequestParam("urlTime") long urlTime) throws TRSException {
		return sensitiveService.getEmotion(md5tag, new Date(urlTime));
	}

}
