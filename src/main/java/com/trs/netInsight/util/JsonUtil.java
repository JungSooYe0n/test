package com.trs.netInsight.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertSend;
import com.trs.netInsight.widget.alert.entity.AlertSendWeChat;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * fastjson
 * @author xiaoying
 *
 */
@Slf4j
public class JsonUtil {

	//存储路径从配置文件中取


	public static void main(String[] args) {

	}

	/**
	 * 为了让微信和预警实体都能用 所以中间有转换
	 * @param alertSaveUrl 预警或者微信的存储路径
	 * @param userId 微信为空
	 * @param n 暂定为今天再加上前6天共7天  入参为6
	 * @param T 预警或者微信实体
	 * @return 返回这几天总的list
	 */
	public static Object getAllList(String alertSaveUrl, String userId,int n,Object T,String fileType,String alertSource){
		List<Object> allList = new ArrayList<>();
		List<String> dateList =DateUtil.getDataStinglist2(n, "yyyy-MM-dd");
		log.debug("dateList"+dateList);
		String url = "";
		//把每天的文件都取出来最后放在一个大List中
		for(String date:dateList){
//			if(StringUtil.isNotEmpty(userId) && StringUtil.isEmpty(fileType)){
//				url = alertSaveUrl+userId+"/"+date+".json";
//			}if (StringUtil.isNotEmpty(userId) && StringUtil.isNotEmpty(fileType)){
//				url = alertSaveUrl+userId+"/"+date + fileType;
//			}else{
//				url = alertSaveUrl+date + fileType;
//			}
			if (StringUtil.isNotEmpty(userId)){
				if (StringUtil.isNotEmpty(fileType)){
					url = alertSaveUrl+userId+"/"+date + fileType;
				}else {
					url = alertSaveUrl+userId+"/"+date+".json";
				}
			}else {
				url = alertSaveUrl + date + fileType;
			}
			List<Object> listByJson = (List<Object>) getListByJson(url,T,alertSource);
			if(listByJson!=null && listByJson.size()>0){

				allList.addAll(listByJson);
			}
		}
		return allList;
	}


	public static Object getListByJson(String alertSaveUrl,Object T,String alertSource) {
		long begin = System.currentTimeMillis();
		String jsonStr = "";
		try {
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//			String format = df.format(new Date());// new Date()为获取当前系统时间
//			File file  = new File(alertSaveUrl+userId+"/"+format+".json");
			File file  = new File(alertSaveUrl);
//			File file  = new File("F:/UTIL/"+userId+"/"+format+".json");
			//有可能这个用户当天就没收到预警  所以连他的文件夹都没有
			if(!file.exists()){
				return null;
			}
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			//由于读取乱码  换BufferedReader
			BufferedReader reader = new BufferedReader (new InputStreamReader(bis));
			//之所以用BufferedReader,而不是直接用BufferedInputStream读取,是因为BufferedInputStream是InputStream的间接子类,
			//InputStream的read方法读取的是一个byte,而一个中文占两个byte,所以可能会出现读到半个汉字的情况,就是乱码.
			//BufferedReader继承自Reader,该类的read方法读取的是char,所以无论如何不会出现读个半个汉字的.
	        StringBuffer sb = new StringBuffer();
	        while (reader.ready()) {
	        	sb.append((char)reader.read());
	        }
	        reader.close();
			/*
			byte[] buffer = new byte[2048];
			int cnt = 0;
			StringBuffer sb = new StringBuffer();
			while((cnt=bis.read(buffer)) != -1) {
				String bluck = new String(buffer, 0, cnt);
				sb.append(bluck);
			}*/
			bis.close();

			jsonStr = "["+sb.toString()+"]";
			if(T instanceof AlertEntity){
				List<AlertEntity> list = JSONArray.parseObject(jsonStr, new TypeReference<ArrayList<AlertEntity>>() {});
				System.out.println("=====BufferIStream===== time: " + (System.currentTimeMillis() - begin) + "ms");
				return list;
			}else if(T instanceof AlertSendWeChat){
				List<AlertSendWeChat> list = JSONArray.parseObject(jsonStr, new TypeReference<ArrayList<AlertSendWeChat>>() {});
				System.out.println("=====BufferIStream===== time: " + (System.currentTimeMillis() - begin) + "ms");
				return list;
			}else if (T instanceof AlertSend){
				List<AlertSend> list = JSONArray.parseObject(jsonStr, new TypeReference<ArrayList<AlertSend>>() {});
				ArrayList<AlertSend> alertSends = new ArrayList<>();
				if (ObjectUtil.isNotEmpty(list) && ("ARTIFICIAL".equals(alertSource) || "AUTO".equals(alertSource))){
					for (AlertSend alertSend : list) {
						if (alertSend.getAlertSource().equals(alertSource)){
							alertSends.add(alertSend);
						}
					}
					return alertSends;
				}
				System.out.println("=====BufferIStream===== time: " + (System.currentTimeMillis() - begin) + "ms");
				return list;
			}
			
//			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
/*	*//**
	 * 过后跟上边的方法整合成list<T>
	 * @param alertSaveUrl
	 * @return
	 *//*
	public static List<AlertSendWeChat> getListByJson(String alertSaveUrl,int n) {
		long begin = System.currentTimeMillis();
		String jsonStr = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
			String format = df.format(new Date());// new Date()为获取当前系统时间
			File file  = new File(alertSaveUrl+format+"-wechat.json");
//			File file  = new File("F:/UTIL/"+userId+"/"+format+".json");
			//有可能这个用户当天就没收到预警  所以连他的文件夹都没有
			if(!file.exists()){
				return null;
			}
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			//由于读取乱码  换BufferedReader
			BufferedReader reader = new BufferedReader (new InputStreamReader(bis));
			//之所以用BufferedReader,而不是直接用BufferedInputStream读取,是因为BufferedInputStream是InputStream的间接子类,
			//InputStream的read方法读取的是一个byte,而一个中文占两个byte,所以可能会出现读到半个汉字的情况,就是乱码.
			//BufferedReader继承自Reader,该类的read方法读取的是char,所以无论如何不会出现读个半个汉字的.
	        StringBuffer sb = new StringBuffer();
	        while (reader.ready()) {
	        	sb.append((char)reader.read());
	        }
	        reader.close();
			
			byte[] buffer = new byte[2048];
			int cnt = 0;
			StringBuffer sb = new StringBuffer();
			while((cnt=bis.read(buffer)) != -1) {
				String bluck = new String(buffer, 0, cnt);
				sb.append(bluck);
			}
			bis.close();

			jsonStr = "["+sb.toString()+"]";
			List<AlertSendWeChat> list = JSONArray.parseObject(jsonStr, new TypeReference<ArrayList<AlertSendWeChat>>() {});
			System.out.println("=====BufferIStream===== time: " + (System.currentTimeMillis() - begin) + "ms");
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}*/
	
	/**
	 * 清空文件内容   目前删除采取的办法是 查出后清空  删完了之后再追加存进去
	 * @param alertSaveUrl
	 */
	public static void clearInfoForFile(String alertSaveUrl) {
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//		String format = df.format(new Date());// new Date()为获取当前系统时间
//		String fileName = alertSaveUrl+userId+"/"+format+".json";
        File file =new File(alertSaveUrl);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	/*
	*//**
	 * list转json
	 * @param list
	 * @return
	 *//*
	public static String list2json(List<AlertEntity> list){
		return JSON.toJSONString(list);
	}*/

	/**
	 * json转实体 之前的笨方法 
	 * xiaoying
	 * @param parseObject
	 * @param format2
	 * @return
	 */
	public static AlertEntity object2alert(JSONObject parseObject,DateFormat format2){
		AlertEntity alert = new AlertEntity();
		alert.setAlertRuleBackupsId(parseObject.getString("alertRuleBackupsId"));
		alert.setId(parseObject.getString("id"));
		try {
			alert.setCreatedTime(format2.parse(parseObject.getString("createdTime")));
			alert.setTime(format2.parse(parseObject.getString("time")));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		alert.setUserId(parseObject.getString("userId"));
		alert.setSid(parseObject.getString("sid"));
		alert.setTitle(parseObject.getString("title"));
		alert.setContent(parseObject.getString("content"));
		alert.setUrlName(parseObject.getString("urlName"));
		alert.setSiteName(parseObject.getString("siteName"));
		alert.setGroupName(parseObject.getString("groupName"));
		alert.setAlertRuleBackupsId(parseObject.getString("alertRuleBackupsId"));
		String alertSource = parseObject.getString("alertSource");
		if(StringUtil.isNotEmpty(alertSource) && !"null".equals(alertSource)){
			alert.setAlertSource(AlertSource.valueOf(alertSource));
		}
		alert.setCommtCount(Long.valueOf(parseObject.getString("commtCount")));
		alert.setRttCount(Long.valueOf(parseObject.getString("rttCount")));
		alert.setScreenName(parseObject.getString("screenName"));
		alert.setAppraise(parseObject.getString("appraise"));
		alert.setReceiver(parseObject.getString("receiver"));
		alert.setSendWay(SendWay.valueOf(parseObject.getString("sendWay")));
		alert.setNreserved1(parseObject.getString("nreserved1"));
		alert.setMd5tag(parseObject.getString("md5tag"));
		alert.setFlag(Boolean.valueOf(parseObject.getString("flag")));
		alert.setRetweetedMid(parseObject.getString("retweetedMid"));
		//剩下四个不是存数据库的了 send favourite sim trslk
		return alert;
	}

}
