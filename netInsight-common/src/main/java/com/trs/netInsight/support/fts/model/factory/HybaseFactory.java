package com.trs.netInsight.support.fts.model.factory;

import com.trs.hybase.client.TRSConnection;
import com.trs.hybase.client.params.ConnectParams;
import com.trs.netInsight.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Hybase连接工厂
 *
 * Created by trs on 2017/8/11.
 */
@Component
@Slf4j
public class HybaseFactory {

	private static ThreadLocal<TRSConnection> hybaseClient = new ThreadLocal<>();

	/**
	 * 连接地址
	 */
	@Getter
	private static String host;

	/**
	 * 连接端口
	 */
	@Getter
	private static String port;

	/**
	 * hybase节点
	 */
	@Getter
	@Setter
	private static String link;

	/**
	 * 查询耗时
	 */
	@Getter
	@Setter
	private static int queryTime;
	/**
	 * 用户名
	 */
	@Getter
	private static String userName;

	/**
	 * 用户密码
	 */
	@Getter
	private static String password;

	/**
	 * 服务路径
	 */
	@Getter
	private static String server;

	@Autowired
	private Environment env;

	/**
	 * 加载配置项
	 * @date Created at 2018年7月16日  下午2:31:45
	 * @Author 谷泽昊
	 */
	@PostConstruct
	public void setProperties() {
		HybaseFactory.host = env.getProperty("hybase.database.url");
		HybaseFactory.port = env.getProperty("hybase.database.port");
		HybaseFactory.userName = env.getProperty("hybase.database.user");
		HybaseFactory.password = env.getProperty("hybase.database.password");
		HybaseFactory.server = env.getProperty("hybase.database.server");
	}

	/**
	 * 线程clean
	 * @date Created at 2018年7月16日  下午2:31:30
	 * @Author 谷泽昊
	 */
	public static void clean() {
		try {
			hybaseClient.get().close();
		} catch (Exception ignore) {

		}
		hybaseClient.remove();
	}

	/**
	 * 线程赋值
	 * @date Created at 2018年7月16日  下午2:31:16
	 * @Author 谷泽昊
	 * @param connection
	 */
	public static void setClient(TRSConnection connection) {
		hybaseClient.set(connection);
	}

	/**
	 * 获取hybase节点连接
	 * @date Created at 2018年7月16日  下午2:30:56
	 * @Author 谷泽昊
	 * @return
	 */
	public static TRSConnection getClient() {
		TRSConnection connection = hybaseClient.get();
		if (connection == null) {
			// 随机选择ip
			String[] split = server.split(";");
			int length = split.length;
			int nextInt = 0;
			if (length - 1 > 0) {
				Random rand = new Random();
				nextInt = rand.nextInt(split.length - 1);
			}
			List<String> asList = Arrays.asList(split);
			String link = asList.get(nextInt);
			//log.info("hybase 链接IP ************************ "+link);
			//System.out.println("hybase 链接IP ---------- "+link);
			String serverList = assemblyServerList(host, port, link);
			connection = new TRSConnection(serverList, userName, password, new ConnectParams());
			setClient(connection);
		}
		return connection;
	}

	/**
	 * 装配服务器列表
	 *
	 * @param host
	 *            地址
	 * @param port
	 *            端口
	 * @param server
	 *            服务器列表
	 * @return String
	 */
	private static String assemblyServerList(String host, String port, String server) {
		StringBuilder serverList = new StringBuilder();
		if (StringUtil.isEmpty(server)) {
			serverList = new StringBuilder("http://" + host + ":" + port);
		} else {
			String[] serverArray = server.split(";");
			for (int i = 0; i < serverArray.length; i++) {
				serverList.append("http://").append(serverArray[i]);
				if (i < serverArray.length - 1) {
					serverList.append(";");
				}
			}
		}
		return serverList.toString();
	}

}
