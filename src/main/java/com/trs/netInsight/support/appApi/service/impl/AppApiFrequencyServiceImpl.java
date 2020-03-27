package com.trs.netInsight.support.appApi.service.impl;

import com.trs.netInsight.support.appApi.entity.AppApiClient;
import com.trs.netInsight.support.appApi.entity.AppApiFrequency;
import com.trs.netInsight.support.appApi.entity.repository.IAppApiFrequencyRepository;
import com.trs.netInsight.support.appApi.result.ApiCommonResult;
import com.trs.netInsight.support.appApi.result.ApiResultType;
import com.trs.netInsight.support.appApi.service.IApiFrequencyService;
import com.trs.netInsight.support.appApi.utils.constance.ApiMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppApiFrequencyServiceImpl implements IApiFrequencyService {

	@Autowired
	private IAppApiFrequencyRepository frequencyRepository;

	@Override
	public List<AppApiFrequency> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countAll() {
		return frequencyRepository.count();
	}

	@Override
	public void save(List<AppApiFrequency> list) {
		this.frequencyRepository.save(list);
	}

	@Override
	public AppApiFrequency save(AppApiFrequency frequency) {
		return frequencyRepository.save(frequency);
	}

	@Override
	public AppApiFrequency findByCodeWithClient(int code, AppApiClient client) {
		AppApiFrequency frequency = null;

		List<AppApiFrequency> list = this.frequencyRepository.findByCode(code);
		if (list != null && list.size() > 0) {
			frequency = list.get(0);
			if (client != null) {
				for (AppApiFrequency apiFrequency : list) {
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
			AppApiFrequency frequency = frequencyRepository.findByCodeAndClientId(code, clientId);
			if (frequency != null){
				frequency.setFrequencyCustom(frequencyCustom);
				frequency = frequencyRepository.save(frequency);
			}else {
				frequency = new AppApiFrequency();
				frequency.setCode(code);
				frequency.setClientId(clientId);
				frequency.setFrequencyCustom(frequencyCustom);
				frequency = frequencyRepository.save(frequency);
			}
			result = new ApiCommonResult(ApiResultType.Success, null);
			result.setData(frequency);
		}else {
			if (setAll){ // 设置全部
				List<AppApiFrequency> frequencies = frequencyRepository.findByClientId(clientId);
				if (frequencies != null && frequencies.size() > 0){ // 已存在频率数据
					if ( frequencies.size() == ApiMethod.values().length) { // 存在完整频率数据
						for (AppApiFrequency entity : frequencies) {
							entity.setFrequencyCustom(frequencyCustom);
						}
					}else { // 存在部分频率数据
						List<AppApiFrequency> frequenciesBak = new ArrayList<>();
						AppApiFrequency frequency = null;
						for (ApiMethod method : ApiMethod.values()) {
							for (AppApiFrequency entity: frequencies) {
								if (entity.getCode() == method.getCode()){
									entity.setFrequencyCustom(frequencyCustom);
									continue;
								}
								frequency = new AppApiFrequency();
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
					AppApiFrequency frequency = null;
					for (ApiMethod method : ApiMethod.values()) {
						frequency = new AppApiFrequency();
						frequency.setCode(method.getCode());
						frequency.setName(method.getName());
						frequency.setFrequencyCustom(frequencyCustom);
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
