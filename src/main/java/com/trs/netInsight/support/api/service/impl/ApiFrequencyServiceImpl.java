package com.trs.netInsight.support.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.trs.netInsight.support.api.result.ApiCommonResult;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.api.utils.constance.ApiMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.support.api.entity.ApiClient;
import com.trs.netInsight.support.api.entity.ApiFrequency;
import com.trs.netInsight.support.api.entity.repository.IApiFrequencyRepository;
import com.trs.netInsight.support.api.service.IApiFrequencyService;

@Service
public class ApiFrequencyServiceImpl implements IApiFrequencyService {

	@Autowired
	private IApiFrequencyRepository frequencyRepository;

	@Override
	public List<ApiFrequency> findAll() {
		// TODO Auto-generated method stub
		return frequencyRepository.findAll();
	}

	@Override
	public ApiFrequency findByCodeWithInit(int code) {
		return frequencyRepository.findByCodeAndClientIdIsNullAndFrequencyCustomIsNull(code);
	}

	@Override
	public long countAll() {
		return frequencyRepository.count();
	}

	@Override
	public void save(List<ApiFrequency> list) {
		this.frequencyRepository.save(list);
	}

	@Override
	public ApiFrequency save(ApiFrequency frequency) {
		return frequencyRepository.save(frequency);
	}

	@Override
	public ApiFrequency findByCodeWithClient(int code, ApiClient client) {
		ApiFrequency frequency = null;

		List<ApiFrequency> list = this.frequencyRepository.findByCode(code);
		if (list != null && list.size() > 0) {
			frequency = list.get(0);
			if (client != null) {
				for (ApiFrequency apiFrequency : list) {
					if (client.getId().equals(apiFrequency.getClientId())) {
						frequency = apiFrequency;
						frequency.chooseLevel(client.getFrequencyLevel());
						break;
					}
				}
			}
		}
		return frequency;
	}

	@Override
	public ApiCommonResult setFrequencyCustom(String clientId, int code, String frequencyCustom, boolean setAll) {
		ApiCommonResult result = null;
		if (code != 0){ // 优先code
			ApiFrequency frequency = frequencyRepository.findByCodeAndClientId(code, clientId);
			if (frequency != null){
				frequency.setFrequencyCustom(frequencyCustom);
				frequency = frequencyRepository.save(frequency);
			}else {
				frequency = new ApiFrequency();
				frequency.setCode(code);
				frequency.setClientId(clientId);
				frequency.setFrequencyCustom(frequencyCustom);
				frequency = frequencyRepository.save(frequency);
			}
			result = new ApiCommonResult(ApiResultType.Success, null);
			result.setData(frequency);
		}else {
			if (setAll){ // 设置全部
				List<ApiFrequency> frequencies = frequencyRepository.findByClientId(clientId);
				if (frequencies != null && frequencies.size() > 0){ // 已存在频率数据
					if ( frequencies.size() == ApiMethod.values().length) { // 存在完整频率数据
						for (ApiFrequency entity : frequencies) {
							entity.setFrequencyCustom(frequencyCustom);
						}
					}else { // 存在部分频率数据
						List<ApiFrequency> frequenciesBak = new ArrayList<>();
						ApiFrequency frequency = null;
						for (ApiMethod method : ApiMethod.values()) {
							for (ApiFrequency entity: frequencies) {
								if (entity.getCode() == method.getCode()){
									entity.setFrequencyCustom(frequencyCustom);
									continue;
								}
								frequency = new ApiFrequency();
								frequency.setCode(method.getCode());
								frequency.setName(method.getName());
								frequency.setClientId(clientId);
								frequency.setFrequencyCustom(frequencyCustom);
								frequenciesBak.add(frequency);
							}
						}
						frequencies.addAll(frequenciesBak);
					}
					frequencyRepository.save(frequencies);
					result = new ApiCommonResult(ApiResultType.Success, "频率更新成功");
				}else {
					frequencies =  new ArrayList<>();
					ApiFrequency frequency = null;
					for (ApiMethod method : ApiMethod.values()) {
						frequency = new ApiFrequency();
						frequency.setCode(method.getCode());
						frequency.setName(method.getName());
						frequency.setFrequencyCustom(frequencyCustom);
						frequency.setClientId(clientId);
						frequencies.add(frequency);
					}
				}
				frequencyRepository.save(frequencies);
				result = new ApiCommonResult(ApiResultType.Success, "频率更新成功");
			}else {
				result = new ApiCommonResult(ApiResultType.NotFindSource, null);
			}
		}
		return result;
	}
}
