package com.trs.netInsight.util;

import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author lilyy
 * @date 2019/8/19 12:39
 */
public class PasswordUtil {

    public static final String sault = "Kw9d$wx[NNL^$9qdK";

    /**
     * 自动注册登录获得明文
     * @param username
     * @param msec
     * @return
     */
    public static String getToken(String username,String msec){
        // spring自带工具包DigestUtils
        String encrypting = username+msec+sault;
        return DigestUtils.md5DigestAsHex(encrypting.getBytes());
    }

    /**
     * 网察忘记密码后生成一个123456
     */
    public static void getPwd(){
        // 加密
        String salt = UUID.randomUUID().toString();// 加密的salt
        // 加密后的密码
        String encryptPsw = UserUtils.getEncryptPsw("123456", salt);
        System.out.println("salt--->"+salt);
        System.out.println("encryptPsw--->"+encryptPsw);
    }
    public static void main(String args[]){
//        getPwd();
        String pwd = getToken("yaokeee3","2019");
        System.out.println("pwd--->"+pwd); //d07d872895e21ff21d25141b39254ac8
    }

}
