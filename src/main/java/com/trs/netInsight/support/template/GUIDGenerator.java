package com.trs.netInsight.support.template;

import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对象唯一标识生成器 结构如下:time(8)-inc(8)-type(4)-tenant(4)-machine(8) Create by
 * yan.changjiang on 2017年11月20日
 */
public class GUIDGenerator {
	private GUIDGenerator() {
	}

	private static AtomicInteger autoIncCounter = new AtomicInteger((new java.util.Random()).nextInt());

	private static final int id = identity();// identity for generator

	/**
	 * 生成唯一标识,根据机器网卡,JVM和Class Loader生成,以保证唯一
	 * 
	 * @return int
	 * @since huangshengbo @ Mar 4, 2011
	 */
	private static int identity() {
		final int machinePiece;// identity for machine
		final int processPiece;// identity for process(jvm & classloader)
		try {
			StringBuilder sb = new StringBuilder();
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				sb.append(nis.nextElement().toString());
			}
			machinePiece = sb.toString().hashCode();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		// identity for jvm
		StringBuilder sb = new StringBuilder();
		sb.append(ManagementFactory.getRuntimeMXBean().getName());
		// identity classloader
		ClassLoader loader = GUIDGenerator.class.getClassLoader();
		sb.append((loader != null) ? System.identityHashCode(loader) : 0);

		processPiece = sb.toString().hashCode();
		return (machinePiece << 16) | (processPiece & 0xFFFF);
	}

	/**
	 * 为指定类型领域对象生成唯一标识(长度为32的16进制形式的字符串)
	 * 
	 * @param type
	 *            领域对象类型
	 * @return String
	 * @since huangshengbo @ Mar 4, 2011
	 */
	@SuppressWarnings("rawtypes")
	public static String generate(Class type) {
		return generate(0, type);
	}

	/**
	 * 为指定类型领域对象生成唯一标识(长度为32的16进制形式的字符串)
	 * 
	 * @param tenant
	 *            租户唯一标识
	 * @param type
	 *            领域对象类型
	 * @return String
	 * @since huangshengbo @ Mar 4, 2011
	 */
	@SuppressWarnings("rawtypes")
	public static String generate(int tenant, Class type) {
		int time = (int) System.currentTimeMillis() / 1000;
		return digits(time, 8) + "-" + digits(autoIncCounter.getAndIncrement(), 8) + "-" + digits(type.hashCode(), 4)
				+ "-" + digits(tenant, 4) + "-" + digits(id, 8);
	}

	/**
	 * 将{@param val}转换成{@param digits}位的16进制数
	 */
	private static String digits(int val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}

	/**
	 * 获取GUID中的租户id
	 *
	 * @param guid
	 *            GUID
	 * @return tenantId
	 */
	public static int tenantId(String guid) {
		String[] components = guid.split("-");
		return Long.decode("0x" + components[3]).intValue();
	}

	public static int type(String guid) {
		String[] components = guid.split("-");
		return Long.decode("0x" + components[2]).intValue();
	}

	public static int generator(String guid) {
		String[] components = guid.split("-");
		return Long.decode("0x" + components[4]).intValue();
	}

	/**
	 * 生成文件名
	 *
	 * @return machine(8)-inc(8)
	 */
	public static String generateName() {
		return digits(id, 8) + digits(autoIncCounter.getAndIncrement(), 8);
	}

}
