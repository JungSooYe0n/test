package com.trs.netInsight.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lilyy
 * @date 2020/4/2 10:40
 */
@Component
public class ReadDayBaogao {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISpecialService specialService;

    public static void main(String args[]){
//        new ReadDayBaogao().readTxt(null);
//        new ReadDayBaogao().getAreaData(1);
        new ReadDayBaogao().yAxisData401_3();

//        String s1 = "12,12,12,12,12,12,12,12,12,12,14,18,38,57,100,130,191,212,285,423,613,949,1126,1412,1784,2281,2876,3667,4500,5423,6650,7730,9134,10995,12612,14459,16018,19856,22302,25233,29566,33414,38105,40723,45170,52836,57763,59929,83029,90853,93780,98984,110070,113982,118783,125931,130730,133670,137877,143303,147863";
//        String ss1[] = s1.split(",");
//        System.out.println(ss1.length);
//        String ss = "2月15日, 2月16日, 2月17日,2月18日, 2月19日, 2月20日,2月21日,2月22日,2月23日,2月24日,2月25日,2月26日,2月27日,2月28日,2月29日,3月1日,3月2日,3月3日,3月4日,3月5日,3月6日,3月7日,3月8日,3月9日,3月10日,3月11日,3月12日,3月13日,3月14日,3月15日,3月16日,3月17日,3月18日,3月19日,3月20日,3月21日,3月22日,3月23日,3月24日,3月25日,3月26日,3月27日,3月28日,3月29日,3月30日,3月30日,3月31日,4月1日,4月2日,4月3日,4月4日,4月5日,4月6日,4月7日,4月8日,4月9日,4月10日,4月11日,4月12日,4月13日,4月14日,4月15日,4月16日";
//        String sss[] = ss.split(",");
//        System.out.println(sss.length);
    }

    /**传入txt路径读取txt文件
     * @return 返回读取到的内容
     */
    public String readTxt(String txtPath) {
        boolean iscontinue = true;
        if(txtPath==null || txtPath.equals("")) txtPath = "D:\\工作文档\\文档\\temp2.txt";
        String imgData = "[ {name:'统计',val1:'static/images/nCoV/',val2:'/tj.png'},{name:'舆情传播分析',val1:'static/images/nCoV/',val2:'/yqcb.png'},{name:'今日热议',val1:'static/images/nCoV/',val2:'/jrry.png'},{name:'聚焦国际',val1:'static/images/nCoV/',val2:'/jjgj.png'},{name:'官方发布',val1:'static/images/nCoV/',val2:'/gffb.png'},{name:'专家观点',val1:'static/images/nCoV/',val2:'/zjgd.png'},{name:'聚焦正能量',val1:'static/images/nCoV/',val2:'/jjznl.png'},{name:'防护知识',val1:'static/images/nCoV/',val2:'/fhzs.png'}]\n";
        File file = new File(txtPath);
        if (file.isFile() && file.exists()) {
            try {

                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                JSONObject jsonObject = new JSONObject();
                JSONObject guonei = new JSONObject(); //国内
                JSONObject guowai = new JSONObject(); //国外
                JSONObject yqcbfx = new JSONObject(); //舆情传播分析
                JSONObject jrry = new JSONObject(); //今日热议
                JSONObject jjgj = new JSONObject(); //聚焦国际
                JSONObject gffb = new JSONObject(); //官方发布
                JSONObject zjgd = new JSONObject(); //专家观点
                JSONObject jjznl = new JSONObject(); //聚焦正能量
                JSONObject jrzdxx = new JSONObject(); //今日重点信息
                JSONObject dpjrry = new JSONObject(); //今日重点信息
                JSONObject fhzs = new JSONObject(); //防护知识

                //今日热议 jrryTopArr401 / jrryArr401
                JSONArray jrryTopArr401 = new JSONArray();
                JSONArray jrryArr401 = new JSONArray();
                //聚焦国际
                JSONArray jjgjArr401 = new JSONArray();
                JSONArray gffbArr401 = new JSONArray();
                JSONArray zjgdArr401 = new JSONArray();
                JSONArray jjznlArr401 = new JSONArray();
                JSONArray jrzdxxArr401 = new JSONArray();
                JSONArray jrdpryArr401 = new JSONArray();

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
                int day = cal.get(Calendar.DATE);
                int month = cal.get(Calendar.MONTH) + 1;

//                JSONObject temp = new JSONObject();
                String key = "";
                String nowtab = "";
                StringBuffer sb = new StringBuffer();
                String text1 = null;
                while ((text1 = bufferedReader.readLine()) != null) {
                    if(text1.contains("--->") || text1.equals("")) continue; //备注和空格直接跳过
                    System.out.println("------>" + text1);
                    if (text1.startsWith("舆情监测区间")){
                        jsonObject.put("yuqingqujian", text1);
                        continue;
                    }
                    if (text1.startsWith("国内疫情")){
                        while (iscontinue){
                            String tt = bufferedReader.readLine();
                            if(tt ==null || tt.contains("--->") || tt.trim().equals("")) continue;
                            if(tt.startsWith("国外疫情")){
                                nowtab = "国外疫情";
                                break;
                            }
                            if (tt.startsWith("国内疫情统计")){
                                guonei.put("countTime", tt);continue;
                            }
                            if (tt.startsWith("zjArr401")){
                                StringBuffer sb2 = new StringBuffer();
                                while (true){
                                    String ttt = bufferedReader.readLine();
                                    sb2.append(ttt);
                                    if(ttt.contains("]")){
                                        break;
                                    }
                                }
                                guonei.put("zjArr401",JSONArray.parseArray(sb2.toString()));
                                continue;
                            }
                            if (tt.startsWith("table401")){
                                StringBuffer sb2 = new StringBuffer();
                                while (true){
                                    String ttt = bufferedReader.readLine();
                                    sb2.append(ttt);
                                    if(ttt.contains("]")){
                                        break;
                                    }
                                }
                                guonei.put("table401",JSONArray.parseArray(sb2.toString()));
                                continue;
                            }
                            if (tt.startsWith("数据来源：")){
                                guonei.put("dataSource", tt);continue;
                            }
                        }
                    }
                    /////////////////国外疫情统计//////////////
                    if (nowtab.equals("国外疫情")){
                        sb.delete(0,sb.length());
                        while (iscontinue) {
                            String tt = bufferedReader.readLine();
                            if(tt ==null || tt.contains("--->") || tt.equals("")) continue;
                            if(!key.equals("") && !tt.startsWith("舆情传播分析")) sb.append(tt);
                            if (tt.startsWith("舆情传播分析")) {
                                String value = sb.toString().replaceAll("yAxisData401_3:", "").replaceAll(" ", "");
                                guowai.put("yAxisData401_3", JSONArray.parseArray(value));
                                key = "";
                                sb.delete(0,sb.length());
                                nowtab = "舆情传播分析";
                                break;
                            }
                            if(tt.startsWith("国外疫情统计")){
                                guowai.put("countTime", tt);continue;
                            }
                            if (tt.startsWith("gwArr401:")){
                                StringBuffer sb2 = new StringBuffer();
                                while (true){
                                    String ttt = bufferedReader.readLine();
                                    sb2.append(ttt);
                                    if(ttt.contains("]")){
                                        break;
                                    }
                                }
                                guowai.put("gwArr401",JSONArray.parseArray(sb2.toString()));
                                continue;
                            }
                            if (tt.startsWith("gwtable401")){
                                StringBuffer sb2 = new StringBuffer();
                                while (true){
                                    String ttt = bufferedReader.readLine();
                                    sb2.append(ttt);
                                    if(ttt.contains("]")){
                                        break;
                                    }
                                }
                                guowai.put("gwtable401",JSONArray.parseArray(sb2.toString()));
                                continue;
                            }
                            if(tt.startsWith("xAxisData401_3")){
                                StringBuffer sb2 = new StringBuffer();
                                while (true){
                                    String ttt = bufferedReader.readLine();
                                    sb2.append(ttt);
                                    if(ttt.contains("]")){
                                        break;
                                    }
                                }
                                guowai.put("xAxisData401_3",JSONArray.parseArray(sb2.toString()));
                                continue;
                            }
                            if(tt.startsWith("legendData401_3")){
                                String value = tt.replaceAll("legendData401_3:", "").replaceAll(" ", "");
                                guowai.put("legendData401_3", JSONArray.parseArray(value));
                                continue;
                            }
                            if(tt.startsWith("yAxisData401_3")){
                                key = "yAxisData401_3";
                                sb.append(tt);
                                continue;
                            }
                        }
                    }
                    if (nowtab.equals("舆情传播分析")) {
                        while (iscontinue) {
                            String tt = bufferedReader.readLine();
                            if(tt==null || tt.contains("--->") || tt.equals("")) continue;
                            if (tt.startsWith("今日热议")) {
                                nowtab = "今日热议";
                                break;
                            }
                            if(tt.startsWith("舆情概述")){
                                yqcbfx.put("yuqinggaishu",tt.replace("舆情概述：",""));
                            }
                            if(tt.startsWith("publicSaynote401==")){
                                yqcbfx.put("publicSaynote401",tt.replace("publicSaynote401==",""));continue;
                            }
                            if(tt.startsWith("yqcbLinenote401==")){
                                yqcbfx.put("yqcbLinenote401",tt.replace("yqcbLinenote401==",""));continue;
                            }
                            //昨日走势今日走势图
                            if(tt.startsWith("areaData401")){
                                yqcbfx.put("areaData401",getAreaData(0));
                                continue;
                            }
                            //昨日走势今日走势图
                            if(tt.startsWith("yAxisData401_4")){
                                StringBuffer sb2 = new StringBuffer();
                                while (true){
                                    String ttt = bufferedReader.readLine();
                                    if(ttt.contains("tableData401")){
                                        key = "tableData401";
                                        break;
                                    }
                                    sb2.append(ttt);
                                }
                                yqcbfx.put("yAxisData401_4",JSONArray.parseArray(sb2.toString()));
                            }
                            //地图右侧数据
                            if(key.equals("tableData401")){
                                yqcbfx.put("tableData401",getAreaData(1));
                                key = "";
                                continue;
                            }
                        }
                    }

                    //////////////今日热议///////////////
                    if (nowtab.equals("今日热议")) {
                        int number = 1;
                        while (iscontinue) {
                            String tt = bufferedReader.readLine();
                            if (tt == null || tt.contains("--->") || tt.equals("")) continue;
                            if (tt.startsWith("聚焦国际")) {
                                jrry.put("jrryTopArr401",jrryTopArr401);
                                jrry.put("jrryArr401",jrryArr401);
                                nowtab = "聚焦国际";
                                break;
                            }
                            if(tt.startsWith("今日舆论热议话题")){
                                jrry.put("title",tt);
                                continue;
                            }
                            if(tt.startsWith("TOP")){
                                JSONObject jd = new JSONObject();
                                JSONObject jtop = new JSONObject();
                                jtop.put("title",tt.split(" ")[1]);
                                String liang = bufferedReader.readLine();
                                String content = bufferedReader.readLine();
                                String detail = bufferedReader.readLine();
                                String id = bufferedReader.readLine();
                                String[] lstr = liang.replace("搜索量：","").replace("阅读","$").split("\\$");
                                jtop.put("search",lstr[0].trim());
                                jtop.put("read",lstr[1].trim());
                                jtop.put("time","2020-02-26 00:00:00;2020-03-05 23:59:59");
                                jtop.put("img","static/images/nCoV/"+month+"m"+day+"d/jrry"+number+".png");
                                jtop.put("id",id.replace("id:",""));
                                //jrryArr401
                                jd.put("title",tt.split(" ")[1]);
                                jd.put("img","static/images/nCoV/newPic/top"+number+".png");
                                jd.put("content",content);
                                jd.put("detail",detail);
                                jd.put("href","");
                                jd.put("id",id.replace("id:",""));
                                jrryTopArr401.add(jtop);
                                jrryArr401.add(jd);
                                number++;
                                continue;
                            }

                        }
                    }
                    //////////////聚焦国际///////////////
                    if (nowtab.equals("聚焦国际")) {
                        int number = 1;
                        while (iscontinue) {
                            String tt = bufferedReader.readLine();
                            if (tt == null || tt.contains("--->") || tt.equals("")) continue;
                            if (tt.startsWith("官方发布")) {
                                nowtab = "官方发布";
                                jjgj.put("jjgjArr401",jjgjArr401);
                                break;
                            }
                            String title = tt;
                            String read = bufferedReader.readLine();
                            String content = bufferedReader.readLine();
                            String detail = bufferedReader.readLine();
                            String id = bufferedReader.readLine();
                            JSONObject jb = new JSONObject();
                            jb.put("img","static/images/nCoV/newPic/top"+number+".png");
                            jb.put("title",title.split("\\.")[1].trim());
                            jb.put("content",content);
                            jb.put("read",read);
                            jb.put("detail",detail);
                            jb.put("href","");
                            jb.put("id",id.replace("id:","").trim());
                            jjgjArr401.add(jb);
                            number++;
                        }
                    }
                    //////////////官方发布///////////////
                    if (nowtab.equals("官方发布")) {
                        while (iscontinue) {
                            String tt = bufferedReader.readLine();
                            if (tt == null || tt.contains("--->") || tt.equals("")) continue;
                            if (tt.startsWith("专家观点")) {
                                nowtab = "专家观点";
                                gffb.put("gffbArr401",gffbArr401);
                                break;
                            }
                            String name = tt;
                            String title = bufferedReader.readLine();
                            String content1 = bufferedReader.readLine();
                            String content2 = bufferedReader.readLine();
                            String href = bufferedReader.readLine();
                            JSONObject jb = new JSONObject();
                            jb.put("name",name);
                            jb.put("title",title);
                            jb.put("content1",content1);
                            jb.put("content2",content2);
                            jb.put("href",href.replace("链接：","").trim());

                            gffbArr401.add(jb);
                        }
                    }
                    //////////////专家观点///////////////
                    if (nowtab.equals("专家观点")) {
                        while (iscontinue) {
                            String tt = bufferedReader.readLine();
                            if (tt == null || tt.contains("--->") || tt.equals("")) continue;
                            if (tt.startsWith("聚焦正能量")) {
                                nowtab = "聚焦正能量";
                                zjgd.put("zjgdArr401",zjgdArr401);
                                break;
                            }
                            String name = tt;
                            String title1 = bufferedReader.readLine();
                            String content1 = bufferedReader.readLine();
                            String href1 = bufferedReader.readLine();
                            String title2 = bufferedReader.readLine();
                            String content2 = bufferedReader.readLine();
                            String href2 = bufferedReader.readLine();
                            JSONObject jb = new JSONObject();
                            jb.put("name",name);
                            jb.put("title1",title1);
                            jb.put("content1",content1);
                            jb.put("href1",href1.replace("链接：","").trim());
                            jb.put("title2",title2);
                            jb.put("content2",content2);
                            jb.put("href2",href2.replace("链接：","").trim());

                            zjgdArr401.add(jb);
                        }
                    }
                    /////////////////聚焦正能量
                    if (nowtab.equals("聚焦正能量")) {
                        int number = 1;
                        while (iscontinue) {
                            String tt = bufferedReader.readLine();
                            if (tt == null || tt.contains("--->") || tt.equals("")) continue;
                            if (tt.startsWith("防护知识")) {
                                nowtab = "防护知识";
                                jjznl.put("jjznlArr401",jjznlArr401);
                                break;
                            }
                            String title = tt;
                            String content = bufferedReader.readLine();
                            String source = bufferedReader.readLine();
                            String href = bufferedReader.readLine();
                            JSONObject jb = new JSONObject();
                            jb.put("title",title);
                            jb.put("content",content);
                            jb.put("href",href.substring(3).trim());
                            jb.put("source",source.substring(5));
                            jb.put("img","static/images/nCoV/"+month+"m"+day+"d/jjznl"+number+".png");
                            jjznlArr401.add(jb);
                            number++;
                        }
                    }
                    //防护知识
                    if (nowtab.equals("防护知识")) {
                        String title = bufferedReader.readLine();
                        String content = bufferedReader.readLine();
                        String source = bufferedReader.readLine();
                        fhzs.put("title",title);
                        fhzs.put("content",content);
                        fhzs.put("source",source);
                        nowtab = "";
                        continue;
                    }
                    if (text1.startsWith("timeData:")){
                        String value = text1.replace("timeData:","");
                        jsonObject.put("timeData",JSONArray.parseArray(value));
                        continue;
                    }
                    if (text1.startsWith("videoSrc:")){
                        jsonObject.put("videoSrc",text1.replace("videoSrc:",""));
                        continue;
                    }
                    if (text1.startsWith("activeName")){
                        JSONObject jba = new JSONObject();
                        String name = text1.replace("activeName:","").trim();
                        String activeValue = bufferedReader.readLine();
                        activeValue = activeValue.replace("activeValue:","").trim();
                        jba.put("name",name);
                        jba.put("value",activeValue);
                        jsonObject.put("active",jba);
                        continue;
                    }
                    if (text1.startsWith("hotInfoList:")){
                        StringBuffer sb2 = new StringBuffer();
                        while (true){
                            String tt = bufferedReader.readLine();
                            sb2.append(tt);
                            if(tt.contains("]")){
                                break;
                            }
                        }
                        JSONArray jsonArray = JSONArray.parseArray(sb2.toString());
                        jsonObject.put("hotInfoList",jsonArray);
                    }


                }
                jsonObject.put("imgData",imgData);
                jsonObject.put("guonei",guonei);
                jsonObject.put("guowai",guowai);
                jsonObject.put("yqcbfx",yqcbfx);
                jsonObject.put("jrry",jrry);
                jsonObject.put("jjgj",jjgj);
                jsonObject.put("gffb",gffb);
                jsonObject.put("zjgd",zjgd);
                jsonObject.put("jjznl",jjznl);
                jsonObject.put("jrzdxx",jrzdxx);
                jsonObject.put("dpjrry",dpjrry);
                jsonObject.put("fhzs",fhzs);
                System.out.println("国内::"+guonei.toJSONString());
                System.out.println("国内::"+guonei.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("国外::"+guowai.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("舆情传播分析::"+yqcbfx.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("今日热议::"+jrry.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("聚焦国际::"+jjgj.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("官方发布::"+gffb.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("专家观点::"+zjgd.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("聚焦正能量::"+jjznl.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("今日重点信息::"+jrzdxx.toJSONString());
                System.out.println("大屏今日热议::"+dpjrry.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("外层::"+jsonObject.toJSONString().replaceAll("\"\\[", "[").replaceAll("]\"", "]"));
                System.out.println("yuqingqujian---"+jsonObject.get("yuqingqujian"));
                System.out.println("yuqingqujian---"+jsonObject.get("yuqingqujian"));
                return jsonObject.toJSONString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     */
    public JSONArray getAreaData(int istable){
        String txtPath = "D:\\工作文档\\文档\\tableArea.txt";
        File file = new File(txtPath);
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "gb2312");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String text1 = null;
                JSONArray jsonArray = new JSONArray();
                JSONArray jsonArrayTop = new JSONArray();
                Map<String,String> map = new HashMap<>();
                map.put("湖北","湖北");
                map.put("北京","北京");
                map.put("广东","广东");
                map.put("上海","上海");
                map.put("山东","河北");
                map.put("浙江","河南");
                map.put("四川","江苏");
                map.put("江苏","四川");
                map.put("黑龙江","1");
                map.put("河南","1");
                while ((text1 = bufferedReader.readLine()) != null) {
                    if(text1.contains("--->") || text1.equals("")) continue; //备注和空格直接跳过
                    JSONObject jsonObject = new JSONObject();
                    String area[] = text1.split("\t");
                    jsonObject.put("name",area[0]);
                    jsonObject.put("value",Integer.parseInt(area[1]));
                    jsonArray.add(jsonObject);
                    if(map.get(area[0].trim())!=null){
                        jsonArrayTop.add(jsonObject);
                    }
                    jsonArray.add(jsonObject);
                }
                System.out.println(jsonArray.toString());
                System.out.println(jsonArrayTop.toString());
                if(istable==1) return jsonArrayTop;
                else return jsonArray;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
    public void yAxisData401_3(){
        String txtPath = "D:\\工作文档\\文档\\temp.txt";
        File file = new File(txtPath);
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "gb2312");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String text1 = null;
                StringBuffer stringBuffer = new StringBuffer();
                while ((text1 = bufferedReader.readLine()) != null) {
                    if(text1.contains("--->") || text1.equals("")) continue; //备注和空格直接跳过
                   stringBuffer.append(text1.trim());
                   stringBuffer.append(",");
                }
                System.out.println(stringBuffer.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
