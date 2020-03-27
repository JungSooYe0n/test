package com.trs.netInsight.util.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import com.trs.netInsight.config.constant.Const;

public class PhantomjsFactory {
	private static PhantomjsFactory phantomjs;
	
	public static final String URL="http://192.168.200.5:9111/index.html ";
	public static final String JS_PATH="/home/trs/phantomjs/phantomjs4netinsight.js ";
	
	private PhantomjsFactory(){};
	
	public synchronized static PhantomjsFactory getInstance(){
		if(phantomjs==null){
			phantomjs = new PhantomjsFactory();
			return phantomjs;
		}else{
			return phantomjs;
		}
	}
	public String[] produceImage(List<String> phantomjsData) throws Exception{
		//数据格式处理
		String dataFormated = dataFormatHandling(phantomjsData);
		Runtime runtime = Runtime.getRuntime();
		//Process process = runtime.exec("phantomjs "+JS_PATH+URL);
		Process process = runtime.exec("phantomjs "+JS_PATH+URL+dataFormated);
		InputStream inputStream = process.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer str  =  new StringBuffer();
		String a= "";
		while((a=bufferedReader.readLine())!=null){
			str.append(a);
		}
		// START	console to txt
		String[] split = str.toString().split("#TRS#");
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("D:/phantomjs/jstest/11.txt"));
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
		//bufferedWriter.write(split.length);
		for(String str2 : split){
			bufferedWriter.newLine();
			bufferedWriter.write(str2);
		}
		bufferedWriter.close();
		//END
		String[] base64ImageArr = dataFormatHandling(str.toString());
		return base64ImageArr;
	}
	private String dataFormatHandling(List<String> phantomjsData){
		StringBuffer dataFormated = new StringBuffer();
		//phantomjsData.forEach(e ->{dataFormated.append(e+Const.TRS_SEPARATOR);});
		for (int i = 0; i < phantomjsData.size(); i++) {
			if(i==(phantomjsData.size()-1)){
//				phantomjs 传输 " 有问题 只能传 '
				dataFormated.append(phantomjsData.get(i).replace('"', '\''));
			}else{
				dataFormated.append(phantomjsData.get(i).replace('"', '\'')+Const.TRS_SEPARATOR);
			}
		}
		return dataFormated.toString();
	}
	private String[] dataFormatHandling(String data){
		String[] split = data.split(Const.TRS_SEPARATOR);
		return split;
	}
}
