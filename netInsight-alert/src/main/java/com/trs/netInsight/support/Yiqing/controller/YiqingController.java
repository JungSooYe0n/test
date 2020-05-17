package com.trs.netInsight.support.Yiqing.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.Yiqing.entity.Yiqing;
import com.trs.netInsight.support.Yiqing.service.IYiqingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * openApi暴露接口,接口内部无须处理异常,直接抛出
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月17日
 */
@Slf4j
@RestController
@RequestMapping("/yq")
public class YiqingController {

    @Autowired
    private IYiqingService iYiqingService;

    /**
     * 获取pageList
     *
     * @param request
     * @return
     * @Return : Object
     * http://localhost:28088/netInsight/yq/getTxt?name=403
     */
    @FormatResult
    @GetMapping(value = "/getTxt")
    public Object readFile(@RequestParam(value = "name") String name, HttpServletRequest request) {
        iYiqingService.readTxt(name,null);
        return "ok";
    }

    /**
     * http://localhost:28088/netInsight/yq/getData?name=403
     * @param name
     * @return
     */
    @FormatResult
    @GetMapping("/getData")
    public Object getData(@RequestParam(value = "name") String name) {
        Yiqing yiqing = iYiqingService.getData(name);
        if(yiqing!=null){
            JSONObject all = new JSONObject();
            all.put("name",yiqing.getName());
            JSONObject jsonObject = JSON.parseObject(yiqing.getValue());
            all.put("data",jsonObject);
            return all;
        }
        return null;
    }

}
