package com.trs.netInsight.util;

import com.trs.netInsight.handler.exception.OperationException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil<T> {

    /**
     * 适用于第一行是标题行的excel，例如
     * 姓名	年龄	性别	身高
     * 张三	25	男	175
     * 李四	22	女	160
     * 每一行构成一个map，key值是列标题，value是列值。没有值的单元格其value值为null
     * 返回结果最外层的list对应一个excel文件，第二层的list对应一个sheet页，第三层的map对应sheet页中的一行
     */
    public static List<List<HashMap<String, String>>> readExcelWithTitle(String filepath) throws Exception{
        String fileType = filepath.substring(filepath.lastIndexOf(".") + 1, filepath.length());
        InputStream is = null;
        Workbook wb = null;
        try {
            is = new FileInputStream(filepath);

            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook(is);
            } else if (fileType.equals("xlsx")) {
                wb = new XSSFWorkbook(is);
            } else {
                throw new OperationException("上传文件格式错误！");
            }

            List<List<HashMap<String, String>>> result = new ArrayList<List<HashMap<String,String>>>();//对应excel文件

            int sheetSize = wb.getNumberOfSheets();
            for (int i = 0; i < sheetSize; i++) {//遍历sheet页
                Sheet sheet = wb.getSheetAt(i);
                List<HashMap<String, String>> sheetList = new ArrayList<HashMap<String, String>>();//对应sheet页

                List<String> titles = new ArrayList<String>();//放置所有的标题

                int rowSize = sheet.getLastRowNum() + 1;
                for (int j = 0; j < rowSize; j++) {//遍历行
                    Row row = sheet.getRow(j);
                    if (row == null) {//略过空行
                        continue;
                    }
                    int cellSize = row.getLastCellNum();//行中有多少个单元格，也就是有多少列
                    if (j == 0) {//第一行是标题行
                        for (int k = 0; k < cellSize; k++) {
                            Cell cell = row.getCell(k);
                            titles.add(cell.toString());
                        }
                    } else {//其他行是数据行
                        HashMap<String, String> rowMap = new HashMap<String, String>();//对应一个数据行
                        for (int k = 0; k < titles.size(); k++) {
                            Cell cell = row.getCell(k);
                            String key = titles.get(k);
                            String value = null;
                            if (cell != null) {
                                value = cell.toString();
                            }
                            rowMap.put(key, value);
                        }
                        sheetList.add(rowMap);
                    }
                }
                result.add(sheetList);
            }

            return result;
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            if (wb != null) {
                wb.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }
    public static List<List<HashMap<String, String>>> readExcelWithTitleFile(File filepath) throws Exception{
        InputStream is = null;
        Workbook wb = null;
        try {
            is = new FileInputStream(filepath);

//            if (fileType.equals("xls")) {
//                wb = new HSSFWorkbook(is);
//            } else if (fileType.equals("xlsx")) {
                wb = new XSSFWorkbook(is);
//            } else {
//                throw new OperationException("上传文件格式错误！");
//            }

            List<List<HashMap<String, String>>> result = new ArrayList<List<HashMap<String,String>>>();//对应excel文件

            int sheetSize = wb.getNumberOfSheets();
            for (int i = 0; i < sheetSize; i++) {//遍历sheet页
                Sheet sheet = wb.getSheetAt(i);
                List<HashMap<String, String>> sheetList = new ArrayList<HashMap<String, String>>();//对应sheet页

                List<String> titles = new ArrayList<String>();//放置所有的标题

                int rowSize = sheet.getLastRowNum() + 1;
                for (int j = 0; j < rowSize; j++) {//遍历行
                    Row row = sheet.getRow(j);
                    if (row == null) {//略过空行
                        continue;
                    }
                    int cellSize = row.getLastCellNum();//行中有多少个单元格，也就是有多少列
                    if (j == 0) {//第一行是标题行
                        for (int k = 0; k < cellSize; k++) {
                            Cell cell = row.getCell(k);
                            titles.add(cell.toString());
                        }
                    } else {//其他行是数据行
                        HashMap<String, String> rowMap = new HashMap<String, String>();//对应一个数据行
                        boolean isgo = true;
                        for (int k = 0; k < titles.size(); k++) {
                            Cell cell =  row.getCell(k);
                            String key = titles.get(k);
                            String value = null;
                            if (cell != null) {
                                value = cell.toString();
                                switch (cell.getCellType()){
                                    case Cell.CELL_TYPE_NUMERIC:
                                        cell.setCellType(CellType.STRING);
                                        value = String.valueOf(cell.getStringCellValue());
                                        break;
                                }

                            }
                            if (StringUtil.isEmpty(value) && k < (titles.size() - 1)){
                                isgo = false;
                            }
                            rowMap.put(key, value);
                        }
                        if (isgo) sheetList.add(rowMap);
                    }
                }
                result.add(sheetList);
            }
            if (filepath.isFile()) {  // 为文件时调用删除文件方法
                filepath.delete();
            }
            return result;
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            if (wb != null) {
                wb.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }
    /**
     * 删除文件.
     * @param fileDir  文件路径
     */
    public boolean deleteExcel(String fileDir){
        boolean flag = false;
        File file = new File(fileDir);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                file.delete();
                flag = true;
            }
        }
        return flag;
    }
    public static void downExcel( HttpServletResponse res,String newsFile,String name){
        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + name+".xlsx");
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = res.getOutputStream();
            File file = new File(newsFile);
            res.setHeader("content-length",file.length()+"");
            bis = new BufferedInputStream(new FileInputStream(file));
            int m = 0;
            while ((m=bis.read(buff)) != -1) {
                os.write(buff, 0, m);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * excel导出到输出流
     * 谁调用谁负责关闭输出流
     * @param os 输出流
     * @param excelExtName excel文件的扩展名，支持xls和xlsx，不带点号
     * @param data
     * @throws IOException
     */
    public static void writeExcel(OutputStream os, String excelExtName, Map<String, List<List<String>>> data) throws IOException {
        Workbook wb = null;
        try {
            if ("xls".equals(excelExtName)) {
                wb = new HSSFWorkbook();
            } else if ("xlsx".equals(excelExtName)) {
                wb = new XSSFWorkbook();
            } else {
                throw new Exception("当前文件不是excel文件");
            }
            for (String sheetName : data.keySet()) {
                Sheet sheet = wb.createSheet(sheetName);
                List<List<String>> rowList = data.get(sheetName);
                for (int i = 0; i < rowList.size(); i++) {
                    List<String> cellList = rowList.get(i);
                    Row row = sheet.createRow(i);
                    for (int j = 0; j < cellList.size(); j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(cellList.get(j));
                    }
                }
            }
            wb.write(os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (wb != null) {
                wb.close();
            }
            if (os != null){
                os.close();
            }
        }
    }




}

