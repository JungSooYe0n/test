package com.trs.netInsight.config.serualizer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 参数 Json->Date 转换
 *
 * Create by yan.changjiang on 2017年11月22日
 */
public class DateDeserializer extends JsonDeserializer<Date> {

	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		try {
			return new SimpleDateFormat(FORMAT).parse(jsonParser.getText());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
