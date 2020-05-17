package com.trs.netInsight.widget.weixin.entity.qrcode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信二维码
 * 
 * @Type QRCode.java
 * @Desc
 * @author 谷泽昊
 * @date 2018年1月25日 下午2:42:19
 * @version
 */
@Getter
@Setter
@ToString
public class QRCode {
	private long expireSeconds;
	private String actionName;
	private int sceneId;

	public QRCode(int sceneId) {
		this.expireSeconds = 1800;
		this.actionName = "QR_SCENE";
		this.sceneId = sceneId;
	}

	public String toJSON() {
		return "{\"expire_seconds\": " + this.expireSeconds + ", \"action_name\": \"" + this.actionName
				+ "\", \"action_info\": {\"scene\": {\"scene_id\": " + this.sceneId + "}}}";
	}
}
