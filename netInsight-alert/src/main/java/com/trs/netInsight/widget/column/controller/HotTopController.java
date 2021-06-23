package com.trs.netInsight.widget.column.controller;

import com.alibaba.fastjson.JSONArray;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.trs.ckm.soap.CkmSoapException;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.ckm.entity.SegWord;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsRankList;
import com.trs.netInsight.support.fts.entity.FtsRankListHtb;
import com.trs.netInsight.support.fts.entity.FtsRankListRsb;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.column.entity.HotTop;
import com.trs.netInsight.widget.column.service.impl.HotTopRepository;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//import com.alibaba.fastjson.JSONObject;

//import net.sf.json.JSONObject;

/**
 * 热榜操作接口
 *
 * @author 拓尔思信息技术股份有限公司
 * @since gao @ 2021年5月10日
 *
 */
@RestController
@RequestMapping("/hotTop")
@Api(description = "榜单接口")
@Slf4j
public class HotTopController {
    @Autowired
    private ICommonListService commonListService;
    @Autowired
    private HotTopRepository hotTopRepository;
    @Autowired
    private ICkmService iCkmService;
    @FormatResult
    @GetMapping(value = "/getSelectHotTop")
    @ApiOperation("各榜单热度数据接口")
    public Object getHotTopList() throws TRSException {
        User user = UserUtils.getUser();
        List<HotTop> hotTops = hotTopRepository.findByUserIdOrderBySequence(user.getId());
        if (hotTops.size() > 0){
            log.info(hotTops.toString());
        }else {
            hotTops.add(new HotTop("微博",1,false,"热搜榜;话题榜"));
            hotTops.add(new HotTop("百度",2,false,"百度热榜"));
            hotTops.add(new HotTop("360",3,false,"360热搜榜"));
            hotTops.add(new HotTop("搜狗",4,false,"热搜榜;微信热搜词"));
            hotTops.add(new HotTop("腾讯",5,false,"腾讯新闻话题榜"));
            hotTops.add(new HotTop("今日头条",6,false,"头条热榜"));
            hotTops.add(new HotTop("抖音",7,false,"抖音热榜"));
            hotTops.add(new HotTop("知乎",8,false,"热榜;热搜榜"));
            hotTops.add(new HotTop("哔哩哔哩",9,false,"哔哩哔哩排行榜"));
            hotTops.add(new HotTop("澎湃",10,false,"澎湃热新闻"));
            hotTops.add(new HotTop("天涯",11,false,"天涯热榜"));
            hotTopRepository.save(hotTops);
        }
        List result = new ArrayList();
        for (HotTop hotTop: hotTops) {
            HashMap hashMap = new HashMap();
            hashMap.put("name",hotTop.getName());
            hashMap.put("sequence",hotTop.getSequence());
            hashMap.put("hide",hotTop.isHide());
            List list = new ArrayList();
            String childrenSort = hotTop.getChildrenSort();
            String[] children = childrenSort.split(";|；");
            for (String child : children) {
                list.add(child);
            }
            hashMap.put("childrenSort",list);
            result.add(hashMap);
        }
        return result;
    }
    @FormatResult
    @RequestMapping(value = "/setSelectHotTop", method = RequestMethod.POST)
    @ApiOperation("各榜单热度数据接口")
    public Object setHotTopList(@ApiParam("数据") @RequestParam(value = "data", required = true) String data) throws TRSException {
        data = data.trim();
        JSONArray array = JSONArray.parseArray(data);

//        List<Object> filter = array.stream().filter((Predicate<? super Object>) array).collect(Collectors.toList());
        List<Object> filter = new Gson().fromJson(array.toString(),new TypeToken<List<Object>>(){}.getType());
        User user = UserUtils.getUser();
        List<HotTop> hotTops = hotTopRepository.findByUserId(user.getId());
        for (HotTop hotTop: hotTops) {
            for (Object json : filter) {
                log.info(json.toString());
                com.alibaba.fastjson.JSONObject parseObject = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(JSONObject.fromObject(json)));
                int sequence = parseObject.getInteger("sequence");
                String name = parseObject.getString("name");
                if (name.equals(hotTop.getName())){
                    hotTop.setHide(parseObject.getBoolean("hide"));
                    hotTop.setSequence(sequence);
                    hotTop.setChildrenSort(parseObject.getString("childrenSort"));
                }
            }
        }
        hotTopRepository.save(hotTops);
        return "success";

    }
    @FormatResult
    @GetMapping(value = "/hotList")
    @ApiOperation("各榜单热度数据接口")
    public Object hotList(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = true) String timeRange,
                                 @ApiParam("榜单类型") @RequestParam(value = "siteName", required = true) String siteName,
                                 @ApiParam("频道类型") @RequestParam(value = "channelName", required = false) String channelName,
                                 @ApiParam("关键词") @RequestParam(value = "keyword", required = false) String keyword) throws TRSException {
        QueryBuilder queryBuilder = new QueryBuilder();
        String[] timeArray = DateUtil.formatTimeRange(timeRange);
//        queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
        queryBuilder.filterField(FtsFieldConst.FIELD_LASTTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        queryBuilder.setPageNo(0);
        queryBuilder.setPageSize(50);
        queryBuilder.orderBy("IR_LASTTIME", true);
        if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName)) {
            queryBuilder.orderBy("IR_RANK", false);
        }
        if (ObjectUtil.isNotEmpty(channelName)) queryBuilder.filterField(FtsFieldConst.FIELD_CHANNEL,channelName,Operator.Equal);
        if (ObjectUtil.isNotEmpty(siteName)) queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME,siteName,Operator.Equal);
        if (StringUtil.isNotEmpty(keyword)) {
//            String trsl = "IR_URLTITLE:" + keyword;
//            queryBuilder.filterByTRSL(trsl);
            String[] split = keyword.split("\\s+|,");
            String splitNode = "";
            for (int i = 0; i < split.length; i++) {
                if (StringUtil.isNotEmpty(split[i])) {
                    splitNode += split[i] + ",";
                }
            }
            keyword = splitNode.substring(0, splitNode.length() - 1);
            if (keyword.endsWith(";") || keyword.endsWith(",") || keyword.endsWith("；")
                    || keyword.endsWith("，")) {
                keyword = keyword.substring(0, keyword.length() - 1);

            }
            String hybaseField = "IR_URLTITLE";
            if(Const.GROUPNAME_WEIBO.equals(siteName) && "热搜榜".equals(channelName)){
                hybaseField = "IR_HOTWORD";
            }
            StringBuilder fuzzyBuilder = new StringBuilder();
            fuzzyBuilder.append(hybaseField).append(":((\"").append(keyword.replaceAll("[,|，]+", "\") AND (\"")
                    .replaceAll("[;|；]+", "\" OR \"")).append("\"))");
            queryBuilder.filterByTRSL(fuzzyBuilder.toString());
        }
        if (Const.GROUPNAME_WEIBO.equals(siteName)){
            queryBuilder.setDatabase("热搜榜".equals(channelName) ? Const.WEIBO_RSB : Const.WEIBO_HTB);
            if ("热搜榜".equals(channelName)){
                //微博热搜榜
                PagedList<FtsRankListRsb> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankListRsb.class,false,false,false,"hotTop");
                List<FtsRankListRsb> list = ftsPageList.getPageItems();
                List<FtsRankListRsb> listTemp = new ArrayList();
                for(int i=0;i<list.size();i++){//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if(listTemp.get(j).getHotWord().contains(list.get(i).getHotWord())){
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) listTemp.add(list.get(i));
                    if (listTemp.size() > 9) break;

                }
                SortListRsb sortList = new SortListRsb();
                //按时间排序
                Collections.sort(listTemp, sortList);
                List<Object> resultList = new ArrayList<>();
                for (FtsRankListRsb vo : listTemp) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title",vo.getHotWord());
                    map.put("heat",vo.getDescExtr());
                    resultList.add(map);
                }
                return resultList;
            }else {
                //微博话题榜
//                queryBuilder.orderBy("IR_READNUM", true);
                PagedList<FtsRankListHtb> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankListHtb.class,false,false,false,"hotTop");
                List<FtsRankListHtb> list = ftsPageList.getPageItems();
                SortListHtb sortList = new SortListHtb();
                Collections.sort(list, sortList);
                List<FtsRankListHtb> listTemp = new ArrayList();
                for(int i=0;i<list.size();i++){//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if(listTemp.get(j).getTitle().contains(list.get(i).getTitle())){
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) listTemp.add(list.get(i));
                    if (listTemp.size() > 9) break;

                }
                List<Object> resultList = new ArrayList<>();
                for (FtsRankListHtb vo : listTemp) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title",vo.getTitle());
                    map.put("heat",vo.getReadNum());
                    resultList.add(map);
                }
                return resultList;
            }
        }else {
            queryBuilder.setDatabase(Const.DC_BANGDAN);
            PagedList<FtsRankList> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankList.class,false,false,false,"hotTop");
            List<FtsRankList> list = ftsPageList.getPageItems();
            List<FtsRankList> listTemp = new ArrayList();
            for(int i=0;i<list.size();i++){//排重取十条数据
                boolean isAdd = true;
                for (int j = 0; j < listTemp.size(); j++) {
                    if(listTemp.get(j).getTitle().contains(list.get(i).getTitle())){
                        isAdd = false;
                      break;
                    }
                }
                if (isAdd) listTemp.add(list.get(i));
                if (listTemp.size() > 9) break;

            }
            if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                SortListBangdan sortList = new SortListBangdan();
                Collections.sort(listTemp, sortList);
            }else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName)){

            }else {
                SortListBangdanHeat sortList = new SortListBangdanHeat();
                //按时间排序
                Collections.sort(listTemp, sortList);
            }

            List<Object> resultList = new ArrayList<>();
            for (FtsRankList vo : listTemp) {
                Map<String, Object> map = new HashMap<>();
                map.put("title",vo.getTitle());
                if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                    map.put("heat",vo.getSearchIndex());
                }else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName)){

                }else {
                    map.put("heat",vo.getHeat());
                }

                resultList.add(map);
            }
            return resultList;
        }



    }
    @FormatResult
    @GetMapping(value = "/getSegMakeWord")
    @ApiOperation("获取分词")
    public Object getSegMakeWord(@ApiParam("榜单") @RequestParam(value = "word", required = true) String word) throws TRSException {
        List<SegWord> segList = null;
        try {
            segList = iCkmService.SegMakeWord(word);
        } catch (CkmSoapException e) {
            segList = new ArrayList<>();
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (SegWord segword : segList) {
            stringBuilder.append(segword.getWord()+",");
        }
        String keyWords = stringBuilder.toString().substring(0,stringBuilder.toString().length()-1);
        List result = new ArrayList();
        HashMap map = new HashMap();
        map.put("wordSpace",2000000000);
        map.put("wordOrder",false);
        map.put("keyWords",keyWords);
        result.add(map);
        return result;
    }
    public static List removeDuplicate(List list){
        List listTemp = new ArrayList();
        for(int i=0;i<list.size();i++){
            if(!listTemp.contains(list.get(i))){
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }
}
