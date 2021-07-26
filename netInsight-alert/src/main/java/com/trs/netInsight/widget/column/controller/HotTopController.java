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
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
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

import java.text.Collator;
import java.util.*;

//import com.alibaba.fastjson.JSONObject;

//import net.sf.json.JSONObject;

/**
 * 热榜操作接口
 *
 * @author 拓尔思信息技术股份有限公司
 * @since gao @ 2021年5月10日
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
        if (hotTops.size() > 0) {
            log.info(hotTops.toString());
        } else {
            hotTops.add(new HotTop("微博", 1, false, "热搜榜;话题榜;娱乐榜;要闻榜"));
            hotTops.add(new HotTop("百度", 2, false, "百度热搜"));
            hotTops.add(new HotTop("360", 3, false, "360热搜榜"));
            hotTops.add(new HotTop("搜狗", 4, false, "热搜榜;微信热搜词"));
            hotTops.add(new HotTop("腾讯", 5, false, "腾讯新闻话题榜"));
            hotTops.add(new HotTop("今日头条", 6, false, "头条热榜;同城榜"));
            hotTops.add(new HotTop("抖音", 7, false, "热点榜;娱乐榜;社会榜;同城榜"));
            hotTops.add(new HotTop("知乎", 8, false, "热榜;热搜榜"));
            hotTops.add(new HotTop("哔哩哔哩", 9, false, "哔哩哔哩排行榜"));
            hotTops.add(new HotTop("澎湃", 10, false, "澎湃热新闻"));
            hotTops.add(new HotTop("天涯", 11, false, "天涯热榜"));
            hotTopRepository.save(hotTops);
        }
        List result = new ArrayList();
        for (HotTop hotTop : hotTops) {
            HashMap hashMap = new HashMap();
            hashMap.put("name", hotTop.getName());
            hashMap.put("sequence", hotTop.getSequence());
            hashMap.put("hide", hotTop.isHide());
            hashMap.put("location", hotTop.getLocation());
            List list = new ArrayList();
            String childrenSort = hotTop.getChildrenSort();
            String[] children = childrenSort.split(";|；");
            for (String child : children) {
                list.add(child);
            }
            hashMap.put("childrenSort", list);
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
        List<Object> filter = new Gson().fromJson(array.toString(), new TypeToken<List<Object>>() {
        }.getType());
        User user = UserUtils.getUser();
        List<HotTop> hotTops = hotTopRepository.findByUserId(user.getId());
        for (HotTop hotTop : hotTops) {
            for (Object json : filter) {
                log.info(json.toString());
                com.alibaba.fastjson.JSONObject parseObject = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(JSONObject.fromObject(json)));
                int sequence = parseObject.getInteger("sequence");
                String name = parseObject.getString("name");
                if (name.equals(hotTop.getName())) {
                    hotTop.setHide(parseObject.getBoolean("hide"));
                    hotTop.setSequence(sequence);
                    hotTop.setChildrenSort(parseObject.getString("childrenSort"));
                    if ("抖音".equals(name) || "今日头条".equals(name)) {
                        hotTop.setLocation(parseObject.getString("location"));
                    }
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
                          @ApiParam("关键词") @RequestParam(value = "keyword", required = false) String keyword,
                          @ApiParam("同城（对外接口使用,可不传）") @RequestParam(value = "location", required = false) String location) throws TRSException {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterField(FtsFieldConst.FIELD_LASTTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        queryBuilder.setPageNo(0);
        queryBuilder.setPageSize(100);
        queryBuilder.orderBy("IR_LASTTIME", true);
        if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName) || ("微博".equals(siteName) && "要闻榜".equals(channelName))) {
            queryBuilder.orderBy("IR_RANK", false);
        }
        //只判断哪些按热度值排行并且每次条数不是50的  时间一致就按原条数 否则按2倍
        if(Const.HTB_SITENAME_BAIDU.equals(siteName)){
            queryBuilder.setPageSize(30);
        }else if (Const.HTB_SITENAME_SOUDOG_WEINXIN.equals(siteName)){
            queryBuilder.setPageSize(10);
        }else if (Const.HTB_SITENAME_DOUYIN.equals(siteName) && "热点榜".equals(channelName)){
            queryBuilder.setPageSize(50);//时间一致
        }else if (Const.HTB_SITENAME_DOUYIN.equals(siteName) && "同城榜".equals(channelName)){
            queryBuilder.setPageSize(40);//*2
        }else if (Const.HTB_SITENAME_BILIBILI.equals(siteName)){
            queryBuilder.setPageSize(200);
        }else if (Const.HTB_SITENAME_PENGPAI.equals(siteName)){
            queryBuilder.setPageSize(10);
        }else if (Const.HTB_SITENAME_TIANYA.equals(siteName)){
            queryBuilder.setPageSize(40);
        }
        if (ObjectUtil.isNotEmpty(channelName)) {
            queryBuilder.filterField(FtsFieldConst.FIELD_CHANNEL, channelName, Operator.Equal);
            if ("同城榜".equals(channelName)) {
                //只有同城榜需要 IR_CITY 字段 新增字段需要做空判断 默认北京
                if (ObjectUtil.isEmpty(location)) {
                    User user = UserUtils.getUser();
                    String name = siteName.contains("抖音") ? "抖音" : siteName.contains("今日头条") ? "今日头条" : "";
                    if (ObjectUtil.isNotEmpty(user) && StringUtil.isNotEmpty(name)) {
                        List<HotTop> hotTops = hotTopRepository.findByUserIdAndName(user.getId(), name);
                        if (ObjectUtil.isNotEmpty(hotTops)) location = hotTops.get(0).getLocation();
                    } else {
                        location = "北京";
                    }
                }
                queryBuilder.filterField("IR_CITY", ObjectUtil.isNotEmpty(location) ? location : "北京", Operator.Equal);
            }
        }
        if (ObjectUtil.isNotEmpty(siteName))
            queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, siteName, Operator.Equal);
        if (StringUtil.isNotEmpty(keyword)) {
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
            if (Const.GROUPNAME_WEIBO.equals(siteName) && "热搜榜".equals(channelName)) {
                hybaseField = "IR_HOTWORD";
            }
            StringBuilder fuzzyBuilder = new StringBuilder();
            fuzzyBuilder.append(hybaseField).append(":((\"").append(keyword.replaceAll("[,|，]+", "\") AND (\"")
                    .replaceAll("[;|；]+", "\" OR \"")).append("\"))");
            queryBuilder.filterByTRSL(fuzzyBuilder.toString());
        }
        if (Const.GROUPNAME_WEIBO.equals(siteName) && !"要闻榜".equals(channelName)) {
            queryBuilder.setDatabase(Const.DC_BANGDAN);
            if ("热搜榜".equals(channelName) || "娱乐榜".equals(channelName)) {
                //微博热搜榜
                PagedList<FtsRankListRsb> ftsPageList = commonListService.queryPageListForClass(queryBuilder, FtsRankListRsb.class, false, false, false, "hotTop");
                List<FtsRankListRsb> list = ftsPageList.getPageItems();
                List<FtsRankListRsb> listTemp = new ArrayList();
                SortListRsb sortList = new SortListRsb();
                Collections.sort(list, sortList);
                for (int i = 0; i < list.size(); i++) {//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if (listTemp.get(j).getHotWord().contains(list.get(i).getHotWord())) {
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) listTemp.add(list.get(i));
                    if (listTemp.size() > 9) break;

                }

                List<Object> resultList = new ArrayList<>();
                for (FtsRankListRsb vo : listTemp) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", vo.getHotWord());
                    map.put("heat", vo.getHeat());
                    resultList.add(map);
                }
                return resultList;
            } else {
                //微博话题榜
//                queryBuilder.orderBy("IR_READNUM", true);
                queryBuilder.filterField(FtsFieldConst.FENLEI_HOTTOP, "总榜", Operator.Equal);
                PagedList<FtsRankListHtb> ftsPageList = commonListService.queryPageListForClass(queryBuilder, FtsRankListHtb.class, false, false, false, "hotTop");
                List<FtsRankListHtb> list = ftsPageList.getPageItems();
                SortListHtb sortList = new SortListHtb();
                Collections.sort(list, sortList);
                List<FtsRankListHtb> listTemp = new ArrayList();
                for (int i = 0; i < list.size(); i++) {//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if (listTemp.get(j).getTitle().contains(list.get(i).getTitle())) {
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
                    map.put("title", vo.getTitle());
                    map.put("heat", vo.getReadNum());
                    resultList.add(map);
                }
                return resultList;
            }
        } else {
            queryBuilder.setDatabase(Const.DC_BANGDAN);
            PagedList<FtsRankList> ftsPageList = commonListService.queryPageListForClass(queryBuilder, FtsRankList.class, false, false, false, "hotTop");
            List<FtsRankList> list = ftsPageList.getPageItems();
            List<FtsRankList> listTemp = new ArrayList();
            if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                SortListBangdan sortList = new SortListBangdan();
                Collections.sort(list, sortList);
            } else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName) || ("微博".equals(siteName) && "要闻榜".equals(channelName))) {

            } else {
                SortListBangdanHeat sortList = new SortListBangdanHeat();
                //按时间排序
                Collections.sort(list, sortList);
            }
            for (int i = 0; i < list.size(); i++) {//排重取十条数据
                boolean isAdd = true;
                for (int j = 0; j < listTemp.size(); j++) {
                    if (listTemp.get(j).getTitle().contains(list.get(i).getTitle())) {
                        isAdd = false;
                        break;
                    }
                }
                if (isAdd) listTemp.add(list.get(i));
                if (listTemp.size() > 9) break;

            }


            List<Object> resultList = new ArrayList<>();
            for (FtsRankList vo : listTemp) {
                Map<String, Object> map = new HashMap<>();
                map.put("title", vo.getTitle());
                if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                    map.put("heat", vo.getSearchIndex());
                } else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName) || ("微博".equals(siteName) && "要闻榜".equals(channelName))) {
//这些没有热度指数
                } else {
                    map.put("heat", vo.getHeat());
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
            stringBuilder.append(segword.getWord() + ",");
        }
        String keyWords = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
        List result = new ArrayList();
        HashMap map = new HashMap();
        map.put("wordSpace", 2000000000);
        map.put("wordOrder", false);
        map.put("keyWords", keyWords);
        result.add(map);
        return result;
    }

    @FormatResult
    @GetMapping(value = "/getHotTopCity")
    @ApiOperation("获取城市")
    public Object getHotTopCity(@ApiParam("榜单类型") @RequestParam(value = "siteName", required = false) String siteName) throws TRSException {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setPageSize(500);
        queryBuilder.setPageNo(0);
        queryBuilder.filterField(FtsFieldConst.FIELD_LASTTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1("0d"), Operator.Between);
        queryBuilder.filterField(FtsFieldConst.FIELD_CHANNEL, "同城榜", Operator.Equal);
        if (ObjectUtil.isNotEmpty(siteName)) queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, siteName, Operator.Equal);
        queryBuilder.setDatabase(Const.DC_BANGDAN);
        List<String> citys = new ArrayList<>();

        GroupResult groupResult = null;
        groupResult = commonListService.categoryQuery(queryBuilder, false, false, false, "IR_CITY", "hotTop");
        List<GroupInfo> groupList = groupResult.getGroupList();
        for (GroupInfo groupInfo : groupList) {
            citys.add(groupInfo.getFieldValue());
        }
        Comparator comparator = Collator.getInstance(Locale.CHINA);
        Collections.sort(citys, comparator);
        HashMap hashMap = new HashMap();

        int size = citys.size();
        String[] AllZm = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        HashMap hashMapOne = new HashMap();
        for (int i = 0; i < AllZm.length; i++) {
            //Comparator 排序不准确才用这个摘取字母的笨方法 26个循环
            List listTZm = new ArrayList();
            for (int j = 0; j < size; j++) {
                String zm = PinyinUtil.toPinyinWithPolyphone(citys.get(j)).substring(0, 1).toUpperCase();
                if (AllZm[i].equals(zm)){
                    HashMap hashMap1 = new HashMap();
                    hashMap1.put("name", citys.get(j));
                    listTZm.add(hashMap1);
                }
            }
            if (listTZm.size() > 0) {
                HashMap hashMap2 = new HashMap();
                hashMap2.put(AllZm[i], listTZm);
                hashMapOne.putAll(hashMap2);
            }

        }
//        String lastZm = "";
//        List listZm = null;
//        for (int i = 0; i < size; i++) {
//            String zm = PinyinUtil.toPinyinWithPolyphone(citys.get(i)).substring(0, 1).toUpperCase();
//
//            if (i == 0) {
//                listZm = new ArrayList();
//                HashMap hashMap1 = new HashMap();
//                hashMap1.put("name", citys.get(0));
//                lastZm = zm;
//                listZm.add(hashMap1);
//            } else {
//                if (zm.equals(lastZm)) {
//                    HashMap hashMap1 = new HashMap();
//                    hashMap1.put("name", citys.get(i));
//                    listZm.add(hashMap1);
//                } else {
//                    //当不等于的时候，把上一个字母放进去，然后清空重新计算
//                    HashMap hashMap2 = new HashMap();
//                    hashMap2.put(lastZm, listZm);
//                    hashMapOne.putAll(hashMap2);
//                    listZm = new ArrayList();
//                    listZm.clear();
//                    lastZm = zm;
//                    HashMap hashMap3 = new HashMap();
//                    hashMap3.put("name", citys.get(i));
//                    listZm.add(hashMap3);
//
//                }
//                if (i == size - 1) {
//                    //最后一个字母会一直相同，需要把本字母放进去
//                    HashMap hashMap1 = new HashMap();
//                    hashMap1.put(lastZm, listZm);
//                    hashMapOne.putAll(hashMap1);
//                }
//
//            }
//        }
        hashMap.put("cities", hashMapOne);
        return hashMap;
    }

    @FormatResult
    @GetMapping(value = "/setChangeHotTop")
    @ApiOperation("修改热榜榜单历史数据")
    public Object setChangeHotTop() throws TRSException {
        List<HotTop> hotTops = hotTopRepository.findAll();
        for (HotTop hotTop : hotTops) {
            switch (hotTop.getName()) {
                case "微博":
                    hotTop.setChildrenSort("热搜榜;话题榜;娱乐榜;要闻榜");
                    break;
                case "百度":
                    hotTop.setChildrenSort("百度热搜");
                    break;
                case "今日头条":
                    hotTop.setChildrenSort("头条热榜;同城榜");
                    hotTop.setLocation("北京");
                    break;
                case "抖音":
                    hotTop.setChildrenSort("热点榜;娱乐榜;社会榜;同城榜");
                    hotTop.setLocation("北京");
                    break;
            }
        }
        hotTopRepository.save(hotTops);
        return "success";
    }


}
