package com.trs.netInsight.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.utils.Base64Util;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;

@Slf4j
public class HttpUtil {
	
	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			/*for (String key : map.keySet()) {
				log.error(key + "--->" + map.get(key));
			}*/
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			log.error("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL 所代表远程资源的响应结果
	 */
	public static InputStream sendGetInputStream(String url, String param) {
		log.error("url=" + url);
		InputStream inputStream = null;
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			/*for (String key : map.keySet()) {
				log.error(key + "--->" + map.get(key));
			}*/
			// 定义 BufferedReader输入流来读取URL的响应
			inputStream = connection.getInputStream();
		} catch (Exception e) {
			log.error("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return inputStream;
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	@RequestMapping
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			log.error("打开和URL之间的连接");
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//			conn.setRequestProperty("Content-type", "application/json;charset=UTF-8");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
//			out = new PrintWriter(conn.getOutputStream());
			out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));
			// 发送请求参数
			log.error("发送请求参数");
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			log.error("定义BufferedReader输入流来读取URL的响应");
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			log.error("发送url成功地址："+url);
		} catch (Exception e) {
			log.error("发送url失败地址："+url);
			log.error("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		log.error("请求完成");
		return result;
	}
	//OA关联ETL通用接口
	public static String oaRelateEtl(String url, Map<String, String> contents) {
//		HttpClient client = new HttpClient();
		org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
		PostMethod postMethod = new PostMethod(url);
		for(String key:contents.keySet()) {
			if (contents.containsKey(key)) {
				postMethod.setParameter(key, contents.get(key));
			}
		}
		client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
		String json = "{}";
		try {
			if (client.executeMethod(postMethod) == 200) {
				json = postMethod.getResponseBodyAsString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}
		return json;
	}
	public static String doGet(String url, String charset) {
		log.error("进入 doget方法 :" + url);
		if (null == charset) {
			charset = "utf-8";
		}
		HttpClient httpClient = null;
		HttpGet httpGet = null;
		String result = null;

		try {
			httpClient = new SSLClient();
			httpGet = new HttpGet(url);

			HttpResponse response = httpClient.execute(httpGet);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("doget报错" + e);
		}

		return result;
	}

	public static InputStream doGetInputStream(String url, String charset) {
		log.error("进入 doget方法 :" + url);
		if (null == charset) {
			charset = "utf-8";
		}
		HttpClient httpClient = null;
		HttpGet httpGet = null;
		InputStream inputStream = null;

		try {
			httpClient = new SSLClient();
			httpGet = new HttpGet(url);

			HttpResponse response = httpClient.execute(httpGet);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				inputStream = resEntity.getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("doget报错" + e);
		}

		return inputStream;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String doPost(String url, Map<String, String> map, String charset) {
		//log.error("进入dopost方法");
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			//log.error("开始获取连接");
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			// 设置参数
			//log.error("设置参数");
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
				//log.error(elem.getKey() + "----" + elem.getValue());
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
				httpPost.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			//log.error("response:" + response);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				//log.error("resEntity:" + resEntity);
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			log.error("dopost报错" + ex);
			ex.printStackTrace();
		}
		log.error("得到结果" + result);
		return result;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String doDataPost(String url, Map<String, JSONObject> map, String charset) {
		log.error("进入dopost方法");
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			log.error("开始获取连接");
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			// 设置参数
			log.error("设置参数");
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
				//log.error(elem.getKey() + "----" + elem.getValue());
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
				httpPost.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			log.error("response:" + response);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				log.error("resEntity:" + resEntity);
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			log.error("dopost报错" + ex);
			ex.printStackTrace();
		}
		log.error("得到结果" + result);
		return result;
	}

	public static String sendPost(String url, JSONObject param) {
		String body = "";

		//创建httpclient对象
		CloseableHttpClient client = HttpClients.createDefault();
		//创建post方式请求对象
		HttpPost httpPost = new HttpPost(url);

		//装填参数
		StringEntity entity = new StringEntity(param.toString(), "utf-8");
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
		//设置参数到请求对象中
		httpPost.setEntity(entity);
		log.error("json格式参数post请求地址："+url);

		//设置header信息
		//指定报文头【Content-type】、【User-Agent】
		//httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
		httpPost.setHeader("Content-type","application/json");
		httpPost.setHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1");

		//执行请求操作，并拿到结果（同步阻塞）
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpPost);
		} catch (IOException e) {
			log.error("发送 json参数格式POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		HttpEntity responseEntity = response.getEntity();
		if (ObjectUtil.isNotEmpty(responseEntity)){
			//按utf-8编码转换结果实体为String类型
			try {
				body = EntityUtils.toString(responseEntity,"utf-8");
			} catch (IOException e) {
				log.error("处理json参数格式POST请求结果出现异常1！",e);
				e.printStackTrace();
			}
		}
		try {
			EntityUtils.consume(responseEntity);
		} catch (IOException e) {
			log.error("处理json参数格式POST请求结果出现异常2！",e);
			e.printStackTrace();
		} finally {
			//释放链接
			try {
				response.close();
			} catch (IOException e) {
				log.error("释放json参数格式POST请求链接出现异常！",e);
				e.printStackTrace();
			}
		}
		return body;
	}

	/**
	 * 专为单条微博分析 微博用户头像
	 * @return
	 */
	public static String  getImageBase64(String url) {

		String suffix = url.substring(url.lastIndexOf(".") + 1);
		try {
			URL urls = new URL(url);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Image image = Toolkit.getDefaultToolkit().getImage(urls);
			BufferedImage biOut = toBufferedImage(image);
			ImageIO.write(biOut, suffix, baos);
			String base64Str = Base64Util.encode(baos.toByteArray());
			return base64Str;
		} catch (Exception e) {
			return "";
		}
	}

	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			int transparency = Transparency.OPAQUE;
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}


	/**
	 * 将网络图片编码为base64
	 *
	 * @param url
	 * @return
	 */
	public static String encodeImageToBase64(String url)  {
		//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		String suffix = url.substring(url.lastIndexOf(".") + 1);
		//打开链接
		HttpURLConnection conn = null;
		try {
			URL urls = new URL(url);
			conn = (HttpURLConnection) urls.openConnection();
			//设置请求方式为"GET"
			conn.setRequestMethod("GET");
			//超时响应时间为5秒
			conn.setConnectTimeout(5 * 1000);
			//通过输入流获取图片数据
			InputStream inStream = conn.getInputStream();
			//得到图片的二进制数据，以二进制封装得到数据，具有通用性
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			//创建一个Buffer字符串
			byte[] buffer = new byte[1024];
			//每次读取的字符串长度，如果为-1，代表全部读取完毕
			int len = 0;
			//使用一个输入流从buffer里把数据读取出来
			while ((len = inStream.read(buffer)) != -1) {
			//用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
				outStream.write(buffer, 0, len);
			}
			//关闭输入流
			inStream.close();
			byte[] data = outStream.toByteArray();
			//对字节数组Base64编码
			BASE64Encoder encoder = new BASE64Encoder();
			String base64 = encoder.encode(data);
			return base64;//返回Base64编码过的字节数组字符串
		} catch (Exception e) {
			e.printStackTrace();
			log.error("图片上传失败,请联系客服!");
			//throw new Exception("图片上传失败,请联系客服!");
			return "";

		}
	}

}
