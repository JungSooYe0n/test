package com.trs.netInsight.widget.report.scheduled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.report.entity.MaterialLibrary;
import com.trs.netInsight.widget.report.service.IReportService;

import lombok.extern.slf4j.Slf4j;

/**
 * 报告模块定时调度类
 * Created by xiaoying on 2017年12月13日
 */
@Slf4j
@Component("reportschedule")
public class ReportSchedule {

	@Autowired
	private IReportService reportService;
	
	
	/**
	 * 每天定时更新素材库的天数，每天减1
	 * @throws Exception
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	public void updateRemainDays()throws Exception{
		log.info("定时更新素材库剩余天数开始！");
		List<MaterialLibrary> allLibList;
		allLibList=reportService.getAllLibrary();
		if(ObjectUtil.isNotEmpty(allLibList)){
			Map<String,List<MaterialLibrary>> map=calculateDays(allLibList);
			List<MaterialLibrary> updatelibList=map.get("newlist");
			List<MaterialLibrary> deletelibList=map.get("dellist");
			reportService.saveBatchList(updatelibList);
			//接下来删除剩余天数为0的素材库
			reportService.delBatchList(deletelibList);
		}else{
			log.info("没有素材库，不需要执行！");
		}
	}
	
	/**
	 * 计算剩余天数,封装成更新的和删除的list
	 * @param libraryList
	 * @return
	 */
	private Map<String,List<MaterialLibrary>> calculateDays(List<MaterialLibrary> libraryList){
		Map<String,List<MaterialLibrary>> map=new HashMap<String,List<MaterialLibrary>>();
		List<MaterialLibrary> newlibList=new ArrayList<MaterialLibrary>();
		List<MaterialLibrary> expirelibList=new ArrayList<MaterialLibrary>();
		libraryList.forEach(item->{
			int remainDay=item.getRemainDays()-1;
			if(remainDay==0){
				expirelibList.add(item);
			}else{
				item.setRemainDays(remainDay);
				newlibList.add(item);
			}
		});
		map.put("newlist", newlibList);
		map.put("dellist", expirelibList);
		return map;
	}
	
}
