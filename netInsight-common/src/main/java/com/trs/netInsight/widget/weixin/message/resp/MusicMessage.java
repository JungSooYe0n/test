package com.trs.netInsight.widget.weixin.message.resp;

/**
 * 音乐消息
 * 
 */
public class MusicMessage extends BaseMessage {
	// 音乐
	private com.trs.netInsight.widget.weixin.message.resp.Music Music;

	public com.trs.netInsight.widget.weixin.message.resp.Music getMusic() {
		return Music;
	}

	public void setMusic(com.trs.netInsight.widget.weixin.message.resp.Music music) {
		Music = music;
	}
}