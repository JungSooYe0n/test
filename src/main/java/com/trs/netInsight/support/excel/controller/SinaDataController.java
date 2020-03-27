package com.trs.netInsight.support.excel.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.excel.entity.SinaData;
import com.trs.netInsight.support.excel.service.ISinaDataService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.util.HttpUtil;
import com.trs.netInsight.util.UserUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/11/15.
 * @desc
 */
@Slf4j
@RestController
@RequestMapping("/sinaData")
@Api(description = "测试微博数据")
public class SinaDataController {
    @Autowired
    private FullTextSearch hybase8SearchService;

    @Autowired
    private ISinaDataService sinaDataService;

    @RequestMapping(value = "/saveSinaData", method = RequestMethod.POST)
    @ApiOperation("测试微博数据")
    @FormatResult
    public Object testWeibo() throws Exception {

        List<String> objects = readXls();
        for (String object : objects) {
            GroupResult irLat = null;
            try {
                irLat = hybase8SearchService.categoryQuery(false, "IR_UID:" + object , false, false,false, "IR_VRESERVED5", Integer.MAX_VALUE, "dc_sinaweibo_0401_test1116");
                QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.setDatabase("dc_sinaweibo_0401_test1116");
                queryBuilder.filterByTRSL("IR_UID:" + object);
                long l = hybase8SearchService.ftsCount(queryBuilder, false, false,false,null);
            } catch (TRSSearchException e) {
                log.error("IR_LAT分类统计错误："+e,e);
            }
            if (null != irLat){
                List<GroupInfo> groupList = irLat.getGroupList();
                if (null != groupList && groupList.size()>0){
                    for (GroupInfo groupInfo : groupList) {
                        SinaData sinaData = new SinaData();
                        sinaData.setUserId(UserUtils.getUser().getId());
                        sinaData.setType("lat");
                        sinaData.setUid(String.valueOf(object));
                        sinaData.setData(groupInfo.getFieldValue());
                        sinaData.setCount(groupInfo.getCount());

                        sinaDataService.save(sinaData);
                    }
                }else {
                    log.info("未查到lat数据的UID："+object);
                }

            }else {
                log.info("未查到lat数据的UID："+object);
            }

        }
        return "success";

//        for (Object object : objects) {
//            GroupResult sreserved1 = hybase8SearchService.categoryQuery(false, "IR_UID:" + (String)object, false, false, "IR_SRESERVED1", Integer.MAX_VALUE, "system.dc_sinaweibo_0401_ldftest");
//            if (sreserved1 != null){
//                List<GroupInfo> groupList = sreserved1.getGroupList();
//                if (null != groupList && groupList.size()>0){
//                    for (GroupInfo groupInfo : groupList) {
//                        //一个IR_UID对应多条数据
//                     //   SinaData sinaData = sinaDataService.findByUid((String) object,"sreserved1");
//                        //if (null == sinaData){
//                        SinaData sinaData = new SinaData();
//                            sinaData.setType("sreserved1");
//                       // }
//                       sinaData.setUserId(UserUtils.getUser().getId());
//                        sinaData.setData(groupInfo.getFieldValue());
//                        sinaData.setCount(groupInfo.getCount());
//                        sinaData.setUid((String) object);
//                        sinaDataService.save(sinaData);
//                    }
//                }else {
//                    log.info("未查到sreserved1数据的UID："+object.toString());
//                    System.err.println("未查到sreserved1数据的UID："+object.toString());
//                }
//            }else {
//                log.info("未查到sreserved1数据的UID："+object.toString());
//                System.err.println("未查到sreserved1数据的UID："+object.toString());
//            }
//
//            GroupResult tag_txt = hybase8SearchService.categoryQuery(false, "IR_UID:" + (String) object, false, false, "IR_TAG_TXT", Integer.MAX_VALUE, "system.dc_sinaweibo_0401_ldftest");
//            if (null != tag_txt){
//                List<GroupInfo> groupList = tag_txt.getGroupList();
//                if (null != groupList && groupList.size()>0){
//                    for (GroupInfo groupInfo : groupList) {
//                      //  SinaData sinaData = sinaDataService.findByUid((String) object,"tagTxt");
//                       // if (null == sinaData){
//                        SinaData  sinaData = new SinaData();
//                            sinaData.setType("tagTxt");
//                       // }
//                       sinaData.setUserId(UserUtils.getUser().getId());
//                        sinaData.setData(groupInfo.getFieldValue());
//                        sinaData.setCount(groupInfo.getCount());
//                        sinaData.setUid((String)object);
//                        sinaDataService.save(sinaData);
//                    }
//                }else {
//                    log.info("未查到tagTxt数据的UID："+object.toString());
//                    System.err.println("未查到tagTxt数据的UID："+object.toString());
//                }
//            }else {
//                log.info("未查到tagTxt数据的UID："+object.toString());
//                System.err.println("未查到tagTxt数据的UID："+object.toString());
//            }
//        }
    }

    public  List<String> readXls() throws Exception {
        InputStream is = new FileInputStream("D:/sina_data.xlsx");

        //HSSFWorkbook excel = new HSSFWorkbook(is);
        XSSFWorkbook excel = new XSSFWorkbook(is);
        ArrayList<String> list = new ArrayList<>();

        // 循环工作表Sheet
        for (int numSheet = 0; numSheet < excel.getNumberOfSheets(); numSheet++) {
            //HSSFSheet sheet = excel.getSheetAt(numSheet);
            XSSFSheet sheet = excel.getSheetAt(numSheet);
            if (sheet == null)
                continue;
            // 循环行Row
            for (int rowNum = 0; rowNum < sheet.getLastRowNum()+1; rowNum++) {
                //HSSFRow row = sheet.getRow(rowNum);
                XSSFRow row = sheet.getRow(rowNum);
                if (row == null)
                    continue;

                //取第一列数据
                //HSSFCell cell0 = row.getCell(0);
                XSSFCell cell0 = row.getCell(0);
                if (cell0 == null)
                    continue;
                long numericCellValue =(long)cell0.getNumericCellValue();
                String value = String.valueOf(numericCellValue);
                list.add(value);
//                String stringCellValue = cell0.getStringCellValue();
//                list.add(stringCellValue.substring(stringCellValue.length()-10));
            }
        }
        return list;
    }


    @RequestMapping(value = "/ceShiData1", method = RequestMethod.POST)
    @ApiOperation("测试微博数据1")
    @FormatResult
    public Object ceShiData1() throws Exception {
        String url = "http://dc.trs.org.cn/dcrawlernetinsight/NetInsightResource/getContent";
        HashMap<String, String> map = new HashMap<>();
        map.put("urlName","http://sports.eastday.co170041445000000.html");
        String doPost = HttpUtil.doPost(url, map, "utf-8");
        if (doPost.contains("\"ok\":0")){
            Map<String,String> dataMap = (Map<String,String>)JSON.parse(doPost);
            String result = dataMap.get("result");
            System.err.println("成功："+result);
            return result;
        }else {
            Map<String,String> dataMap = (Map<String,String>)JSON.parse(doPost);
            String result = dataMap.get("failed");
            System.err.println("失败："+result);
            return result;
        }

    }

    @RequestMapping(value = "/ceShiData2", method = RequestMethod.POST)
    @ApiOperation("测试微博数据2")
    @FormatResult
    public Object ceShiData2() throws Exception {
        String url = "http://dc.trs.org.cn/dcrawlernetinsight/NetInsightResource/submitData";
        HashMap<String, JSONObject> map = new HashMap<>();
       String jsonStr="[{\"DBNAME\":\"DCNewArticle\",\"one\":\"yang\",\"two\":\"yanyan\"}]";
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        map.put("data",jsonObject);
        String doPost = HttpUtil.doDataPost(url, map, "utf-8");
        if (doPost.contains("\"ok\":0")){
            Map<String,String> dataMap = (Map<String,String>)JSON.parse(doPost);
            String result = dataMap.get("msg");
            System.err.println("成功："+result);
            return result;
        }else {
            Map<String,String> dataMap = (Map<String,String>)JSON.parse(doPost);
            String result = dataMap.get("failed");
            System.err.println("失败："+result);
            return result;
        }

    }



    @RequestMapping(value = "/ceShiData3", method = RequestMethod.POST)
    @ApiOperation("测试微博数据3")
    @FormatResult
    public Object ceShiData3() throws Exception {
        String url = "http://dc.trs.org.cn/dcrawlernetinsight/NetInsightResource/submitMetaSearchPoints";
        HashMap<String, String> map = new HashMap<>();
        map.put("keywords","北京;我们");
        String doPost = HttpUtil.doPost(url, map, "utf-8");
        if (doPost.contains("\"ok\":0")){
            Map<String,String> dataMap = (Map<String,String>)JSON.parse(doPost);
            String result = dataMap.get("msg");
            System.err.println("成功："+result);
            return result;
        }else {
            Map<String,String> dataMap = (Map<String,String>)JSON.parse(doPost);
            String result = dataMap.get("failed");
            System.err.println("失败："+result);
            return result;
        }

    }
}
