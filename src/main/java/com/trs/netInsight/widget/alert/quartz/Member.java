package com.trs.netInsight.widget.alert.quartz;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("serial")
public class Member implements Serializable{
    private String name;
    private int age;
    private Date birthday;
    private double soruce;
    //关于邮件的信息
    private Map<String,Object> map;
    public Member() {
        super();
    }
    public Member(String name, int age, Date birthday, double soruce,Map<String,Object> map) {
        super();
        this.name = name;
        this.age = age;
        this.birthday = birthday;
        this.soruce = soruce;
        //关于邮件的信息
        this.setMap(map);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public Date getBirthday() {
        return birthday;
    }
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    public double getSoruce() {
        return soruce;
    }
    public void setSoruce(double soruce) {
        this.soruce = soruce;
    }
    public Map<String,Object> getMap() {
		return map;
	}
	public void setMap(Map<String,Object> map) {
		this.map = map;
	}
	
    @Override
    public String toString() {
        return "Member [name=" + name + ", age=" + age + ", birthday=" + birthday + 
        		", soruce=" + soruce + ",map="+map+",alertEntity="+"]";
    }
	

}
