package com.trs.netInsight.widget.alert.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.alert.entity.AlertBackups;
import com.trs.netInsight.widget.alert.entity.repository.AlertBackupsRepository;
import com.trs.netInsight.widget.alert.service.IAlertBackupsService;

@Service
public class AlertBackupsServiceImpl implements IAlertBackupsService {

	@Autowired
	private AlertBackupsRepository alertRepository;

	@Override
	public List<AlertBackups> selectAll(String userId, int pageSize) throws TRSException {
		List<AlertBackups> countList;
		if (pageSize <= 0) {
			countList = alertRepository.findByUserId(userId, new Sort(Sort.Direction.DESC, "lastModifiedTime"));
		} else {
			countList = alertRepository.findByUserId(userId,
					new PageRequest(0, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		}
		return countList;
	}

	@Override
	public void save(AlertBackups alertEntity) {
		alertRepository.save(alertEntity);
	}

	@Override
	public void delete(String alertId) {
		alertRepository.delete(alertId);
	}

	@Override
	public List<AlertBackups> findByUserId(String uid, Sort sort) {
		return alertRepository.findByUserId(uid, sort);
	}

	@Override
	public List<AlertBackups> findByOrganizationId(String organizationId, Sort sort) {
		return alertRepository.findByOrganizationId(organizationId, sort);
	}

	@Override
	public List<AlertBackups> selectByOrganizationId(String organizationId, Integer pageSize) {
		List<AlertBackups> countList;
		if (pageSize <= 0) {
			countList = alertRepository.findByOrganizationId(organizationId,
					new Sort(Sort.Direction.DESC, "lastModifiedTime"));
		} else {
			countList = alertRepository.findByOrganizationId(organizationId,
					new PageRequest(0, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		}
		return countList;
	}

	@Override
	public void save(Iterable<AlertBackups> entities) {
		alertRepository.save(entities);
	}

	@Override
	public Iterable<AlertBackups> findAll() {
		Iterable<AlertBackups> findAll = alertRepository.findAll();
		return findAll;
	}

	@Override
	public List<AlertBackups> findByRuleId(String ruleId) {
		return alertRepository.findByAlertRuleBackupsId(ruleId);
	}

	@Override
	public void delete(List<AlertBackups> findByRuleId) {
		alertRepository.delete(findByRuleId);
	}
}
