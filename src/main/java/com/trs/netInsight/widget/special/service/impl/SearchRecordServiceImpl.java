package com.trs.netInsight.widget.special.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.trs.jpa.utils.Criteria;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.special.entity.SearchRecord;
import com.trs.netInsight.widget.special.entity.repository.SearchRecordRepository;
import com.trs.netInsight.widget.special.service.ISearchRecordService;

@Service
public class SearchRecordServiceImpl implements ISearchRecordService {

	@Autowired
	private SearchRecordRepository recordRepository;
	
	
	@Override
	public void createRecord(String keywords) {
		//如果搜索过  就更新时间  没搜索过 就存上
		/*List<SearchRecord> searchList = recordRepository.findByUserIdAndKeywords(UserUtils.getUser().getId(), keywords);
		if(searchList!=null && searchList.size()>0){
			searchList.get(0).setCreatedTime(new Date());
			recordRepository.save(searchList.get(0));
		}else{*/
		//存的时候都存  取的时候不重复
			recordRepository.save(new SearchRecord(keywords));
//		}
		
	}

	@Override
	public List<SearchRecord> findByUserId(String userId) {
	List<SearchRecord> recordList = recordRepository.findByUserId(userId,new Sort(Direction.DESC, "createdTime"));
	//sql只能先groupby再orderby  所以用java实现
	Map<String,String> map = new HashMap<>();
	List<SearchRecord> list = new ArrayList<>();
	for(SearchRecord record:recordList){
		if(!map.containsKey(record.getKeywords())){
			map.put(record.getKeywords(), record.getKeywords());
			list.add(record);
		}
	}
	return list;
	}

	@Override
	public List<SearchRecord> findByUserIdAndKeywords(String userId, String keywords) {
		return recordRepository.findByUserIdAndKeywords(userId, keywords);
	}

}
