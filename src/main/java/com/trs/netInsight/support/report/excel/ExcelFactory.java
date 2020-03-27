package com.trs.netInsight.support.report.excel;

import com.trs.netInsight.config.constant.ExcelConst;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * excel实例工厂类
 *
 * Create by yan.changjiang on 2017年11月24日
 */
public class ExcelFactory {

	private static ExcelFactory excelFactory;

	private ExcelFactory() {
	}

	/**
	 * 获取ExcelFactory实例
	 *
	 * @return ExcelFactory
	 */
	public synchronized static ExcelFactory getInstance() {
		if (excelFactory == null) {
			excelFactory = new ExcelFactory();
		}
		return excelFactory;
	}

	/**
	 * 导出数据到Excel
	 */
	@SuppressWarnings({ "resource", "deprecation" })
	public ByteArrayOutputStream export(ExcelData data) throws IOException {
		Workbook  workbook = new  XSSFWorkbook();//xlsx
//		Workbook  workbook = new  HSSFWorkbook();//xls
		Sheet  sheet = workbook.createSheet(ExcelConst.EXCEL_SHEET);

		// 标头样式
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);

		// 填充表头
		Row row = sheet.createRow(0);
		Cell cell;
		for (int i = 0; i < data.column(); i++) {
			cell = row.createCell(i);
			cell.setCellValue(data.getHead(i));
			cell.setCellStyle(style);
		}

		//填充头和内容
		List<List<String>> rowAndCell = data.getRowAndCell();
		for (int i = 0; i < rowAndCell.size(); i++) {
			Row row1 = sheet.createRow(0);
			Cell cell1;
			List<String> strings = rowAndCell.get(i);
			for (int j = 0; j < strings.size(); j++) {
				cell1 = row1.createCell(j);
				cell1.setCellValue(data.getHead(j));
				cell1.setCellStyle(style);
			}

		}
		// 超链接样式
		CellStyle linkStyle = workbook.createCellStyle();
		Font cellFont = workbook.createFont();
		cellFont.setUnderline((byte) 1);
		cellFont.setColor(HSSFColor.BLUE.index);
		linkStyle.setFont(cellFont);

		// 填充数据
		Row newRow;
		for (int i = 0; i < data.row(); i++) {
			List<DataRow> dataRow = data.getRow(i);
			newRow = sheet.createRow(i + 1);
			for (int j = 0; j < dataRow.size(); j++) {
				Object value = dataRow.get(j).getValue();
				if (value instanceof Date) {
					newRow.createCell(j).setCellValue(
							new SimpleDateFormat(DateUtil.yyyyMMdd).format((Date) dataRow.get(j).getValue()));
				} else if (StringUtil.isNotEmpty(dataRow.get(j).getLink())) {
					Cell hyperCell = newRow.createCell(j);
					String url = dataRow.get(j).getLink();
					url = url.startsWith("http://") ? url : "http://" + url;
					hyperCell.setCellType(CellType.STRING);
					hyperCell.setCellStyle(linkStyle);
					try{
						Hyperlink hyperlink =  workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
						hyperlink.setAddress(url);
						hyperCell.setCellValue(url);
						hyperCell.setHyperlink(hyperlink);
						hyperCell.setCellValue(url);
					}catch(Exception e){
						hyperCell.setCellValue(url);
					}
				} else {
					Object dataValue = dataRow.get(j).getValue();
					if(ObjectUtil.isNotEmpty(dataValue)){
						String string = dataValue.toString();
						if(dataValue.toString().length()>32766){//最大一格存储32767
							string = string.substring(0, 32765);
						}
						Boolean isNum = string.matches("^(-?\\d+)(\\.\\d+)?$");
						Boolean isInteger  = string.matches("^[-\\+]?[\\d]*$");
						Cell oneCell = newRow.createCell(j);
						if(isNum){
							CellStyle numStyle = workbook.createCellStyle();
							if(isInteger){
								numStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,#0"));
								oneCell.setCellValue((int)Double.parseDouble(string.trim()));
							}else {
								numStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
								oneCell.setCellValue(Double.parseDouble(string.trim()));
							}
							oneCell.setCellStyle(numStyle);
						}else {
							newRow.createCell(j).setCellValue(string);
						}
					} else if(dataValue != null && "0".equals(dataValue.toString())){
						String string = dataValue.toString();
						Cell oneCell = newRow.createCell(j);
						CellStyle numStyle = workbook.createCellStyle();
						numStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,#0"));
						oneCell.setCellStyle(numStyle);
						oneCell.setCellValue((int)Double.parseDouble(string.trim()));
					}else{
						newRow.createCell(j).setCellValue("");
					}
					
				}
			}
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		workbook.write(os);
		return os;
	}

	/**
	 * 导出数据到Excel
	 */
	@SuppressWarnings({ "resource", "deprecation" })
	public ByteArrayOutputStream exportOfManySheet(ExcelData data) throws IOException {
		Workbook workbook = new XSSFWorkbook();//xlsx
		Map<String, LinkedHashMap<Integer, List<DataRow>>> sheets = data.getSheet();
		for (String key : sheets.keySet()) {
			LinkedHashMap<Integer, List<DataRow>> sheet_row = sheets.get(key);

			Sheet sheet = workbook.createSheet(key);

			// 标头样式
			CellStyle style = workbook.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);

			// 填充表头
			Row row = sheet.createRow(0);
			Cell cell;
			List< String > headList = data.getHeadList(key);
			for (int i = 0; i < headList.size(); i++) {
				cell = row.createCell(i);
				cell.setCellValue(headList.get(i));
				cell.setCellStyle(style);
			}

			//填充头和内容
			List<List<String>> rowAndCell = data.getRowAndCell();
			for (int i = 0; i < rowAndCell.size(); i++) {
				Row row1 = sheet.createRow(0);
				Cell cell1;
				List<String> strings = rowAndCell.get(i);
				for (int j = 0; j < strings.size(); j++) {
					cell1 = row1.createCell(j);
					cell1.setCellValue(data.getHead(j));
					cell1.setCellStyle(style);
				}

			}
			// 超链接样式
			CellStyle linkStyle = workbook.createCellStyle();
			Font cellFont = workbook.createFont();
			cellFont.setUnderline((byte) 1);
			cellFont.setColor(HSSFColor.BLUE.index);
			linkStyle.setFont(cellFont);

			// 填充数据
			Row newRow;
			for (int i = 0; i < sheet_row.size(); i++) {
				List<DataRow> dataRow = sheet_row.get(i);
				newRow = sheet.createRow(i + 1);
				for (int j = 0; j < dataRow.size(); j++) {
					Object value = dataRow.get(j).getValue();
					if (value instanceof Date) {
						newRow.createCell(j).setCellValue(
								new SimpleDateFormat(DateUtil.yyyyMMdd).format((Date) dataRow.get(j).getValue()));
					} else if (StringUtil.isNotEmpty(dataRow.get(j).getLink())) {
						Cell hyperCell = newRow.createCell(j);
						String url = dataRow.get(j).getLink();
						url = url.startsWith("http://") ? url : "http://" + url;
						hyperCell.setCellType(CellType.STRING);
						hyperCell.setCellStyle(linkStyle);
						try {
							Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
							hyperlink.setAddress(url);
							hyperCell.setCellValue(url);
							hyperCell.setHyperlink(hyperlink);
							hyperCell.setCellValue(url);
						} catch (Exception e) {
							hyperCell.setCellValue(url);
						}
					} else {
						Object dataValue = dataRow.get(j).getValue();
						if (ObjectUtil.isNotEmpty(dataValue)) {
							String string = dataValue.toString();
							if (dataValue.toString().length() > 32766) {//最大一格存储32767
								string = string.substring(0, 32765);
						}
							Boolean isNum = string.matches("^(-?\\d+)(\\.\\d+)?$");
							Boolean isInteger  = string.matches("^[-\\+]?[\\d]*$");
							Cell oneCell = newRow.createCell(j);
							if(isNum){
								CellStyle numStyle = workbook.createCellStyle();
								if(isInteger){
									numStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,#0"));
									oneCell.setCellValue((int)Double.parseDouble(string.trim()));
								}else {
									numStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
									oneCell.setCellValue(Double.parseDouble(string.trim()));
								}
								oneCell.setCellStyle(numStyle);
							}else {
								newRow.createCell(j).setCellValue(string);
							}
						} else if(dataValue != null && "0".equals(dataValue.toString())){
							String string = dataValue.toString();
							Cell oneCell = newRow.createCell(j);
							CellStyle numStyle = workbook.createCellStyle();
							numStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,#0"));
							oneCell.setCellStyle(numStyle);
							oneCell.setCellValue((int)Double.parseDouble(string.trim()));
						} else {
							newRow.createCell(j).setCellValue("");
						}

					}
				}
			}
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		workbook.write(os);
		return os;
	}
}
