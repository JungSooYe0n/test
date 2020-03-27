//package com.trs.support.ssh;
//
//import com.jcraft.jsch.UserInfo;
//
//public class MyUserInfo implements UserInfo {
//
//    @Override
//    public String getPassphrase() {
//        // TODO Auto-generated method stub
//        log.error("MyUserInfo.getPassphrase()");
//        return null;
//    }
//
//    @Override
//    public String getPassword() {
//        // TODO Auto-generated method stub
//        log.error("MyUserInfo.getPassword()");
//        return null;
//    }
//
//    @Override
//    public boolean promptPassphrase(String arg0) {
//        // TODO Auto-generated method stub
//        log.error("MyUserInfo.promptPassphrase()");
//        log.error(arg0);
//        return false;
//    }
//
//    @Override
//    public boolean promptPassword(String arg0) {
//        // TODO Auto-generated method stub
//        log.error("MyUserInfo.promptPassword()");
//        log.error(arg0);
//        return false;
//    }
//
//    @Override
//    public boolean promptYesNo(String arg0) {
//        // TODO Auto-generated method stub'
//        log.error("MyUserInfo.promptYesNo()");
//        log.error(arg0);
//        if (arg0.contains("The authenticity of host")) {
//            return true;
//        }
//        return true;
//    }
//
//    @Override
//    public void showMessage(String arg0) {
//        // TODO Auto-generated method stub
//        log.error("MyUserInfo.showMessage()");
//    }
//
//}