package com.trs.netInsight.util;

import com.trs.netInsight.support.api.entity.ApiClient;
import com.trs.netInsight.support.api.entity.repository.IClientRepository;
import com.trs.netInsight.widget.user.entity.Organization;
import org.apache.commons.codec.digest.DigestUtils;

public class ClientUtil {

    /**
     * 机构申请client
     *
     * @param organization
     * @return
     * @Return : Client
     * @since changjiang @ 2018年7月2日
     */
    public static ApiClient applyClientByOrg(Organization organization, String level, IClientRepository clientRepository) {
        ApiClient client = null;
        if (organization != null) {
            client = new ApiClient();
            client.setClientName(organization.getOrganizationName());
            client.setGrantOrgId(organization.getId());
            String secretKey = DigestUtils.md5Hex(organization.getId());
            secretKey = "NICLIENT" + secretKey;
            client.setClientSecretKey(secretKey);
            client.setFrequencyLevel(level);
            client = clientRepository.save(client);
        }
        return client;
    }
}
