package com.trs.netInsight.config.serualizer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date->Json 转换
 *
 * Create by yan.changjiang on 2017年11月22日
 */
public class DateSerializer extends JsonSerializer<Date> {

	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {
		jsonGenerator.writeString(new SimpleDateFormat(FORMAT).format(date));
	}
}
