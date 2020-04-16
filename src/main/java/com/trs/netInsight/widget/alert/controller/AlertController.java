package com.trs.netInsight.widget.alert.controller;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.SourceUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 预警过信息查询
 *
 * Created by xiaoying on 2017/12/14.
 */
@RestController
@RequestMapping("/alert")
@Api(description = "预警信息 针对alert表")
@Slf4j
public class AlertController {
	
	
	@Value("${http.client}")
	private boolean httpClient;
	
	@Autowired
	private IAlertService alertService;

	private List< String> timeList = Arrays.asList("0d","7d","30d");

	@ApiOperation("已发送和站内的列表法展示接口")
	@GetMapping("/list")
	@FormatResult
	public Object list(@ApiParam("从0开始 第几页") @RequestParam("pageNo") int pageNo,
			@ApiParam("第几条") @RequestParam("pageSize") int pageSize,
			@ApiParam("站内SMS 还是已发送SEND") @RequestParam("way") String way,
			@ApiParam("来源") @RequestParam(value = "source", defaultValue = "国内新闻") String source,
			@ApiParam("时间") @RequestParam(value = "time", defaultValue = "0d") String time,
			@ApiParam("接收者 ") @RequestParam(value = "receivers", defaultValue = "ALL") String receivers,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("结果中搜索 ") @RequestParam(value = "keywords", required = false) String keywords,
			@ApiParam("结果中搜索的范围")@RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope) throws OperationException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		source = SourceUtil.isSource(source);
		if(StringUtil.isEmpty(source)){
			return null;
		}
		if(httpClient){
			//跨工程
			return alertService.alertListHttp(pageNo, pageSize, way, source, time, receivers, invitationCard, forwarPrimary, keywords,fuzzyValueScope);
		}else{
			//本地,返回的alertEntity实体中keywords为string类型。。海贝库查出来的是List<String>
			return alertService.alertListLocal(pageNo, pageSize, way, source, time, receivers, invitationCard, forwarPrimary, keywords,fuzzyValueScope);
		}
	}

	/**
	 * 站内预警弹窗
	 *
	 * @param time
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("站内预警弹窗")
	@FormatResult
	@RequestMapping(value = "/listSMS", method = RequestMethod.GET)
	public Object listSMS(@ApiParam("时间间隔") @RequestParam(value = "time", defaultValue = "10n", required = false) String time,
						  @ApiParam("是否是刚刚登陆进来") @RequestParam(value = "justLogin", defaultValue = "false", required = false) Boolean justLogin,
						  @ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
						  @ApiParam("步长") @RequestParam(value = "pageSize", defaultValue = "3", required = false) int pageSize)
			throws OperationException {
		return null;
	    /*Boolean isAlert = UserUtils.getUser().getIsAlert();
		if(isAlert == null || isAlert){
			*//*
			20200107修改
			刚登陆进来，返回列表前三条
			刷新页面时，首先查询十分钟内的数据，如果有则返回，没有则返回列表前三条.
			页面定时请求  首先查询十分钟内的数据，如果有，则返回，没有则返回列表前三条
		 *//*
			pageSize = pageSize >= 1 ? pageSize : 3;
			int i = 0;
			if (justLogin) {
				time = "0d";
				i = 1;
			}
			String source = SourceUtil.isSource("ALL");
			if(StringUtil.isEmpty(source)){
				return null;
			}
			if (httpClient) {
				//跨工程
				Object obj = alertService.alertListHttp(pageNo, pageSize, "SMS", source, time, "ALL", "", "", "", "");
				for (; i < timeList.size(); i++) {
					time = timeList.get(i);
					if (ObjectUtil.isEmpty(obj)) {
						obj = alertService.alertListHttp(pageNo, pageSize, "SMS", source, time, "ALL", "", "", "", "");
					} else {
						break;
					}
				}
				return obj;
			} else {
				//本地,返回的alertEntity实体中keywords为string类型。。海贝库查出来的是List<String>
				Object obj = alertService.alertListLocal(pageNo, pageSize, "SMS", source, time, "ALL", "", "", "", "");
				for (; i < timeList.size(); i++) {
					time = timeList.get(i);
					if (ObjectUtil.isEmpty(obj)) {
						obj = alertService.alertListLocal(pageNo, pageSize, "SMS", source, time, "ALL", "", "", "", "");
					} else {
						break;
					}
				}
				return obj;
			}
		} else {
			//预警弹框不在提醒  ---  如果设置了不再提醒，当前次登录中不再提醒，再次登录状态重置，继续提醒
			return null;
		}*/
	}

	/**
	 * 站内预警弹窗是否继续提示
	 *
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("站内预警弹窗是否继续提示")
	@FormatResult
	@RequestMapping(value = "/isAlertSMS", method = RequestMethod.GET)
	public Object isAlertSMS(/*@ApiParam("不在提示") @RequestParam(value = "isAlert", defaultValue = "true", required = false) Boolean justLogin */)
			throws OperationException {
		User user = UserUtils.updateIsAlert(false);
		return user;
	}

	@ApiOperation("预警删除接口")
	@PostMapping("/delete")
	@FormatResult
	public Object delete(@RequestParam("id") String id,@RequestParam("deTime") String createdTime) throws OperationException {
		if(httpClient){
			return alertService.deleteHttp(id,createdTime);
		}else{
			return alertService.deleteLocal(id);
		}
	}

}
