package com.trs.netInsight.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cn.jiguang.common.ServiceHelper;
import cn.jiguang.common.connection.NativeHttpClient;
import cn.jpush.api.push.CIDResult;
import cn.jpush.api.push.model.*;
import cn.jpush.api.push.model.notification.*;
import com.google.gson.*;
//import com.trs.netInsight.handler.result.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import org.springframework.core.env.Environment;

/**
 * 极光推送工具类
 */
public class JPushClientUtil {
	protected static final Logger LOG = LoggerFactory.getLogger(JPushClientUtil.class);
	
	 // demo App defined in resources/jpush-api.conf 
   // protected static final String APP_KEY ="476ba5b8251e428071f53d72";
    //protected static final String MASTER_SECRET = "d5ecedb449a0cf8421e7e69f";
	
	public static final String TITLE = "网察预警提醒";
    public static final String ALERT = "你收到一个任务提醒!";
    public static final String MSG_CONTENT = "Test from API Example - msgContent";
    public static final String REGISTRATION_ID = "0900e8d85ef";
    public static final String TAG = "tag_api";
    public static long sendCount = 0;

	/**
	 * 发送app任务提醒
	 */
	public static com.trs.netInsight.handler.result.Message SendPush(Map<String,String> map) {
		ClientConfig clientConfig = ClientConfig.getInstance();
        Environment env = SpringUtil.getBean(Environment.class);
        String appKey = env.getProperty("trs.app.AppID");
        String appSecret = env.getProperty("trs.app.AppSecret");
        final JPushClient jpushClient = new JPushClient(appSecret, appKey, null, clientConfig);

        String authCode = ServiceHelper.getBasicAuthorization(appKey, appSecret);
        NativeHttpClient httpClient = new NativeHttpClient(authCode, null, clientConfig);
//        ApacheHttpClient httpClient = new ApacheHttpClient(authCode, null, clientConfig);
//        jpushClient.getPushClient().setHttpClient(httpClient);
        final PushPayload payload = buildPushObject_android_tag_alertWithTitle(map); //可能找不到该设备的用户报错
//        final PushPayload payload = buildPushObject_all_all_alert();
//        PushPayload payload = buildPushObject_all_alias_alert();
        try {
            PushResult result = jpushClient.sendPush(payload);
            LOG.info("Got result - " + result);
            System.out.println(result);

            return com.trs.netInsight.handler.result.Message.getMessage(CodeUtils.SUCCESS, "发送成功！Got result -  ", result);
            // 如果使用 NettyHttpClient，需要手动调用 close 方法退出进程
            // If uses NettyHttpClient, call close when finished sending request, otherwise process will not exit.
            // jpushClient.close();
        } catch (APIConnectionException e) {
            LOG.error("Connection error. Should retry later. ", e);
            LOG.error("Sendno: " + payload.getSendno());
           return com.trs.netInsight.handler.result.Message.getMessage(CodeUtils.FAIL, "Connection error. Should retry later,e:", e);


        } catch (APIRequestException e) {
            LOG.error("Error response from JPush server. Should review and fix it. ", e);
            LOG.info("HTTP Status: " + e.getStatus());
            LOG.info("Error Code: " + e.getErrorCode());
            LOG.info("Error Message: " + e.getErrorMessage());
            LOG.info("Msg ID: " + e.getMsgId());
            LOG.error("Sendno: " + payload.getSendno());
            return com.trs.netInsight.handler.result.Message.getMessage(CodeUtils.FAIL, "Error response from JPush server. Should review and fix it, e:", e);

        }
    }

    /**
     * 测试多线程发送 2000 条推送耗时
     */
    public static void testSendPushes() {
//        ClientConfig clientConfig = ClientConfig.getInstance();
//        final JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);
//        String authCode = ServiceHelper.getBasicAuthorization(APP_KEY, MASTER_SECRET);
//        // Here you can use NativeHttpClient or NettyHttpClient or ApacheHttpClient.
//        NativeHttpClient httpClient = new NativeHttpClient(authCode, null, clientConfig);
//        // Call setHttpClient to set httpClient,
//        // If you don't invoke this method, default httpClient will use NativeHttpClient.
////        ApacheHttpClient httpClient = new ApacheHttpClient(authCode, null, clientConfig);
//        jpushClient.getPushClient().setHttpClient(httpClient);
//        final PushPayload payload = buildPushObject_ios_tagAnd_alertWithExtrasAndMessage();
//        for(int i=0;i<10;i++) {
//            Thread thread = new Thread() {
//                public void run() {
//                    for (int j = 0; j < 200; j++) {
//                        long start = System.currentTimeMillis();
//                        try {
//                            PushResult result = jpushClient.sendPush(payload);
//                            LOG.info("Got result - " + result);
//
//                        } catch (APIConnectionException e) {
//                            LOG.error("Connection error. Should retry later. ", e);
//                            LOG.error("Sendno: " + payload.getSendno());
//
//                        } catch (APIRequestException e) {
//                            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//                            LOG.info("HTTP Status: " + e.getStatus());
//                            LOG.info("Error Code: " + e.getErrorCode());
//                            LOG.info("Error Message: " + e.getErrorMessage());
//                            LOG.info("Msg ID: " + e.getMsgId());
//                            LOG.error("Sendno: " + payload.getSendno());
//                        }
//
//                        System.out.println("耗时" + (System.currentTimeMillis() - start) + "毫秒 sendCount:" + (++sendCount));
//                    }
//                }
//            };
//            thread.start();
//        }
    }
	
	public static PushPayload buildPushObject_all_all_alert() {
	    return PushPayload.alertAll(ALERT);
	}
	
    public static PushPayload buildPushObject_all_alias_alert() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias("alias1"))
                .setNotification(Notification.alert(ALERT))
                .build();
    }
    /*
     * 目标是 tag 为 "tag1" 的设备，内容是 Android 通知 ALERT，并且标题为 TITLE。
     * type 任务类型	map.put("type", "1");
     * info 任务详细内容	map.put("info", "内容在这里");
     */
    public static PushPayload buildPushObject_android_tag_alertWithTitle(Map<String,String> map) {
    	String ids = map.get("receviceUserid");
    	String userids[] = quchonguserids(ids);
    	System.out.println("receviceUserid===="+Arrays.toString(userids));
    	String alert = map.get("title");

        return PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.alias(userids))
         //       .setNotification(Notification.android(alert, TITLE, map))
                .setNotification(Notification.newBuilder()
                .addPlatformNotification(AndroidNotification.newBuilder()
                .setBuilderId(1)
                .addExtras(map)
                .setAlert(alert)
                .setTitle(TITLE).build()).build())
                .build();
    }
    public static String[] quchonguserids(String ids){
    	String arr[] = ids.split(",");
    	for (int i = 0; i < arr.length; i++) {
            for(int j = i+1;j < arr.length;j++){
                //根据重复元素的下标来删除元素
                if(arr[i].trim().equals(arr[j].trim())){
                    //定义一个删除元素后的缓存数组
                	String[] nArr = new String[arr.length-1];
                    for(int k =0;k < arr.length;k++)
                    if(k < j){
                    	if(!arr[k].trim().equals(""))
                    		nArr[k] = arr[k].trim();
                    }else if(k > j){
                    	if(!arr[k].trim().equals(""))
                    		nArr[k-1] = arr[k].trim();
                    }
                    //这里有一个坑，缓存数组赋值给原数组后，因下标+1可能会导致排重遗漏
                    arr = nArr;
                    j--;
                }
            }
        }
    	return arr;
    }
    public static void buildPushObject_with_extra() {

        JsonObject jsonExtra = new JsonObject();
        jsonExtra.addProperty("extra1", 1);
        jsonExtra.addProperty("extra2", false);

        Map<String, String> extras = new HashMap<String, String>();
        extras.put("extra_1", "val1");
        extras.put("extra_2", "val2");

        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.tag("tag1"))
                .setNotification(Notification.newBuilder()
                        .setAlert("alert content")
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setTitle("Android Title")
                                .addExtras(extras)
                                .addExtra("booleanExtra", false)
                                .addExtra("numberExtra", 1)
                                .addExtra("jsonExtra", jsonExtra)
                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .incrBadge(1)
                                .addExtra("extra_key", "extra_value").build())
                        .build())
                .build();

        System.out.println(payload.toJSON());
    }
    
    public static PushPayload buildPushObject_ios_tagAnd_alertWithExtrasAndMessage() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.tag_and("tag1", "tag_all"))
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(ALERT)
                                .setBadge(5)
                                .setSound("happy")
                                .addExtra("from", "JPush")
                                .build())
                        .build())
                 .setMessage(Message.content(MSG_CONTENT))
                 .setOptions(Options.newBuilder()
                         .setApnsProduction(true)
                         .build())
                 .build();
    }

    public static PushPayload buildPushObject_android_newly_support() {
        JsonObject inbox = new JsonObject();
        inbox.add("line1", new JsonPrimitive("line1 string"));
        inbox.add("line2", new JsonPrimitive("line2 string"));
        inbox.add("contentTitle", new JsonPrimitive("title string"));
        inbox.add("summaryText", new JsonPrimitive("+3 more"));
        Notification notification = Notification.newBuilder()
                .addPlatformNotification(AndroidNotification.newBuilder()
                        .setAlert(ALERT)
                        .setBigPicPath("path to big picture")
                        .setBigText("long text")
                        .setBuilderId(1)
                        .setCategory("CATEGORY_SOCIAL")
                        .setInbox(inbox)
                        .setStyle(1)
                        .setTitle("Alert test")
                        .setPriority(1)
                        .build())
                .build();
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.all())
                .setNotification(notification)
                .setOptions(Options.newBuilder()
                        .setApnsProduction(true)
                        .setSendno(ServiceHelper.generateSendno())
                        .build())
                .build();
    }
    
    public static PushPayload buildPushObject_ios_audienceMore_messageWithExtras() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.newBuilder()
                        .addAudienceTarget(AudienceTarget.tag("tag1", "tag2"))
                        .addAudienceTarget(AudienceTarget.alias("alias1", "alias2"))
                        .build())
                .setMessage(Message.newBuilder()
                        .setMsgContent(MSG_CONTENT)
                        .addExtra("from", "JPush")
                        .build())
                .build();
    }

    public static PushPayload buildPushObject_all_tag_not() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.tag_not("abc", "123"))
                .setNotification(Notification.alert(ALERT))
                .build();
    }

    public static PushPayload buildPushObject_android_cid() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.registrationId("18071adc030dcba91c0"))
                .setNotification(Notification.alert(ALERT))
                .setCid("cid")
                .build();
    }

    public static void testSendPushWithCustomConfig() {
//        ClientConfig com.trs.netInsight.config = ClientConfig.getInstance();
//        // Setup the custom hostname
//        com.trs.netInsight.config.setPushHostName("https://api.jpush.cn");
//
//        JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, com.trs.netInsight.config);
//
//        // For push, all you need do is to build PushPayload object.
//        PushPayload payload = buildPushObject_all_all_alert();
//
//        try {
//            PushResult result = jpushClient.sendPush(payload);
//            LOG.info("Got result - " + result);
//
//        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
//
//        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
//            LOG.info("Msg ID: " + e.getMsgId());
//        }
    }

    public static void testSendIosAlert() {
//        JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY);
//
//        IosAlert alert = IosAlert.newBuilder()
//                .setTitleAndBody("test alert", "subtitle", "test ios alert json")
//                .setActionLocKey("PLAY")
//                .build();
//        try {
//            PushResult result = jpushClient.sendIosNotificationWithAlias(alert, new HashMap<String, String>(), "alias1");
//            LOG.info("Got result - " + result);
//        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
//        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
//        }
    }

    public static void testSendWithSMS() {
//        JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY);
//        try {
//            SMS sms = SMS.content("Test SMS", 10);
//            PushResult result = jpushClient.sendAndroidMessageWithAlias("Test SMS", "test sms", sms, "alias1");
//            LOG.info("Got result - " + result);
//        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
//        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
//        }
    }

    public static void testGetCidList() {
//        JPushClient jPushClient = new JPushClient(MASTER_SECRET, APP_KEY);
//        try {
//            CIDResult result = jPushClient.getCidList(3, null);
//            LOG.info("Got result - " + result);
//        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
//        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
//        }
    }

    public static void testSendPushWithCid() {
//        JPushClient jPushClient = new JPushClient(MASTER_SECRET, APP_KEY);
//        PushPayload pushPayload = buildPushObject_android_cid();
//        try {
//            PushResult result = jPushClient.sendPush(pushPayload);
//            LOG.info("Got result - " + result);
//        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
//        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
//        }
    }

}
