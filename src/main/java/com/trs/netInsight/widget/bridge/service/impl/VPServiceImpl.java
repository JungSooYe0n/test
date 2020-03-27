package com.trs.netInsight.widget.bridge.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.handler.exception.AuthorityException;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.bridge.entity.VirtualProps;
import com.trs.netInsight.widget.bridge.entity.enums.PropsType;
import com.trs.netInsight.widget.bridge.entity.repostory.VPRepository;
import com.trs.netInsight.widget.bridge.service.IVPService;

/**
 * 虚拟道具服务类
 *
 * Created by ChangXiaoyang on 2017/9/14.
 */
@Service
public class VPServiceImpl implements IVPService {

	@Autowired
	private VPRepository vpRepository;

	/**
	 * 检验可用方案点数
	 *
	 */
	@Override
	public void checkProps(PropsType type) throws AuthorityException {
		String userId=UserUtils.getUser().getId();
		Optional<VirtualProps> optional = vpRepository.findByUserIdAndPropsType(userId, type);
		if (optional.isPresent()) {
			VirtualProps props = optional.get();
			int usable = props.getUsableProps();
			if (usable <= 0) {
				throw new AuthorityException("点数不足");
			}
			props.setUsableProps(usable - 1);
			props.setUsedProps(props.getUsedProps() + 1);
			vpRepository.save(props);
		}
	}

}
