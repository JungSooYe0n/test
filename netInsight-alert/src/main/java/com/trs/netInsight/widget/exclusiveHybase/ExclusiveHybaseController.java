package com.trs.netInsight.widget.exclusiveHybase;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSInputRecord;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/exclusive")
@Api(description = "专享库操作接口")
@Slf4j
public class ExclusiveHybaseController {
    @Autowired
    private FullTextSearch hybase8SearchServiceNew;

    @Autowired
    private ICommonListService commonListService;

    @Autowired
    private IHybaseShardService hybaseShardService;

    @FormatResult
    @EnableRedis
    @ApiOperation("修改列表情绪标")
    @RequestMapping(value = "/updateEmotionFlag", method = RequestMethod.POST)
    public Object updateEmotionFlag(@ApiParam("被修改数据的ID") @RequestParam(value = "sid", required = true) String sid,
                                    @ApiParam("被修改数据的groupName") @RequestParam(value = "groupName", required = true) String groupName,
                                    @ApiParam("修改值") @RequestParam(value = "emotion", required = true) String emotion) throws TRSSearchException,com.trs.hybase.client.TRSException,TRSException {
        QueryBuilder queryBuilder = new QueryBuilder();
        if (Const.PAGE_SHOW_WEIBO.contains(groupName)){
            queryBuilder.filterField(FtsFieldConst.FIELD_MID, sid, Operator.Equal);
        }else if(Const.PAGE_SHOW_WEIXIN.equals(groupName)){
            queryBuilder.filterField(FtsFieldConst.FIELD_HKEY, sid, Operator.Equal);
        }else {
            queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
        }
        queryBuilder.page(0, 1);
        InfoListResult infoListResult = commonListService.queryPageList(queryBuilder,false,false,false,groupName,"detail", UserUtils.getUser(),false);
        PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
        List<FtsDocumentCommonVO> ftsQuery = content.getPageItems();
        if (null != ftsQuery && ftsQuery.size() > 0) {
//        	修改 情绪 标
            for (FtsDocumentCommonVO ftsDocumentCommonVO : ftsQuery) {
                updateEmotion(ftsDocumentCommonVO,emotion);
            }
        }
        return "success";
    }

    private void updateEmotion(FtsDocumentCommonVO ftsDocumentCommonVO,String emotion) throws com.trs.hybase.client.TRSException,TRSException{
        User user = UserUtils.getUser();
        if (ObjectUtil.isNotEmpty(user) && user.isExclusiveHybase()){
//			有小库情况下  才能执行此操作
            if (ObjectUtil.isNotEmpty(ftsDocumentCommonVO)){
                String groupName = ftsDocumentCommonVO.getGroupName();
                TRSInputRecord trsInputRecord = new TRSInputRecord();
                trsInputRecord.setUid(ftsDocumentCommonVO.getSysUid());
                trsInputRecord.addColumn(FtsFieldConst.FIELD_APPRAISE, emotion);

                if (StringUtil.isNotEmpty(user.getOrganizationId())){
                    HybaseShard trsHybaseShard = hybaseShardService.findByOrganizationId(user.getOrganizationId());
                    if(ObjectUtil.isNotEmpty(trsHybaseShard)){
                        String database = trsHybaseShard.getTradition();
                        if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
                            database = trsHybaseShard.getWeiBo();
                        } else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
                            database = trsHybaseShard.getWeiXin();
                        } else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
                            database = trsHybaseShard.getOverseas();
                        } else if (Const.MEDIA_TYPE_VIDEO.contains(groupName)){
                            database = trsHybaseShard.getVideo();
                        }
                        hybase8SearchServiceNew.updateRecords(database, trsInputRecord);
                    }
                }

            }
        }

    }
}
